package com.example.tumblrlite.domain.model

enum class PostKind { TEXT, IMAGE, GIF, VIDEO, MIXED, UNKNOWN }

data class FeedPost(
    val id: String,
    val blogName: String,
    val avatarUrl: String?,
    val timestampLabel: String,
    val text: String,
    val kind: PostKind,
    val mediaUrl: String? = null,
    val mediaDescription: String? = null,
    val mediaAspectRatio: Float? = null,
    val rebloggedFromName: String? = null,
)
