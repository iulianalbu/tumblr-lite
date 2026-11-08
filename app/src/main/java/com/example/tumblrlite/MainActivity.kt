package com.example.tumblrlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tumblrlite.core.ui.theme.TumblrLiteTheme
import com.example.tumblrlite.feature.navigation.AppShell
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TumblrLiteTheme {
                AppShell()
            }
        }
    }
}
