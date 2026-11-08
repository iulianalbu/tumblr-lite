package com.example.tumblrlite.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tumblrlite.domain.model.FeedPost
import com.example.tumblrlite.domain.model.PostKind
import com.example.tumblrlite.domain.repository.FeedRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeFeedRepository @Inject constructor() : FeedRepository {
    private val samplePosts = listOf(
        FeedPost("1", "fieldnotes", "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?w=96&h=96&fit=crop", "12 min ago", "A quiet morning, a long walk, and nowhere in particular to be.", PostKind.IMAGE, "https://images.unsplash.com/photo-1470252649378-9c29740c9fa8?w=900&auto=format&fit=crop", "Sunrise over a misty field"),
        FeedPost("2", "smallobservations", "https://images.unsplash.com/photo-1490730141103-6cac27aaab94?w=96&h=96&fit=crop", "38 min ago", "Some days are made of tiny good things. Remember to notice them.", PostKind.TEXT),
        FeedPost("3", "movingpixels", "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=96&h=96&fit=crop", "1 hr ago", "A little loop from the coast this afternoon.", PostKind.GIF, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=900&auto=format&fit=crop", "Coastal landscape (sample still image representing a GIF)"),
        FeedPost("4", "slowtravel", "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=96&h=96&fit=crop", "2 hr ago", "Train window thoughts. The best kind of itinerary is the one with room to change.", PostKind.VIDEO),
        FeedPost("5", "theprintroom", "https://images.unsplash.com/photo-1470770841072-f978cf4d019e?w=96&h=96&fit=crop", "3 hr ago", "Collected colors from a weekend outside the city.", PostKind.MIXED, "https://images.unsplash.com/photo-1470770841072-f978cf4d019e?w=900&auto=format&fit=crop", "Mountain lake at dusk"),
        FeedPost("6", "typeandspace", "https://images.unsplash.com/photo-1494438639946-1ebd1d20bf85?w=96&h=96&fit=crop", "Yesterday", "Make a little room for the unexpected.", PostKind.UNKNOWN),
    )

    override fun posts(): Flow<PagingData<FeedPost>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { SamplePagingSource(samplePosts) },
    ).flow

    private class SamplePagingSource(private val posts: List<FeedPost>) : PagingSource<Int, FeedPost>() {
        override fun getRefreshKey(state: PagingState<Int, FeedPost>): Int? = state.anchorPosition?.div(PAGE_SIZE)

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FeedPost> {
            delay(350)
            val page = params.key ?: 0
            val start = page * PAGE_SIZE
            if (start >= posts.size) return LoadResult.Page(emptyList(), if (page == 0) null else page - 1, null)
            val data = posts.drop(start).take(PAGE_SIZE)
            return LoadResult.Page(data, if (page == 0) null else page - 1, if (start + data.size < posts.size) page + 1 else null)
        }

        private companion object { const val PAGE_SIZE = 3 }
    }

    private companion object { const val PAGE_SIZE = 3 }
}
