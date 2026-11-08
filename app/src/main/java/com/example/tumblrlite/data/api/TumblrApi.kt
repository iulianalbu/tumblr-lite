package com.example.tumblrlite.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Documented endpoint boundary only; no client or credentials are configured in this bootstrap. */
interface TumblrApi {
    @GET("v2/user/following")
    suspend fun following(@Query("limit") limit: Int? = null, @Query("offset") offset: Int? = null): ApiEnvelope<FollowingResponseDto>

    @GET("v2/blog/{blogIdentifier}/posts")
    /** Tumblr documents a maximum page size of 20. Do not send both offset and before. */
    suspend fun blogPosts(
        @Path("blogIdentifier") blogIdentifier: String,
        @Query("api_key") apiKey: String,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("before") before: Long? = null,
    ): ApiEnvelope<BlogPostsResponseDto>
}

@Serializable data class ApiEnvelope<T>(val meta: ApiMetaDto? = null, val response: T? = null)
@Serializable data class ApiMetaDto(val status: Int? = null, val msg: String? = null)
@Serializable data class FollowingResponseDto(val blogs: List<FollowedBlogDto> = emptyList(), val total_blogs: Int? = null)
@Serializable data class FollowedBlogDto(val name: String? = null, val title: String? = null, val url: String? = null)
@Serializable data class BlogPostsResponseDto(val blog: BlogDto? = null, val posts: List<ApiPostDto> = emptyList(), val total_posts: Int? = null)
@Serializable data class BlogDto(val name: String? = null, val title: String? = null, val url: String? = null)
@Serializable data class ApiPostDto(val id: Long? = null, val type: String? = null, val timestamp: Long? = null, val summary: String? = null, val body: String? = null)
