package com.example.movieswipe.network

import android.util.Log
import com.example.movieswipe.BuildConfig
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// --- Data Models ---
data class AuthRequest(val googleToken: String)
data class RefreshTokenRequest(val refreshToken: String)
data class AuthResponse(
    val message: String?,
    val data: AuthData?
)
data class AuthData(
    val user: Any?, // TODO: Define User model as needed
    val token: String?,
    val refreshToken: String?
)
data class RefreshTokenResponse(
    val message: String?,
    val data: RefreshTokenData?
)
data class RefreshTokenData(
    val token: String?
)

// --- Retrofit API Interface ---
interface AuthApi {
    @POST("/auth/")
    suspend fun authenticate(@Body body: AuthRequest): Response<AuthResponse>

    @POST("/auth/refresh-token")
    suspend fun refreshToken(@Body body: RefreshTokenRequest): Response<RefreshTokenResponse>
}

object ApiService {
    private const val BASE_URL = BuildConfig.BACKEND_BASE_URL
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    suspend fun authenticateWithGoogleToken(googleToken: String): Result<String> {
        return try {
            val response = authApi.authenticate(AuthRequest(googleToken))
            if (response.isSuccessful) {
                val token = response.body()?.data?.token
                if (token != null) {
                    Result.success(token)
                } else {
                    Result.failure(Exception("No token in response"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Auth error", e)
            Result.failure(e)
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<String> {
        return try {
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                val token = response.body()?.data?.token
                if (token != null) {
                    Result.success(token)
                } else {
                    Result.failure(Exception("No token in response"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Refresh error", e)
            Result.failure(e)
        }
    }
}
