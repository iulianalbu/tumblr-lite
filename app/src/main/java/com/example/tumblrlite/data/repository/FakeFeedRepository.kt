package com.example.tumblrlite.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tumblrlite.domain.model.FeedPost
import com.example.tumblrlite.domain.model.PostKind
import com.example.tumblrlite.domain.repository.FeedRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeFeedRepository @Inject constructor() : FeedRepository {
    private val samplePosts = listOf(
        FeedPost("1", "fieldnotes", null, "12 min ago", "", PostKind.IMAGE, "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?w=900&auto=format&fit=crop", "Sunrise over a misty field", 1.5f, "slowdays"),
        FeedPost("2", "smallobservations", null, "38 min ago", "", PostKind.IMAGE, "https://images.unsplash.com/photo-1490730141103-6cac27aaab94?w=900&auto=format&fit=crop", "Soft morning light", 0.8f, null),
        FeedPost("3", "movingpixels", null, "1 hr ago", "", PostKind.IMAGE, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=900&auto=format&fit=crop", "Coastal landscape", 1.25f, "slowdays"),
        FeedPost("4", "slowtravel", null, "2 hr ago", "", PostKind.IMAGE, "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=900&auto=format&fit=crop", "Lake and mountains", 0.9f, null),
        FeedPost("5", "theprintroom", null, "3 hr ago", "", PostKind.IMAGE, "https://images.unsplash.com/photo-1470770841072-f978cf4d019e?w=900&auto=format&fit=crop", "Mountain lake at dusk", 1.4f, "fieldnotes"),
        FeedPost("6", "typeandspace", null, "Yesterday", "", PostKind.IMAGE, "https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?w=900&auto=format&fit=crop", "Quiet interior with a window", 0.75f, null),
    )

    override fun posts(): Flow<PagingData<FeedPost>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { SampleFeedPagingSource(samplePosts, PAGE_SIZE) },
    ).flow

    private companion object { const val PAGE_SIZE = 3 }
}

internal class SampleFeedPagingSource(
    private val posts: List<FeedPost>,
    private val pageSize: Int,
    private val beforeLoad: suspend (page: Int) -> Unit = {},
) : PagingSource<Int, FeedPost>() {
    override fun getRefreshKey(state: PagingState<Int, FeedPost>): Int? = state.anchorPosition?.div(pageSize)

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FeedPost> {
        val page = params.key ?: 0
        return try {
            beforeLoad(page)
            delay(350)
            val start = page * pageSize
            val data = posts.drop(start).take(pageSize)
            LoadResult.Page(
                data = data,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (start + data.size < posts.size) page + 1 else null,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LoadResult.Error(error)
        }
    }
}
