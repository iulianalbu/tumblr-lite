package com.example.tumblrlite.domain.model

data class TumblrAccount(
    val username: String,
    val followingCount: Int,
    val blogs: List<FollowedBlog>,
    val totalBlogs: Int,
)

data class FollowedBlog(val name: String, val title: String, val url: String?)
