package com.example.tumblrlite.feature.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.example.tumblrlite.domain.model.FeedPost
import com.example.tumblrlite.domain.model.PostKind

@Composable
fun FeedScreen(onSignOut: () -> Unit, viewModel: FeedViewModel = hiltViewModel()) {
    val posts = viewModel.posts.collectAsLazyPagingItems()
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Following", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Sample preview · no Tumblr account connected", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            }
            TextButton(onClick = onSignOut) { Text("Back") }
        }
        when {
            posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0 -> FeedLoading()
            posts.loadState.refresh is LoadState.Error && posts.itemCount == 0 -> FeedError(onRetry = posts::retry)
            posts.itemCount == 0 && posts.loadState.refresh is LoadState.NotLoading -> FeedEmpty()
            else -> PullToRefreshBox(
                isRefreshing = posts.loadState.refresh is LoadState.Loading && posts.itemCount > 0,
                onRefresh = posts::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(posts.itemCount, key = { index -> posts[index]?.id ?: "placeholder-$index" }, contentType = { index -> posts[index]?.kind ?: "placeholder" }) { index ->
                        posts[index]?.let { PostCard(it) }
                    }
                    item(key = "append-state", contentType = "loading") {
                        when (posts.loadState.append) {
                            is LoadState.Loading -> Box(Modifier.fillMaxWidth().padding(18.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                            is LoadState.Error -> TextButton(onClick = posts::retry, modifier = Modifier.fillMaxWidth()) { Text("Couldn't load more · tap to retry") }
                            is LoadState.NotLoading -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(post: FeedPost) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = post.avatarUrl, contentDescription = "${post.blogName} avatar", modifier = Modifier.size(42.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(post.blogName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(post.timestampLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(post.kind.label(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            }
            if (post.text.isNotBlank()) Text(post.text, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp), style = MaterialTheme.typography.bodyLarge)
            when (post.kind) {
                PostKind.IMAGE, PostKind.GIF, PostKind.MIXED -> post.mediaUrl?.let {
                    AsyncImage(model = it, contentDescription = post.mediaDescription, modifier = Modifier.fillMaxWidth().height(250.dp), contentScale = ContentScale.Crop)
                }
                PostKind.VIDEO -> Box(Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 16.dp).clip(RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Text("Video preview · playback is planned for a later milestone", modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                PostKind.TEXT, PostKind.UNKNOWN -> Unit
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun PostKind.label(): String = when (this) {
    PostKind.TEXT -> "Text"
    PostKind.IMAGE -> "Photo"
    PostKind.GIF -> "GIF sample"
    PostKind.VIDEO -> "Video"
    PostKind.MIXED -> "Mixed"
    PostKind.UNKNOWN -> "Post"
}

@Composable private fun FeedLoading() {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(3) { Card(Modifier.fillMaxWidth().height(180.dp)) {} }
    }
}
@Composable private fun FeedEmpty() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No posts yet") } }
@Composable private fun FeedError(onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Couldn't load the feed")
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}
