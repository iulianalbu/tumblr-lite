package com.example.tumblrlite.feature.feed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun LikesScreen(modifier: Modifier = Modifier, viewModel: LikesViewModel = hiltViewModel()) {
    PhotoFeedContent(
        posts = viewModel.likedPosts.collectAsLazyPagingItems(),
        modifier = modifier.statusBarsPadding().fillMaxSize(),
        listBottomPadding = 124.dp,
    )
}

@Composable
fun ForYouScreen(modifier: Modifier = Modifier, viewModel: FeedViewModel = hiltViewModel()) {
    PhotoFeedContent(
        posts = viewModel.samplePosts.collectAsLazyPagingItems(),
        modifier = modifier.statusBarsPadding().fillMaxSize(),
        listBottomPadding = 124.dp,
        bannerLabel = "For You · sample picks · mock content",
    )
}
