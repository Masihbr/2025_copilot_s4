package com.example.movieswipe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.movieswipe.ui.theme.MovieSwipeTheme
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.movieswipe.network.ApiService
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import com.example.movieswipe.data.TokenManager

class LoginActivity : ComponentActivity() {
    private val webClientId: String
        get() = BuildConfig.GOOGLE_WEB_CLIENT_ID
    private val credentialManager by lazy { CredentialManager.create(this) }
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenManager = TokenManager.getInstance(this)
        val accessToken = tokenManager.getAccessToken()
        val refreshToken = tokenManager.getRefreshToken()
        if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
            // TODO: Optionally validate token with backend or check expiry
            Log.d(TAG, "Tokens found, bypassing login.")
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            return
        }
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            var snackbarMessage by remember { mutableStateOf("") }
            LaunchedEffect(snackbarMessage) {
                if (snackbarMessage.isNotEmpty()) {
                    snackbarHostState.showSnackbar(snackbarMessage)
                    snackbarMessage = ""
                }
            }
            MovieSwipeTheme {
                Box(Modifier.fillMaxSize()) {
                    LoginScreen(onLoginClick = {
                        signInWithGoogle(snackbarHostStateSetter = { snackbarMessage = it })
                    })
                    androidx.compose.material3.SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }

    private fun signInWithGoogle(snackbarHostStateSetter: (String) -> Unit = {}) {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .setNonce("mock-nonce") // Replace with a secure nonce in production
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                Log.d(TAG, "Google sign-in success: $result")
                handleSignIn(result, snackbarHostStateSetter)
            } catch (e: GetCredentialException) {
                Log.w(TAG, "No authorized accounts found, falling back to sign-up flow.")
                Log.e(TAG, "Google sign-in error (sign-in): ${e.localizedMessage}", e)
                // If no authorized accounts, try sign-up flow
                signUpWithGoogle(snackbarHostStateSetter)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in signInWithGoogle: ${e.localizedMessage}", e)
                snackbarHostStateSetter("Sign-in failed: ${e.localizedMessage}")
            }
        }
    }

    private fun signUpWithGoogle(snackbarHostStateSetter: (String) -> Unit = {}) {
        Log.d(TAG, "signUpWithGoogle")
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setNonce("mock-nonce") // Replace with a secure nonce in production
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                Log.d(TAG, "Google sign-up success: $result")
                handleSignIn(result, snackbarHostStateSetter)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google sign-in error (sign-up): ${e.localizedMessage}", e)
                snackbarHostStateSetter("Google sign-in failed: ${e.localizedMessage}")
                Toast.makeText(
                    this@LoginActivity,
                    "Google sign-in failed: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in signUpWithGoogle: ${e.localizedMessage}", e)
                snackbarHostStateSetter("Sign-up failed: ${e.localizedMessage}")
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse, snackbarHostStateSetter: (String) -> Unit = {}) {
        val credential = result.credential
        if (credential is androidx.credentials.CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Log.d(TAG, "Google ID token received: $idToken")
                // Call backend API to authenticate
                lifecycleScope.launch {
                    snackbarHostStateSetter("Authenticating with backend...")
                    val apiResult = ApiService.authenticateWithGoogleToken(idToken)
                    apiResult.onSuccess { tokens ->
                        Log.d(TAG, "Received JWT access token: ${tokens.accessToken}")
                        Log.d(TAG, "Received JWT refresh token: ${tokens.refreshToken}")
                        val tokenManager = TokenManager.getInstance(this@LoginActivity)
                        tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }.onFailure { e ->
                        Log.e(TAG, "Backend auth failed: ${e.localizedMessage}", e)
                        snackbarHostStateSetter("Backend auth failed: ${e.localizedMessage}")
                    }
                }
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Invalid Google ID token", e)
                snackbarHostStateSetter("Invalid Google ID token")
                Toast.makeText(this, "Invalid Google ID token", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in handleSignIn: ${e.localizedMessage}", e)
                snackbarHostStateSetter("Sign-in error: ${e.localizedMessage}")
            }
        } else {
            Log.e(TAG, "Unexpected credential type: ${credential::class.java.simpleName}")
            snackbarHostStateSetter("Unexpected credential type")
            Toast.makeText(this, "Unexpected credential type", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun LoginScreen(onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onLoginClick, modifier = Modifier.wrapContentSize()) {
            Text(text = "Login")
        }
    }
}
