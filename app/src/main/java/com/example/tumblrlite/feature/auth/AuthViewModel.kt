package com.example.tumblrlite.feature.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed interface AuthState {
    data object SignedOut : AuthState
    data object Preview : AuthState
    data class SignInUnavailable(val message: String) : AuthState
}

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val state = _state.asStateFlow()

    fun beginTumblrSignIn() {
        _state.value = AuthState.SignInUnavailable("Tumblr sign-in isn't configured. See docs/AUTH.md for the verified integration boundary.")
    }
    fun previewFeed() { _state.value = AuthState.Preview }
    fun signOut() { _state.value = AuthState.SignedOut }
}
