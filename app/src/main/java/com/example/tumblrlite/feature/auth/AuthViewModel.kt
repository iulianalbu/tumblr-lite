package com.example.tumblrlite.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tumblrlite.domain.model.TumblrAccount
import com.example.tumblrlite.domain.repository.TumblrAccountRepository
import com.example.tumblrlite.domain.repository.TumblrApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed interface AuthState {
    data object SignedOut : AuthState
    data object Preview : AuthState
    data object Connecting : AuthState
    data class Connected(val account: TumblrAccount) : AuthState
    data class FollowingFeed(val account: TumblrAccount) : AuthState
    data class ConnectionFailed(val message: String) : AuthState
}

@HiltViewModel
class AuthViewModel @Inject constructor(private val accountRepository: TumblrAccountRepository) : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.SignedOut)
    val state = _state.asStateFlow()

    fun checkTumblrConnection() {
        if (_state.value == AuthState.Connecting) return
        _state.value = AuthState.Connecting
        viewModelScope.launch {
            _state.value = try {
                AuthState.Connected(accountRepository.loadAccount())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: TumblrApiException) {
                AuthState.ConnectionFailed(failure.message ?: "Tumblr connection failed")
            } catch (_: Exception) {
                AuthState.ConnectionFailed("Tumblr connection failed. Try again.")
            }
        }
    }
    fun previewFeed() { _state.value = AuthState.Preview }
    fun openFollowingFeed() {
        val connected = _state.value as? AuthState.Connected ?: return
        _state.value = AuthState.FollowingFeed(connected.account)
    }
    fun showConnectedAccount() {
        val following = _state.value as? AuthState.FollowingFeed ?: return
        _state.value = AuthState.Connected(following.account)
    }
    fun signOut() { _state.value = AuthState.SignedOut }
}
