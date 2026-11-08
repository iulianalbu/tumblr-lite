package com.example.tumblrlite.data.repository

import com.example.tumblrlite.data.api.FollowedBlogDto
import com.example.tumblrlite.data.api.OAuth1Credentials
import com.example.tumblrlite.data.api.TumblrApi
import com.example.tumblrlite.domain.model.FollowedBlog
import com.example.tumblrlite.domain.model.TumblrAccount
import com.example.tumblrlite.domain.repository.TumblrAccountRepository
import com.example.tumblrlite.domain.repository.TumblrApiException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitTumblrAccountRepository @Inject constructor(
    private val api: TumblrApi,
    private val credentials: OAuth1Credentials,
) : TumblrAccountRepository {
    override suspend fun loadAccount(): TumblrAccount {
        if (!credentials.isConfigured) throw TumblrApiException.MissingCredentials
        try {
            val infoEnvelope = api.userInfo()
            val info = infoEnvelope.response?.user ?: throw TumblrApiException.MalformedResponse
            val followingEnvelope = api.following(limit = 20, offset = 0)
            val following = followingEnvelope.response ?: throw TumblrApiException.MalformedResponse
            return TumblrAccount(
                username = info.name?.takeIf(String::isNotBlank) ?: throw TumblrApiException.MalformedResponse,
                followingCount = info.following ?: following.total_blogs ?: following.blogs.size,
                blogs = following.blogs.mapNotNull { it.toDomain() },
                totalBlogs = following.total_blogs ?: following.blogs.size,
            )
        } catch (failure: TumblrApiException) {
            throw failure
        } catch (failure: HttpException) {
            when (failure.code()) {
                401 -> throw TumblrApiException.Unauthorized
                429 -> throw TumblrApiException.RateLimited(failure.response()?.headers()?.get("Retry-After"))
                else -> throw TumblrApiException.HttpFailure(failure.code())
            }
        } catch (failure: SerializationException) {
            throw TumblrApiException.MalformedResponse
        } catch (failure: IOException) {
            throw TumblrApiException.Connectivity
        } catch (failure: IllegalArgumentException) {
            throw TumblrApiException.MalformedResponse
        }
    }

    private fun FollowedBlogDto.toDomain(): FollowedBlog? {
        val blogName = name?.takeIf(String::isNotBlank) ?: return null
        return FollowedBlog(name = blogName, title = title?.takeIf(String::isNotBlank) ?: blogName, url = url)
    }
}
