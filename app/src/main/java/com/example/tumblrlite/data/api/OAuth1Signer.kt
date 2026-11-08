package com.example.tumblrlite.data.api

import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** OAuth 1.0a HMAC-SHA1 signer for query and application/x-www-form-urlencoded requests. */
class OAuth1Signer(
    private val credentials: OAuth1Credentials,
    private val nonce: () -> String = ::secureNonce,
    private val timestampSeconds: () -> Long = { System.currentTimeMillis() / 1_000L },
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!credentials.isConfigured) throw java.io.IOException("Tumblr OAuth credentials are not configured")
        val request = chain.request()
        val oauthParameters = linkedMapOf(
            "oauth_consumer_key" to credentials.consumerKey,
            "oauth_nonce" to nonce(),
            "oauth_signature_method" to "HMAC-SHA1",
            "oauth_timestamp" to timestampSeconds().toString(),
            "oauth_token" to credentials.token,
            "oauth_version" to "1.0",
        )
        val signature = signature(request, oauthParameters)
        val header = (oauthParameters + ("oauth_signature" to signature)).entries.joinToString(", ", prefix = "OAuth ") {
            "${percentEncode(it.key)}=\"${percentEncode(it.value)}\""
        }
        return chain.proceed(request.newBuilder().header("Authorization", header).build())
    }

    internal fun signature(request: Request, oauthParameters: Map<String, String>): String {
        val url = request.url
        val body = request.body
        val allParameters = buildList {
            for (name in url.queryParameterNames) url.queryParameterValues(name).forEach { add(name to (it ?: "")) }
            if (body is FormBody) for (index in 0 until body.size) add(body.name(index) to body.value(index))
            oauthParameters.forEach { (name, value) -> if (name != "oauth_signature") add(name to value) }
        }
        val normalized = allParameters
            .map { percentEncode(it.first) to percentEncode(it.second) }
            .sortedWith(compareBy<Pair<String, String>>({ it.first }, { it.second }))
            .joinToString("&") { "${it.first}=${it.second}" }
        val baseUri = buildString {
            append(url.scheme.lowercase()).append("://").append(url.host.lowercase())
            val defaultPort = (url.scheme == "http" && url.port == 80) || (url.scheme == "https" && url.port == 443)
            if (!defaultPort) append(':').append(url.port)
            append(url.encodedPath)
        }
        val baseString = listOf(request.method.uppercase(), baseUri, normalized)
            .joinToString("&") { percentEncode(it) }
        val signingKey = "${percentEncode(credentials.consumerSecret)}&${percentEncode(credentials.tokenSecret)}"
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(signingKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA1"))
        return Base64.getEncoder().encodeToString(mac.doFinal(baseString.toByteArray(StandardCharsets.UTF_8)))
    }

    companion object {
        /** RFC 5849 section 3.6 encoding (UTF-8, unreserved characters only). */
        internal fun percentEncode(value: String): String {
            val bytes = value.toByteArray(StandardCharsets.UTF_8)
            val result = StringBuilder(bytes.size)
            for (byte in bytes) {
                val unsigned = byte.toInt() and 0xff
                if (unsigned in 'a'.code..'z'.code || unsigned in 'A'.code..'Z'.code || unsigned in '0'.code..'9'.code || unsigned == 45 || unsigned == 46 || unsigned == 95 || unsigned == 126) {
                    result.append(unsigned.toChar())
                } else {
                    result.append('%').append(HEX[unsigned ushr 4]).append(HEX[unsigned and 0x0f])
                }
            }
            return result.toString()
        }

        private val HEX = "0123456789ABCDEF".toCharArray()
        private fun secureNonce(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }
        }
    }
}

class OAuth1Credentials(
    val consumerKey: String,
    val consumerSecret: String,
    val token: String,
    val tokenSecret: String,
) {
    val isConfigured: Boolean
        get() = consumerKey.isNotBlank() && consumerSecret.isNotBlank() && token.isNotBlank() && tokenSecret.isNotBlank()

    override fun toString(): String = "OAuth1Credentials(configured=$isConfigured)"
}
