package com.example.panchayat.data.remote.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for Supabase GoTrue Auth API endpoints.
 */
interface SupabaseAuthApi {

    /**
     * Registers a new user with email and password in Supabase Auth.
     */
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<AuthResponse>

    /**
     * Authenticates an existing user with email and password.
     */
    @POST("auth/v1/token?grant_type=password")
    suspend fun signInWithPassword(
        @Body request: SignInRequest
    ): Response<AuthResponse>

    /**
     * Signs out the user and invalidates the session JWT.
     */
    @POST("auth/v1/logout")
    suspend fun signOut(
        @Header("Authorization") authorization: String
    ): Response<Unit>

    /**
     * Retrieves the current authenticated user's details.
     */
    @GET("auth/v1/user")
    suspend fun getUser(
        @Header("Authorization") authorization: String
    ): Response<SupabaseUserDto>
}
