package com.example.tumblrlite.domain.repository

import androidx.paging.PagingData
import com.example.tumblrlite.domain.model.FeedPost
import kotlinx.coroutines.flow.Flow

interface LikesFeedRepository {
    fun posts(): Flow<PagingData<FeedPost>>
}
