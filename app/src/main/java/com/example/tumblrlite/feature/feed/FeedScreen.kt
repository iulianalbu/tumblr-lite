package com.example.tumblrlite.feature.feed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.example.tumblrlite.domain.model.FeedPost
import com.example.tumblrlite.domain.repository.TumblrApiException

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
fun FeedScreen(
    isLiveFollowingFeed: Boolean,
    onOpenSample: () -> Unit,
    onReturnToLive: () -> Unit,
    modifier: Modifier = Modifier,
    listBottomPadding: androidx.compose.ui.unit.Dp = 24.dp,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val posts = (if (isLiveFollowingFeed) viewModel.followingPosts else viewModel.samplePosts).collectAsLazyPagingItems()
    PhotoFeedContent(
        posts = posts,
        modifier = modifier,
        listBottomPadding = listBottomPadding,
        bannerLabel = if (isLiveFollowingFeed) null else "Sample preview · mock content",
        bannerActionLabel = if (isLiveFollowingFeed) null else "Live feed",
        onBannerAction = if (isLiveFollowingFeed) null else onReturnToLive,
        onOpenSample = if (isLiveFollowingFeed) onOpenSample else null,
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
fun PhotoFeedContent(
    posts: LazyPagingItems<FeedPost>,
    modifier: Modifier = Modifier,
    listBottomPadding: androidx.compose.ui.unit.Dp = 24.dp,
    bannerLabel: String? = null,
    bannerActionLabel: String? = null,
    onBannerAction: (() -> Unit)? = null,
    onOpenSample: (() -> Unit)? = null,
) {
    var expandedPost by remember { mutableStateOf<FeedPost?>(null) }

    PullToRefreshBox(
        isRefreshing = posts.loadState.refresh is LoadState.Loading && posts.itemCount > 0,
        onRefresh = posts::refresh,
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0 && bannerLabel == null -> FeedLoading()
            posts.loadState.refresh is LoadState.Error && posts.itemCount == 0 -> {
                val error = (posts.loadState.refresh as LoadState.Error).error
                FeedError(
                    message = error.feedErrorMessage(),
                    onRetry = posts::retry,
                    onOpenSample = onOpenSample,
                )
            }
            posts.itemCount == 0 && posts.loadState.refresh is LoadState.NotLoading -> FeedEmpty()
            else -> LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 6.dp, bottom = listBottomPadding),
                verticalItemSpacing = 6.dp,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (bannerLabel != null) {
                    item(key = "sample-label", span = { StaggeredGridItemSpan.FullLine }) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                bannerLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                            if (onBannerAction != null && bannerActionLabel != null) {
                                TextButton(onClick = onBannerAction) { Text(bannerActionLabel) }
                            }
                        }
                    }
                }
                if (posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0) {
                    item(key = "initial-loading", span = { StaggeredGridItemSpan.FullLine }) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                (posts.loadState.refresh as? LoadState.Error)?.let { refreshError ->
                    item(key = "refresh-error", span = { StaggeredGridItemSpan.FullLine }) {
                        Row(
                            Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(refreshError.error.feedErrorMessage(), style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = posts::retry) { Text("Retry") }
                        }
                    }
                }
                items(
                    count = posts.itemCount,
                    key = { index -> posts.peek(index)?.id ?: "photo-placeholder-$index" },
                    contentType = { "photo" },
                ) { index ->
                    posts[index]?.takeIf { it.mediaUrl != null }?.let { post ->
                        PhotoTile(post = post, onClick = { expandedPost = post })
                    }
                }
                item(key = "append-state", span = { StaggeredGridItemSpan.FullLine }) {
                    when (val append = posts.loadState.append) {
                        is LoadState.Loading -> Box(
                            Modifier.fillMaxWidth().padding(18.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator() }
                        is LoadState.Error -> Row(
                            Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(append.error.feedErrorMessage(), style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = posts::retry) { Text("Retry") }
                        }
                        is LoadState.NotLoading -> Unit
                    }
                }
            }
        }
    }

    expandedPost?.let { post ->
        PhotoDetailDialog(post = post, onDismiss = { expandedPost = null })
    }
}

@Composable
private fun PhotoTile(post: FeedPost, onClick: () -> Unit) {
    val imageFailed = remember(post.mediaUrl) { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio((post.mediaAspectRatio ?: 0.85f).coerceIn(0.58f, 1.65f))
            .clip(RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Open photo from ${post.blogName}" },
    ) {
        AsyncImage(
            model = post.mediaUrl,
            contentDescription = post.mediaDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onState = { state -> imageFailed.value = state is AsyncImagePainter.State.Error },
        )
        if (imageFailed.value) {
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text("Image unavailable", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PhotoDetailDialog(post: FeedPost, onDismiss: () -> Unit) {
    var loved by rememberSaveable(post.id) { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            AsyncImage(
                model = post.mediaUrl,
                contentDescription = post.mediaDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp),
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close photo", tint = Color.White)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f)),
                        ),
                    )
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 44.dp, bottom = 12.dp),
            ) {
                IconButton(onClick = { loved = !loved }, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = if (loved) "Remove love" else "Love post",
                        tint = if (loved) Color(0xFFFF5C8A) else Color.White,
                    )
                }
                Spacer(Modifier.height(4.dp))
                BlogAttribution(post.id, post.blogName, "Posted by")
                post.rebloggedFromName
                    ?.takeIf { it.isNotBlank() && it != post.blogName }
                    ?.let { BlogAttribution(post.id, it, "Reblogged from") }
            }
        }
    }
}

@Composable
private fun BlogAttribution(postId: String, blogName: String, prefix: String) {
    var followed by rememberSaveable(postId, blogName) { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(prefix, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f))
            Text(blogName, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }
        IconButton(
            onClick = { followed = !followed },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = if (followed) Icons.Filled.Check else Icons.Filled.Add,
                contentDescription = if (followed) "Following $blogName" else "Follow $blogName",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun FeedLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FeedEmpty() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No photo posts yet")
    }
}

@Composable
private fun FeedError(message: String, onRetry: () -> Unit, onOpenSample: (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message)
        TextButton(onClick = onRetry) { Text("Retry") }
        if (onOpenSample != null) TextButton(onClick = onOpenSample) { Text("Preview sample feed") }
    }
}

private fun Throwable.feedErrorMessage(): String = when (this) {
    is TumblrApiException -> message ?: "Tumblr request failed. Try again."
    else -> "Couldn't load Tumblr photos. Check your connection and try again."
}
