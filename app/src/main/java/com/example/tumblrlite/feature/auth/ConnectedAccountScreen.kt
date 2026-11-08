package com.example.tumblrlite.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tumblrlite.domain.model.TumblrAccount

@Composable
fun ConnectedAccountScreen(
    account: TumblrAccount,
    onBack: () -> Unit,
    onOpenFollowingFeed: () -> Unit,
    onPreviewSampleFeed: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Tumblr connection verified", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Connected as ${account.username}", style = MaterialTheme.typography.bodyLarge)
            Text("Following ${account.followingCount} blogs · showing ${account.blogs.size} from this page", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onOpenFollowingFeed) { Text("Open live Following feed") }
            TextButton(onClick = onPreviewSampleFeed) { Text("Preview sample feed") }
        }
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(account.blogs, key = { it.name }) { blog ->
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(blog.title, style = MaterialTheme.typography.titleMedium)
                        Text(blog.name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
