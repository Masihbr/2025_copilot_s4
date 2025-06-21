package com.example.movieswipe.network

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.movieswipe.BuildConfig
import com.example.movieswipe.data.TokenManager
import com.example.movieswipe.LoginActivity
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenManager = TokenManager.getInstance(context)
        val accessToken = tokenManager.getAccessToken()
        val refreshToken = tokenManager.getRefreshToken()
        val originalRequest = chain.request()
        val requestWithAuth = if (!accessToken.isNullOrEmpty()) {
            Log.d("AuthInterceptor", "Adding access token to request header.")
            originalRequest.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            Log.d("AuthInterceptor", "No access token found, sending request without Authorization header.")
            originalRequest
        }
        val response = chain.proceed(requestWithAuth)
        // Log expiry info for access and refresh tokens
        fun getExpiry(token: String?): Long? {
            if (token.isNullOrEmpty()) return null
            return try {
                val parts = token.split(".")
                if (parts.size < 2) return null
                val payload = android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
                val json = org.json.JSONObject(String(payload))
                json.optLong("exp", 0)
            } catch (e: Exception) { null }
        }
        val now = System.currentTimeMillis() / 1000
        val accessExp = getExpiry(accessToken)
        val refreshExp = getExpiry(refreshToken)
        val threshold = BuildConfig.EXPIRATION_THRESHOLD_SECONDS
        Log.d("AuthInterceptor", "now: $now, accessExp: $accessExp, refreshExp: $refreshExp, threshold: $threshold")
        if (accessExp != null) {
            Log.d("AuthInterceptor", "Access token seconds to expiry: ${accessExp - now}")
        }
        if (refreshExp != null) {
            Log.d("AuthInterceptor", "Refresh token seconds to expiry: ${refreshExp - now}")
        }
        if (response.code == 401) {
            val accessTokenExpired = accessExp == null || (accessExp - now) <= threshold
            Log.d("AuthInterceptor", "401 received. accessTokenExpired=$accessTokenExpired, accessExp=$accessExp, now=$now, threshold=$threshold")
            if (!refreshToken.isNullOrEmpty() && accessTokenExpired) {
                Log.w("AuthInterceptor", "Access token expired or about to expire. Attempting to refresh access token.")
                response.close()
                // Try to refresh the access token synchronously
                val newAccessToken = runBlocking {
                    val result = AuthService.refreshToken(refreshToken)
                    result.getOrNull()
                }
                return if (newAccessToken != null) {
                    Log.i("AuthInterceptor", "Token refresh successful, retrying request with new access token.")
                    tokenManager.saveTokens(newAccessToken, refreshToken)
                    val newRequest = originalRequest.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $newAccessToken")
                        .build()
                    chain.proceed(newRequest)
                } else {
                    Log.e("AuthInterceptor", "Token refresh failed or refresh token expired. Clearing tokens and redirecting to login.")
                    tokenManager.clearTokens()
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    response
                }
            } else {
                Log.e("AuthInterceptor", "Received 401 and either no refresh token or access token not expired. Clearing tokens and redirecting to login.")
                tokenManager.clearTokens()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                return response
            }
        } else {
            Log.d("AuthInterceptor", "Request successful or failed with non-401 status: ${response.code}")
        }
        return response
    }
}
