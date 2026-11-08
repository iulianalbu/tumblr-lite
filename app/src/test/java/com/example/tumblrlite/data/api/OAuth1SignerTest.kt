package com.example.tumblrlite.data.api

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OAuth1SignerTest {
    @Test
    fun matchesRfc5849Section312RequestSignatureThroughOkHttp() {
        // Public RFC 5849 example values; these are not application credentials.
        val credentials = OAuth1Credentials(
            consumerKey = "9djdj82h48djs9d2",
            consumerSecret = "j49sk3j29djd",
            token = "kkk9d7dh3k39sjv7",
            tokenSecret = "dh893hdasih9",
        )
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("ok"))
        server.start()
        try {
            val rfcRequest = Request.Builder()
                .url("http://example.com/request?b5=%3D%253D&a3=a&c%40=&a2=r%20b")
                .post(FormBody.Builder().add("c2", "").add("a3", "2 q").build())
                .build()
            val signer = OAuth1Signer(credentials, nonce = { "7d8f3e4a" }, timestampSeconds = { 137131201L })
            val rfcSignature = signer.signature(
                rfcRequest,
                mapOf(
                    "oauth_consumer_key" to "9djdj82h48djs9d2",
                    "oauth_token" to "kkk9d7dh3k39sjv7",
                    "oauth_signature_method" to "HMAC-SHA1",
                    "oauth_timestamp" to "137131201",
                    "oauth_nonce" to "7d8f3e4a",
                ),
            )
            // Verified RFC 5849 erratum 2550 corrects the published POST value (it was calculated as GET).
            assertEquals("RFC 5849 corrected POST signature", "r6/TJjbCOr97/+UU0NsvSne7s5g=", rfcSignature)

            val serverRequest = Request.Builder()
                .url(server.url("/request?tag=one&tag=two&empty="))
                .post(FormBody.Builder().add("body", "space value").build())
                .build()
            val response = OkHttpClient.Builder().addInterceptor(signer).build().newCall(serverRequest).execute()
            response.close()
            val authorization = server.takeRequest().getHeader("Authorization").orEmpty()
            val expected = signer.signature(serverRequest, mapOf(
                "oauth_consumer_key" to credentials.consumerKey,
                "oauth_nonce" to "7d8f3e4a",
                "oauth_signature_method" to "HMAC-SHA1",
                "oauth_timestamp" to "137131201",
                "oauth_token" to credentials.token,
                "oauth_version" to "1.0",
            ))
            assertTrue(authorization.contains("oauth_signature=\"${OAuth1Signer.percentEncode(expected)}\""))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun percentEncodingUsesRfcUnreservedSetAndUtf8() {
        assertTrue(OAuth1Signer.percentEncode("a b~!*'()é") == "a%20b~%21%2A%27%28%29%C3%A9")
    }
}
