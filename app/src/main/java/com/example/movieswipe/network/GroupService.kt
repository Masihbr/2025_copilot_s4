package com.example.movieswipe.network

import android.content.Context
import android.util.Log
import com.example.movieswipe.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// --- Group Models ---
data class User(
    val _id: String,
    val name: String,
    val email: String,
    val createdAt: String?,
    val updatedAt: String?
)
data class Group(
    val _id: String,
    val owner: User,
    val members: List<User>,
    val invitationCode: String,
    val createdAt: String?,
    val updatedAt: String?
)
data class GroupResponse(
    val message: String?,
    val data: Group?
)

data class GroupsListResponse(
    val message: String?,
    val data: List<Group>?
)

interface GroupApi {
    @POST("/groups")
    suspend fun createGroup(@Header("Authorization") authHeader: String): Response<GroupResponse>

    @GET("/groups")
    suspend fun getGroups(@Header("Authorization") authHeader: String): Response<GroupsListResponse>

    @GET("/groups/{groupId}")
    suspend fun getGroupById(
        @Header("Authorization") authHeader: String,
        @Path("groupId") groupId: String
    ): Response<GroupResponse>
}

object GroupService {
    private fun getRetrofit(context: Context): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
    private fun getApi(context: Context): GroupApi = getRetrofit(context).create(GroupApi::class.java)

    suspend fun createGroup(context: Context): Result<Group> {
        return try {
            val groupApi = getApi(context)
            val response = groupApi.createGroup("") // Auth header handled by interceptor
            if (response.isSuccessful) {
                val group = response.body()?.data
                if (group != null) Result.success(group)
                else Result.failure(Exception("No group data in response"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupService", "Create group error", e)
            Result.failure(e)
        }
    }

    suspend fun getGroups(context: Context): Result<List<Group>> {
        return try {
            val groupApi = getApi(context)
            val response = groupApi.getGroups("")
            if (response.isSuccessful) {
                val groups = response.body()?.data
                if (groups != null) Result.success(groups)
                else Result.failure(Exception("No groups data in response"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupService", "Get groups error", e)
            Result.failure(e)
        }
    }

    suspend fun getGroupById(context: Context, groupId: String): Result<Group> {
        return try {
            val groupApi = getApi(context)
            val response = groupApi.getGroupById("", groupId)
            if (response.isSuccessful) {
                val group = response.body()?.data
                if (group != null) Result.success(group)
                else Result.failure(Exception("No group data in response"))
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("GroupService", "Get group by id error", e)
            Result.failure(e)
        }
    }
}
