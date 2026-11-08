package com.example.tumblrlite.data.repository

import com.example.tumblrlite.data.api.OAuth1Credentials
import com.example.tumblrlite.data.api.OAuth1Signer
import com.example.tumblrlite.data.api.TumblrApi
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

class RetrofitTumblrAccountRepositoryTest {
    @Test
    fun deserializesAccountAndFollowingAndMapsDomainModels() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("""{"meta":{"status":200,"msg":"OK"},"response":{"user":{"name":"myblog","following":37}}}"""))
        server.enqueue(MockResponse().setBody("""{"meta":{"status":200,"msg":"OK"},"response":{"total_blogs":37,"blogs":[{"name":"staff","title":"Staff","url":"https://staff.tumblr.com"}]}}"""))
        server.start()
        try {
            val repository = repository(server)
            val account = repository.loadAccount()
            assertEquals("myblog", account.username)
            assertEquals(37, account.followingCount)
            assertEquals("staff", account.blogs.single().name)
            assertEquals(37, account.totalBlogs)
            assertTrue(server.takeRequest().getHeader("Authorization").orEmpty().startsWith("OAuth "))
            assertTrue(server.takeRequest().getHeader("Authorization").orEmpty().startsWith("OAuth "))
        } finally { server.shutdown() }
    }

    @Test
    fun mapsUnauthorizedAndRateLimitResponses() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(429).addHeader("Retry-After", "60"))
        server.start()
        try {
            assertFailure(repository(server), TumblrApiException.Unauthorized)
            val limited = failure(repository(server)) as TumblrApiException.RateLimited
            assertEquals("60", limited.retryAfterSeconds)
        } finally { server.shutdown() }
    }

    @Test
    fun mapsMalformedPayloadAndConnectivityFailures() = runBlocking {
        val malformedServer = MockWebServer()
        malformedServer.enqueue(MockResponse().setBody("not-json"))
        malformedServer.start()
        try { assertFailure(repository(malformedServer), TumblrApiException.MalformedResponse) }
        finally { malformedServer.shutdown() }

        val offlineServer = MockWebServer()
        offlineServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        offlineServer.start()
        try { assertFailure(repository(offlineServer), TumblrApiException.Connectivity) }
        finally { offlineServer.shutdown() }
    }

    private fun repository(server: MockWebServer): RetrofitTumblrAccountRepository {
        // Public RFC example values only; no app or user credentials belong in fixtures.
        val credentials = OAuth1Credentials("9djdj82h48djs9d2", "j49sk3j29djd", "kkk9d7dh3k39sjv7", "dh893hdasih9")
        val client = OkHttpClient.Builder().addInterceptor(OAuth1Signer(credentials)).build()
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TumblrApi::class.java)
        return RetrofitTumblrAccountRepository(api, credentials)
    }

    private suspend fun assertFailure(repository: RetrofitTumblrAccountRepository, expected: TumblrApiException) {
        assertEquals(expected, failure(repository))
    }

    private suspend fun failure(repository: RetrofitTumblrAccountRepository): Throwable = try {
        repository.loadAccount()
        AssertionError("Expected Tumblr API failure")
    } catch (failure: Throwable) { failure }
}
