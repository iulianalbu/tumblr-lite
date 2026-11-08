package com.example.tumblrlite.feature.navigation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.tumblrlite.feature.feed.FeedScreen
import com.example.tumblrlite.feature.feed.ForYouScreen
import com.example.tumblrlite.feature.feed.LikesScreen

@Composable
fun AppShell() {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.FOLLOWING) }
    var showSampleFeed by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (selectedTab) {
            AppTab.FOLLOWING -> FeedScreen(
                isLiveFollowingFeed = !showSampleFeed,
                onOpenSample = { showSampleFeed = true },
                onReturnToLive = { showSampleFeed = false },
                modifier = Modifier.statusBarsPadding().fillMaxSize(),
                listBottomPadding = 124.dp,
            )
            AppTab.LIKES -> LikesScreen(modifier = Modifier.fillMaxSize())
            AppTab.FOR_YOU -> ForYouScreen(modifier = Modifier.fillMaxSize())
        }

        FloatingBottomNavigation(
            selectedTab = selectedTab,
            onSelectTab = { selectedTab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 10.dp),
        )
    }
}

@Composable
private fun FloatingBottomNavigation(
    selectedTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .wrapContentWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 5.dp,
    ) {
        Row(
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                Row(
                    modifier = Modifier
                        .clip(if (selected) RoundedCornerShape(24.dp) else CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer,
                        )
                        .selectable(
                            selected = selected,
                            onClick = { onSelectTab(tab) },
                            role = Role.Tab,
                        )
                        .padding(horizontal = if (selected) 14.dp else 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = if (selected) null else tab.label,
                        tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (selected) {
                        Text(
                            text = tab.label,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

private enum class AppTab(val label: String, val icon: ImageVector) {
    FOLLOWING("Following", Icons.Filled.Home),
    LIKES("My Likes", Icons.Filled.Favorite),
    FOR_YOU("For You", Icons.Filled.Star),
}
