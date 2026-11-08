package com.example.tumblrlite.data.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.example.tumblrlite.data.api.OAuth1Credentials
import com.example.tumblrlite.data.api.OAuth1Signer
import com.example.tumblrlite.data.api.TumblrApi
import com.example.tumblrlite.domain.model.PostKind
import com.example.tumblrlite.domain.repository.TumblrApiException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RetrofitFollowingFeedRepositoryTest {
    @Test
    fun deserializesNpfPostsMapsToDomainAndSignsDashboardRequest() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(
            """{"meta":{"status":200,"msg":"OK"},"response":{"posts":[{"id":123,"blog_name":"staff","type":"regular","timestamp":1700000000,"content":[{"type":"text","text":"Tumblr feed post"},{"type":"image","alt_text":"A blue sky","media":[{"type":"image/jpeg","url":"https://64.media.tumblr.com/original.jpg","width":2048},{"type":"image/jpeg","url":"https://64.media.tumblr.com/feed.jpg","width":1280}]}]}]}}""",
        ))
        server.start()
        try {
            val pager = pager(server)
            val firstPage = pager.refresh() as PagingSource.LoadResult.Page
            val post = firstPage.data.single()
            assertEquals("123", post.id)
            assertEquals("staff", post.blogName)
            assertEquals("Tumblr feed post", post.text)
            assertEquals(PostKind.MIXED, post.kind)
            assertEquals("https://64.media.tumblr.com/feed.jpg", post.mediaUrl)
            assertEquals("A blue sky", post.mediaDescription)

            val request = server.takeRequest()
            assertEquals("/v2/user/dashboard?limit=20&offset=0&npf=true", request.path)
            assertTrue(request.getHeader("Authorization").orEmpty().startsWith("OAuth "))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun keepsVideoOnlyPostsOutOfPhotoFeed() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(
            """{"meta":{"status":200},"response":{"posts":[{"id":124,"blog_name":"video-blog","type":"video","content":[{"type":"video","provider":"tumblr","media":{"type":"video/mp4","url":"https://va.media.tumblr.com/video.mp4","width":640,"height":360}}]}]}}""",
        ))
        server.start()
        try {
            val page = pager(server).refresh() as PagingSource.LoadResult.Page
            assertTrue(page.data.isEmpty())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun resolvesImageFromReblogTrailWhenCurrentReblogHasNoImage() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(
            """{"meta":{"status":200},"response":{"posts":[{"id":125,"blog_name":"reblogger","type":"photo","content":[{"type":"text","text":"Worth sharing"}],"trail":[{"blog":{"name":"original-author"},"post":{"id":124},"content":[{"type":"image","alt_text":"Original photo","media":[{"type":"image/jpeg","url":"https://64.media.tumblr.com/reblog-image.jpg","width":800,"height":600}]}],"layout":[]}]}]}}""",
        ))
        server.start()
        try {
            val page = pager(server).refresh() as PagingSource.LoadResult.Page
            val post = page.data.single()
            assertEquals("Worth sharing", post.text)
            assertEquals("https://64.media.tumblr.com/reblog-image.jpg", post.mediaUrl)
            assertEquals("Original photo", post.mediaDescription)
            assertEquals("original-author", post.rebloggedFromName)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun advancesOffsetsUntilTumblrReturnsShortPage() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(dashboardBody((1..20).joinToString(",") { postJson(it) })))
        server.enqueue(MockResponse().setBody(dashboardBody("")))
        server.start()
        try {
            val pager = pager(server)
            val first = pager.refresh() as PagingSource.LoadResult.Page
            assertEquals(20, first.data.size)
            assertEquals(20, first.nextKey)
            val second = pager.append() as PagingSource.LoadResult.Page
            assertTrue(second.data.isEmpty())
            assertEquals(null, second.nextKey)
            assertEquals("/v2/user/dashboard?limit=20&offset=0&npf=true", server.takeRequest().path)
            assertEquals("/v2/user/dashboard?limit=20&offset=20&npf=true", server.takeRequest().path)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun mapsUnauthorizedRateLimitMalformedAndConnectivityFailures() = runBlocking {
        val unauthorized = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(401))
            start()
        }
        try {
            val result = pager(unauthorized).refresh() as PagingSource.LoadResult.Error
            assertEquals(TumblrApiException.Unauthorized, result.throwable)
        } finally { unauthorized.shutdown() }

        val limited = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(429).addHeader("Retry-After", "45"))
            start()
        }
        try {
            val result = pager(limited).refresh() as PagingSource.LoadResult.Error
            assertEquals(TumblrApiException.RateLimited("45"), result.throwable)
        } finally { limited.shutdown() }

        val malformed = MockWebServer().apply {
            enqueue(MockResponse().setBody("not-json"))
            start()
        }
        try {
            val result = pager(malformed).refresh() as PagingSource.LoadResult.Error
            assertEquals(TumblrApiException.MalformedResponse, result.throwable)
        } finally { malformed.shutdown() }

        val offline = MockWebServer().apply {
            enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
            start()
        }
        try {
            val result = pager(offline).refresh() as PagingSource.LoadResult.Error
            assertEquals(TumblrApiException.Connectivity, result.throwable)
        } finally { offline.shutdown() }
    }

    private fun pager(server: MockWebServer): TestPager<Int, com.example.tumblrlite.domain.model.FeedPost> {
        val credentials = OAuth1Credentials("9djdj82h48djs9d2", "j49sk3j29djd", "kkk9d7dh3k39sjv7", "dh893hdasih9")
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().addInterceptor(OAuth1Signer(credentials)).build())
            .addConverterFactory(Json { ignoreUnknownKeys = true; coerceInputValues = true }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TumblrApi::class.java)
        val source = TumblrDashboardPagingSource(api, credentials)
        return TestPager(PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false), source)
    }

    private fun dashboardBody(posts: String) = """{"meta":{"status":200,"msg":"OK"},"response":{"posts":[$posts]}}"""

    private fun postJson(id: Int) = """{"id":$id,"blog_name":"blog-$id","timestamp":1700000000,"content":[{"type":"image","media":[{"type":"image/jpeg","url":"https://64.media.tumblr.com/$id.jpg","width":600,"height":900}]}]}"""
}
