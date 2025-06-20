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

class LoginActivity : ComponentActivity() {
    private val webClientId: String
        get() = BuildConfig.GOOGLE_WEB_CLIENT_ID
    private val credentialManager by lazy { CredentialManager.create(this) }
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieSwipeTheme {
                LoginScreen(onLoginClick = {
                    signInWithGoogle()
                })
            }
        }
    }

    private fun signInWithGoogle() {
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
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.w(TAG, "No authorized accounts found, falling back to sign-up flow.")
                Log.e(TAG, "Google sign-in error (sign-in): ${e.localizedMessage}", e)
                // If no authorized accounts, try sign-up flow
                signUpWithGoogle()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in signInWithGoogle: ${e.localizedMessage}", e)
            }
        }
    }

    private fun signUpWithGoogle() {
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
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google sign-in error (sign-up): ${e.localizedMessage}", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Google sign-in failed: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in signUpWithGoogle: ${e.localizedMessage}", e)
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is androidx.credentials.CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Log.d(TAG, "Google ID token received: $idToken")
                // TODO: Validate idToken on your backend
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Invalid Google ID token", e)
                Toast.makeText(this, "Invalid Google ID token", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in handleSignIn: ${e.localizedMessage}", e)
            }
        } else {
            Log.e(TAG, "Unexpected credential type: ${credential::class.java.simpleName}")
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
