package com.example.tumblrlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tumblrlite.core.ui.theme.TumblrLiteTheme
import com.example.tumblrlite.feature.auth.AuthState
import com.example.tumblrlite.feature.auth.AuthViewModel
import com.example.tumblrlite.feature.auth.LoginScreen
import com.example.tumblrlite.feature.feed.FeedScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AuthViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            TumblrLiteTheme {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize(), color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                    when (val current = state) {
                        AuthState.SignedOut -> LoginScreen(onContinue = viewModel::beginTumblrSignIn, onPreview = viewModel::previewFeed)
                        is AuthState.SignInUnavailable -> LoginScreen(error = current.message, onContinue = viewModel::beginTumblrSignIn, onPreview = viewModel::previewFeed)
                        AuthState.Preview -> FeedScreen(onSignOut = viewModel::signOut)
                    }
                }
            }
        }
    }
}
