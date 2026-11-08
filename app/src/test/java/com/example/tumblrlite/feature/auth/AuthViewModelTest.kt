package com.example.tumblrlite.feature.auth

import com.example.tumblrlite.domain.repository.TumblrAccountRepository
import com.example.tumblrlite.domain.repository.TumblrApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun missingCredentialsAreShownAsConnectionFailure() = runTest(dispatcher) {
        val viewModel = AuthViewModel(TumblrAccountRepository { throw TumblrApiException.MissingCredentials })

        viewModel.checkTumblrConnection()
        assertEquals(AuthState.Connecting, viewModel.state.value)
        runCurrent()

        assertTrue(viewModel.state.value is AuthState.ConnectionFailed)
    }

    @Test
    fun previewCanReturnToSignedOutState() = runTest(dispatcher) {
        val viewModel = AuthViewModel(TumblrAccountRepository { throw TumblrApiException.MissingCredentials })

        viewModel.previewFeed()
        viewModel.signOut()

        assertEquals(AuthState.SignedOut, viewModel.state.value)
    }
}
