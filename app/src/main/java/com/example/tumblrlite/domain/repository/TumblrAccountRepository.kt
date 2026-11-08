package com.example.tumblrlite.domain.repository

import com.example.tumblrlite.domain.model.TumblrAccount

fun interface TumblrAccountRepository {
    suspend fun loadAccount(): TumblrAccount
}

sealed class TumblrApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data object MissingCredentials : TumblrApiException("Local Tumblr OAuth credentials are not configured")
    data object Unauthorized : TumblrApiException("Tumblr rejected the configured token pair (401)")
    data class RateLimited(val retryAfterSeconds: String?) : TumblrApiException(
        retryAfterSeconds?.let { "Tumblr rate limited this request (429). Retry after $it seconds." }
            ?: "Tumblr rate limited this request (429). Try again later."
    )
    data object Connectivity : TumblrApiException("Couldn't reach Tumblr. Check the network and try again.")
    data object MalformedResponse : TumblrApiException("Tumblr returned a response the app couldn't read")
    data class HttpFailure(val status: Int) : TumblrApiException("Tumblr request failed (HTTP $status)")
}
