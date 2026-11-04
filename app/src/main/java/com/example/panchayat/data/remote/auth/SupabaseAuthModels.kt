package com.example.panchayat.data.remote.auth

import com.example.panchayat.data.model.UserProfile
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignUpRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String,

    @Json(name = "data")
    val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SignInRequest(
    @Json(name = "email")
    val email: String,

    @Json(name = "password")
    val password: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "access_token")
    val accessToken: String? = null,

    @Json(name = "token_type")
    val tokenType: String? = null,

    @Json(name = "expires_in")
    val expiresIn: Long? = null,

    @Json(name = "refresh_token")
    val refreshToken: String? = null,

    @Json(name = "user")
    val user: SupabaseUserDto? = null,

    @Json(name = "error")
    val error: String? = null,

    @Json(name = "error_description")
    val errorDescription: String? = null,

    @Json(name = "msg")
    val msg: String? = null
) {
    val isSuccess: Boolean
        get() = accessToken != null || user != null

    val displayErrorMessage: String?
        get() = errorDescription ?: msg ?: error
}

@JsonClass(generateAdapter = true)
data class SupabaseUserDto(
    @Json(name = "id")
    val id: String,

    @Json(name = "email")
    val email: String? = null,

    @Json(name = "role")
    val role: String? = null,

    @Json(name = "aud")
    val aud: String? = null,

    @Json(name = "user_metadata")
    val userMetadata: Map<String, String>? = null,

    @Json(name = "created_at")
    val createdAt: String? = null
) {
    val displayName: String
        get() = userMetadata?.get("name")
            ?: email?.substringBefore("@")
            ?: "Panchayat Member"

    val displayHandle: String
        get() = userMetadata?.get("handle")
            ?: email?.substringBefore("@")?.replace(".", "_")?.lowercase()
            ?: "user_${id.take(6)}"

    fun toUserProfile(): UserProfile {
        return UserProfile(
            id = id,
            email = email,
            name = displayName,
            handle = displayHandle,
            joinedDate = "Joined recently",
            createdAt = createdAt
        )
    }
}

data class SupabaseSession(
    val accessToken: String,
    val refreshToken: String?,
    val user: SupabaseUserDto
)

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val session: SupabaseSession, val userProfile: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}
