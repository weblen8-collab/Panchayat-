package com.example.panchayat.data.remote

import com.example.panchayat.data.model.Post
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    @GET("rest/v1/posts?select=*&order=created_at.desc")
    suspend fun getPosts(
        @Query("limit") limit: Int = 50
    ): Response<List<SupabasePostDto>>

    @GET("rest/v1/posts?select=*&order=created_at.desc")
    suspend fun getPostModels(
        @Query("limit") limit: Int = 50
    ): Response<List<Post>>

    @POST("rest/v1/posts")
    @Headers("Prefer: return=representation")
    suspend fun createPost(
        @Body post: SupabasePostDto
    ): Response<List<SupabasePostDto>>

    @POST("rest/v1/posts")
    @Headers("Prefer: return=representation")
    suspend fun insertPost(
        @Body post: Post
    ): Response<List<Post>>

    @PATCH("rest/v1/posts")
    @Headers("Prefer: return=representation")
    suspend fun updatePostLikes(
        @Query("id") idFilter: String,
        @Body body: SupabaseUpdateLikesDto
    ): Response<List<SupabasePostDto>>

    @GET("rest/v1/profiles?select=*")
    suspend fun getProfiles(): Response<List<SupabaseProfileDto>>

    @POST("rest/v1/profiles")
    @Headers("Prefer: return=representation")
    suspend fun upsertProfile(
        @Body profile: SupabaseProfileDto
    ): Response<List<SupabaseProfileDto>>

    @GET("rest/v1/")
    suspend fun checkHealth(): Response<Map<String, Any>>
}
