package com.example.tumblrlite.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.tumblrlite.domain.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(repository: FeedRepository) : ViewModel() {
    val posts = repository.posts().cachedIn(viewModelScope)
}
