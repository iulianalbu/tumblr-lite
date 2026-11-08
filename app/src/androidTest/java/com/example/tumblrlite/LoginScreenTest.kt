package com.example.tumblrlite

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun signedOutScreenOffersTumblrAndSamplePreview() {
        composeRule.onNodeWithText("Tumblr Lite").assertIsDisplayed()
        composeRule.onNodeWithText("Continue with Tumblr").assertIsDisplayed()
        composeRule.onNodeWithText("Preview sample feed").assertIsDisplayed()
    }
}
