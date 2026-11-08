package com.example.tumblrlite

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun opensOnFollowingAndShowsThreeNavigationDestinations() {
        composeRule.onAllNodesWithText("Following").onFirst().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("My Likes").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("For You").assertIsDisplayed()
        composeRule.onNodeWithText("Tumblr Following · live, read-only").assertDoesNotExist()
    }

    @Test
    fun secondaryTabsOpenTheirPhotoFeeds() {
        composeRule.onNodeWithContentDescription("My Likes").performClick()
        composeRule.onNodeWithText("My Likes").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("For You").performClick()
        composeRule.onNodeWithText("For You · sample picks · mock content").assertIsDisplayed()
    }
}
