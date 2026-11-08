package com.example.tumblrlite.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.example.tumblrlite.domain.model.FeedPost
import com.example.tumblrlite.domain.model.PostKind
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleFeedPagingSourceTest {
    @Test
    fun loadsPostsAcrossPagesAndStopsAtEnd() = runBlocking {
        val pager = TestPager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSource = SampleFeedPagingSource(samplePosts, PAGE_SIZE),
        )

        val first = pager.refresh() as LoadResult.Page
        val second = pager.append() as LoadResult.Page
        val third = pager.append() as LoadResult.Page

        assertEquals(listOf("1", "2"), first.data.map(FeedPost::id))
        assertEquals(listOf("3", "4"), second.data.map(FeedPost::id))
        assertEquals(listOf("5"), third.data.map(FeedPost::id))
        assertEquals(null, third.nextKey)
    }

    @Test
    fun loadFailureIsReturnedAndCanSucceedOnRetry() = runBlocking {
        var failOnce = true
        val pager = TestPager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSource = SampleFeedPagingSource(samplePosts, PAGE_SIZE) { page ->
                if (page == 1 && failOnce) {
                    failOnce = false
                    throw IOException("Transient sample failure")
                }
            },
        )

        pager.refresh()
        val error = pager.append()
        val retried = pager.append()

        assertTrue(error is LoadResult.Error)
        assertEquals(listOf("3", "4"), (retried as LoadResult.Page).data.map(FeedPost::id))
    }

    private companion object {
        const val PAGE_SIZE = 2
        val samplePosts = (1..5).map { id ->
            FeedPost(
                id = id.toString(),
                blogName = "sample-$id",
                avatarUrl = "",
                timestampLabel = "sample",
                text = "Post $id",
                kind = PostKind.TEXT,
            )
        }
    }
}
