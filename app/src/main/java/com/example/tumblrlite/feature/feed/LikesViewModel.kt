package com.example.tumblrlite.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.tumblrlite.domain.repository.LikesFeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LikesViewModel @Inject constructor(
    repository: LikesFeedRepository,
) : ViewModel() {
    val likedPosts = repository.posts().cachedIn(viewModelScope)
}
