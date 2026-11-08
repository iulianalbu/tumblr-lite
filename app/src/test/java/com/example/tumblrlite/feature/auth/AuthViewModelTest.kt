package com.example.tumblrlite.feature.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthViewModelTest {
    @Test
    fun signInDoesNotPretendToAuthenticateWithoutConfiguration() {
        val viewModel = AuthViewModel()

        viewModel.beginTumblrSignIn()

        assertEquals(true, viewModel.state.value is AuthState.SignInUnavailable)
    }

    @Test
    fun previewCanReturnToSignedOutState() {
        val viewModel = AuthViewModel()

        viewModel.previewFeed()
        viewModel.signOut()

        assertEquals(AuthState.SignedOut, viewModel.state.value)
    }
}
