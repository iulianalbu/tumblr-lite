package com.example.tumblrlite.data.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit boundary for documented Tumblr API v2 endpoints. */
interface TumblrApi {
    @GET("v2/user/info")
    suspend fun userInfo(): ApiEnvelope<UserInfoResponseDto>

    @GET("v2/user/following")
    suspend fun following(@Query("limit") limit: Int? = null, @Query("offset") offset: Int? = null): ApiEnvelope<FollowingResponseDto>

    @GET("v2/user/dashboard")
    suspend fun dashboard(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("npf") npf: Boolean = true,
    ): ApiEnvelope<DashboardResponseDto>

    @GET("v2/user/likes")
    suspend fun likes(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int? = null,
        @Query("before") before: Long? = null,
    ): ApiEnvelope<UserLikesResponseDto>

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
@Serializable data class UserInfoResponseDto(val user: TumblrUserDto? = null)
@Serializable data class TumblrUserDto(val name: String? = null, val following: Int? = null)
@Serializable data class FollowedBlogDto(val name: String? = null, val title: String? = null, val url: String? = null)
@Serializable data class BlogPostsResponseDto(val blog: BlogDto? = null, val posts: List<ApiPostDto> = emptyList(), val total_posts: Int? = null)
@Serializable data class BlogDto(val name: String? = null, val title: String? = null, val url: String? = null)
@Serializable data class ApiPostDto(val id: Long? = null, val type: String? = null, val timestamp: Long? = null, val summary: String? = null, val body: String? = null)

@Serializable
data class DashboardResponseDto(
    val posts: List<DashboardPostDto> = emptyList(),
    val total_posts: Int? = null,
)

@Serializable
data class UserLikesResponseDto(
    val liked_posts: List<DashboardPostDto> = emptyList(),
    val liked_count: Int? = null,
)

@Serializable
data class DashboardPostDto(
    val id: Long? = null,
    val id_string: String? = null,
    val blog_name: String? = null,
    val type: String? = null,
    val timestamp: Long? = null,
    val summary: String? = null,
    val title: String? = null,
    val caption: String? = null,
    val content: List<NpfContentBlockDto> = emptyList(),
    val photos: List<LegacyPhotoDto> = emptyList(),
    val trail: List<NpfTrailItemDto> = emptyList(),
    val reblogged_from_name: String? = null,
    val reblogged_root_name: String? = null,
)

@Serializable
data class NpfTrailItemDto(
    val blog: NpfTrailBlogDto? = null,
    val content: List<NpfContentBlockDto> = emptyList(),
    val broken_blog_name: String? = null,
)

@Serializable
data class NpfTrailBlogDto(
    val name: String? = null,
)

@Serializable
data class LegacyPhotoDto(
    val caption: String? = null,
    val alt_sizes: List<NpfMediaDto> = emptyList(),
    val original_size: NpfMediaDto? = null,
)

@Serializable
data class NpfContentBlockDto(
    val type: String? = null,
    val text: String? = null,
    val alt_text: String? = null,
    val media: JsonElement? = null,
)

@Serializable
data class NpfMediaDto(
    val url: String? = null,
    val type: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)
