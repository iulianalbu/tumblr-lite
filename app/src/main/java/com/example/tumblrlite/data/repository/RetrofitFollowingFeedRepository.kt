package com.example.tumblrlite.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tumblrlite.data.api.DashboardPostDto
import com.example.tumblrlite.data.api.NpfMediaDto
import com.example.tumblrlite.data.api.OAuth1Credentials
import com.example.tumblrlite.data.api.TumblrApi
import com.example.tumblrlite.domain.model.FeedPost
import com.example.tumblrlite.domain.model.PostKind
import com.example.tumblrlite.domain.repository.FollowingFeedRepository
import com.example.tumblrlite.domain.repository.TumblrApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitFollowingFeedRepository @Inject constructor(
    private val api: TumblrApi,
    private val credentials: OAuth1Credentials,
) : FollowingFeedRepository {
    override fun posts(): Flow<PagingData<FeedPost>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { TumblrDashboardPagingSource(api, credentials) },
    ).flow

    private companion object { const val PAGE_SIZE = 20 }
}

internal class TumblrDashboardPagingSource(
    private val api: TumblrApi,
    private val credentials: OAuth1Credentials,
) : PagingSource<Int, FeedPost>() {
    override fun getRefreshKey(state: PagingState<Int, FeedPost>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FeedPost> {
        if (!credentials.isConfigured) return LoadResult.Error(TumblrApiException.MissingCredentials)
        val offset = params.key ?: 0
        val limit = params.loadSize.coerceIn(1, MAX_PAGE_SIZE)
        return try {
            var pageOffset = offset
            var nextOffset: Int? = null
            val photoPosts = mutableListOf<FeedPost>()
            do {
                val response = api.dashboard(limit = limit, offset = pageOffset, npf = true)
                    .response ?: throw TumblrApiException.MalformedResponse
                photoPosts += response.posts.mapNotNull(DashboardPostDto::toDomain)
                    .filter(FeedPost::isPhotoPost)
                nextOffset = if (response.posts.size < limit) null else pageOffset + response.posts.size
                if (photoPosts.isEmpty()) nextOffset?.let { pageOffset = it }
            } while (photoPosts.isEmpty() && nextOffset != null)
            LoadResult.Page(
                data = photoPosts,
                prevKey = null,
                nextKey = nextOffset,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: TumblrApiException) {
            LoadResult.Error(failure)
        } catch (failure: HttpException) {
            LoadResult.Error(
                when (failure.code()) {
                    401 -> TumblrApiException.Unauthorized
                    429 -> TumblrApiException.RateLimited(failure.response()?.headers()?.get("Retry-After"))
                    else -> TumblrApiException.HttpFailure(failure.code())
                },
            )
        } catch (failure: SerializationException) {
            LoadResult.Error(TumblrApiException.MalformedResponse)
        } catch (failure: IOException) {
            LoadResult.Error(TumblrApiException.Connectivity)
        } catch (failure: IllegalArgumentException) {
            LoadResult.Error(TumblrApiException.MalformedResponse)
        }
    }

    private companion object { const val MAX_PAGE_SIZE = 20 }
}

internal fun DashboardPostDto.toDomain(): FeedPost? {
    val postId = id_string?.takeIf(String::isNotBlank) ?: id?.toString() ?: return null
    val blog = blog_name?.takeIf(String::isNotBlank) ?: return null
    val blocks = content
    // NPF defines trail items as the previous posts in a reblog chain (oldest first).
    // Prefer the current post's blocks, then inspect the closest parent back toward the root.
    val trailBlocks = trail.asReversed().flatMap { it.content }
    val allBlocks = blocks + trailBlocks
    val text = buildList {
        title?.takeIf(String::isNotBlank)?.let(::add)
        val currentText = blocks.filter { it.type == "text" }.mapNotNull { it.text?.takeIf(String::isNotBlank) }
        val visibleText = currentText.ifEmpty {
            trailBlocks.filter { it.type == "text" }.mapNotNull { it.text?.takeIf(String::isNotBlank) }
        }
        visibleText.forEach(::add)
    }.joinToString("\n\n").ifBlank {
        (summary?.takeIf(String::isNotBlank) ?: caption).orEmpty().stripMarkup()
    }
    val imageBlocks = allBlocks.filter { it.type == "image" }
    val resolvedImage = imageBlocks.firstNotNullOfOrNull { block ->
        block.media?.toMediaList()?.toFeedMedia()?.let { block to it }
    }
    val imageBlock = resolvedImage?.first
    val npfMedia = resolvedImage?.second?.first
    val npfImageUrl = resolvedImage?.second?.second
    val npfMediaSizes = imageBlock?.media?.toMediaList().orEmpty()
    val videoBlock = allBlocks.firstOrNull { it.type == "video" }
    // Some older Tumblr posts remain in the documented legacy photo shape even when requesting NPF.
    val legacyMedia = photos.firstNotNullOfOrNull { photo ->
        (photo.alt_sizes.ifEmpty { listOfNotNull(photo.original_size) }).toFeedMedia()
    }
    val videoMediaUrl = videoBlock?.media?.toMediaList()?.toFeedImageUrl()
    val legacyImageUrl = legacyMedia?.second
    val media = npfImageUrl ?: legacyImageUrl ?: videoMediaUrl
    val hasText = allBlocks.any { it.type == "text" && !it.text.isNullOrBlank() } || text.isNotBlank()
    val kind = when {
        videoBlock != null -> PostKind.VIDEO
        imageBlock != null && hasText -> PostKind.MIXED
        imageBlock != null && npfMediaSizes.any { it.type?.contains("gif", ignoreCase = true) == true || it.url?.endsWith(".gif", true) == true } -> PostKind.GIF
        imageBlock != null || photos.isNotEmpty() -> PostKind.IMAGE
        allBlocks.any { it.type == "audio" } || type == "audio" -> PostKind.UNKNOWN
        allBlocks.any { it.type == "link" } || type == "link" -> PostKind.TEXT
        allBlocks.any { it.type == "text" } || type in setOf("text", "regular", "quote", "chat", "answer") -> PostKind.TEXT
        else -> PostKind.UNKNOWN
    }
    val timestamp = timestamp?.let {
        runCatching { Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).format(POST_DATE_FORMAT) }.getOrNull()
    }.orEmpty()
    val description = imageBlock?.alt_text?.takeIf(String::isNotBlank)
        ?: photos.firstNotNullOfOrNull { it.caption?.stripMarkup()?.takeIf(String::isNotBlank) }
    return FeedPost(
        id = postId,
        blogName = blog,
        avatarUrl = null,
        timestampLabel = timestamp,
        text = text,
        kind = kind,
        mediaUrl = media,
        mediaDescription = description ?: "Post image from $blog",
        mediaAspectRatio = npfMedia?.aspectRatio() ?: legacyMedia?.first?.aspectRatio(),
        rebloggedFromName = (
            trail.lastOrNull()?.let { it.blog?.name ?: it.broken_blog_name }
                ?: reblogged_from_name
                ?: reblogged_root_name
            )?.takeIf(String::isNotBlank),
    )
}

private fun List<NpfMediaDto>.toFeedMedia(): Pair<NpfMediaDto, String>? {
    if (isEmpty()) return null
    // Prefer a feed-sized variant for the narrow masonry columns; fall back if its URL is unusable.
    val preferred = firstOrNull { it.width == null || it.width in 1..720 }
        ?: minByOrNull { it.width ?: Int.MAX_VALUE }
    return sequenceOf(preferred).filterNotNull().plus(asSequence().filterNot { it == preferred })
        .mapNotNull { media -> media.url?.secureTumblrMediaUrl()?.let { media to it } }
        .firstOrNull()
}

private fun NpfMediaDto.aspectRatio(): Float? =
    if (width != null && height != null && width > 0 && height > 0) width.toFloat() / height else null

private fun List<NpfMediaDto>.toFeedImageUrl(): String? = toFeedMedia()?.second

private fun JsonElement.toMediaList(): List<NpfMediaDto> {
    return when (this) {
        is JsonArray -> mapNotNull { element -> runCatching { NPF_JSON.decodeFromJsonElement<NpfMediaDto>(element) }.getOrNull() }
        is JsonObject -> listOfNotNull(runCatching { NPF_JSON.decodeFromJsonElement<NpfMediaDto>(this) }.getOrNull())
        else -> emptyList()
    }
}

private val NPF_JSON = Json { ignoreUnknownKeys = true; coerceInputValues = true }

private fun String.stripMarkup(): String = replace(Regex("<[^>]*>"), " ")
    .replace("&nbsp;", " ")
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace(Regex("\\s+"), " ")
    .trim()

private fun String.secureTumblrMediaUrl(): String? {
    val httpsUrl = if (startsWith("http://", ignoreCase = true) &&
        Regex("^http://(?:[a-z0-9-]+\\.)*media\\.tumblr\\.com/", RegexOption.IGNORE_CASE).containsMatchIn(this)
    ) replaceFirst(Regex("^http://", RegexOption.IGNORE_CASE), "https://") else this
    return httpsUrl.takeIf { it.startsWith("https://", ignoreCase = true) }
}

private val POST_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d · HH:mm")

internal fun FeedPost.isPhotoPost(): Boolean = mediaUrl != null && kind in PHOTO_KINDS

private val PHOTO_KINDS = setOf(PostKind.IMAGE, PostKind.GIF, PostKind.MIXED)
