package com.example.tumblrlite.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tumblrlite.data.api.OAuth1Credentials
import com.example.tumblrlite.data.api.TumblrApi
import com.example.tumblrlite.domain.model.FeedPost
import com.example.tumblrlite.domain.repository.LikesFeedRepository
import com.example.tumblrlite.domain.repository.TumblrApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitLikesFeedRepository @Inject constructor(
    private val api: TumblrApi,
    private val credentials: OAuth1Credentials,
) : LikesFeedRepository {
    override fun posts(): Flow<PagingData<FeedPost>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { TumblrLikesPagingSource(api, credentials) },
    ).flow

    private companion object {
        const val PAGE_SIZE = 20
    }
}

internal data class LikesPageKey(val offset: Int?, val before: Long?)

internal class TumblrLikesPagingSource(
    private val api: TumblrApi,
    private val credentials: OAuth1Credentials,
) : PagingSource<LikesPageKey, FeedPost>() {
    override fun getRefreshKey(state: PagingState<LikesPageKey, FeedPost>): LikesPageKey? = null

    override suspend fun load(params: LoadParams<LikesPageKey>): LoadResult<LikesPageKey, FeedPost> {
        if (!credentials.isConfigured) return LoadResult.Error(TumblrApiException.MissingCredentials)
        val limit = params.loadSize.coerceIn(1, MAX_PAGE_SIZE)
        var pageKey = params.key ?: LikesPageKey(offset = 0, before = null)
        return try {
            val photoPosts = mutableListOf<FeedPost>()
            var nextKey: LikesPageKey?
            do {
                val response = api.likes(
                    limit = limit,
                    offset = pageKey.offset,
                    before = pageKey.before,
                ).response ?: throw TumblrApiException.MalformedResponse
                photoPosts += response.liked_posts.mapNotNull { it.toDomain() }
                    .filter(FeedPost::isPhotoPost)
                nextKey = response.nextKey(pageKey, limit)
                if (photoPosts.isEmpty()) nextKey?.let { pageKey = it }
            } while (photoPosts.isEmpty() && nextKey != null)

            LoadResult.Page(data = photoPosts, prevKey = null, nextKey = nextKey)
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

    private fun com.example.tumblrlite.data.api.UserLikesResponseDto.nextKey(
        current: LikesPageKey,
        limit: Int,
    ): LikesPageKey? {
        if (liked_posts.size < limit) return null
        val offset = current.offset
        if (offset != null && offset + liked_posts.size < MAX_OFFSET) {
            return LikesPageKey(offset = offset + liked_posts.size, before = null)
        }
        val oldestTimestamp = liked_posts.mapNotNull { it.timestamp }.minOrNull() ?: return null
        if (current.before != null && oldestTimestamp >= current.before) return null
        return LikesPageKey(offset = null, before = oldestTimestamp)
    }

    private companion object {
        const val MAX_PAGE_SIZE = 20
        const val MAX_OFFSET = 1000
    }
}
