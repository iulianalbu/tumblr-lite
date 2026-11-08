package com.example.tumblrlite.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(error: String? = null, isConnecting: Boolean = false, onCheckConnection: () -> Unit, onPreview: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tumblr Lite", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text("A quieter way to catch up on blogs you follow.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onCheckConnection, enabled = !isConnecting, modifier = Modifier.widthIn(min = 240.dp)) {
            if (isConnecting) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Check configured Tumblr account")
        }
        Spacer(Modifier.height(8.dp))
        Text("Uses local OAuth 1.0a credentials; no browser sign-in.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onPreview, modifier = Modifier.widthIn(min = 240.dp)) { Text("Preview sample feed") }
        if (error != null) {
            Spacer(Modifier.height(20.dp))
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
