package com.example.panchayat.data.remote.auth

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import retrofit2.Response

/**
 * Client for managing Supabase Authentication (Sign Up, Sign In, Sign Out, Session state).
 */
class SupabaseAuthClient(
    private val authApi: SupabaseAuthApi,
    private val isConfigured: Boolean
) {
    private val _currentSession = MutableStateFlow<SupabaseSession?>(null)
    val currentSession: StateFlow<SupabaseSession?> = _currentSession.asStateFlow()

    private val _currentUser = MutableStateFlow<SupabaseUserDto?>(null)
    val currentUser: StateFlow<SupabaseUserDto?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val isLoggedIn: Boolean
        get() = _currentSession.value != null

    val currentAccessToken: String?
        get() = _currentSession.value?.accessToken

    /**
     * Registers a new user with email and password.
     * Optionally takes [name] and [handle] which are stored in Supabase user_metadata.
     */
    suspend fun signUp(
        email: String,
        password: String,
        name: String = "",
        handle: String = ""
    ): Result<AuthResponse> {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        if (trimmedPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        _authState.value = AuthState.Loading

        return try {
            val metadata = mutableMapOf<String, String>()
            if (name.isNotBlank()) metadata["name"] = name.trim()
            if (handle.isNotBlank()) metadata["handle"] = handle.trim().removePrefix("@")

            val request = SignUpRequest(
                email = trimmedEmail,
                password = trimmedPassword,
                data = if (metadata.isNotEmpty()) metadata else null
            )

            val response = authApi.signUp(request)
            handleAuthResponse(response)
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Sign up failed", e)
            val errorMsg = e.localizedMessage ?: "Failed to connect to Supabase Auth"
            _authState.value = AuthState.Error(errorMsg)
            Result.failure(e)
        }
    }

    /**
     * Signs in an existing user using email and password.
     */
    suspend fun signIn(
        email: String,
        password: String
    ): Result<AuthResponse> {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required"))
        }

        _authState.value = AuthState.Loading

        return try {
            val request = SignInRequest(email = trimmedEmail, password = trimmedPassword)
            val response = authApi.signInWithPassword(request)
            handleAuthResponse(response)
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Sign in failed", e)
            val errorMsg = e.localizedMessage ?: "Failed to connect to Supabase Auth"
            _authState.value = AuthState.Error(errorMsg)
            Result.failure(e)
        }
    }

    /**
     * Signs out the current user and clears session tokens.
     */
    suspend fun signOut(): Result<Unit> {
        val token = currentAccessToken
        if (token != null) {
            try {
                authApi.signOut("Bearer $token")
            } catch (e: Exception) {
                Log.w("SupabaseAuth", "Sign out remote call warning", e)
            }
        }
        _currentSession.value = null
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }

    /**
     * Parses and updates session/state from an auth response.
     */
    private fun handleAuthResponse(response: Response<AuthResponse>): Result<AuthResponse> {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.user != null) {
                val token = body.accessToken ?: ""
                val session = SupabaseSession(
                    accessToken = token,
                    refreshToken = body.refreshToken,
                    user = body.user
                )
                _currentSession.value = session
                _currentUser.value = body.user
                val profile = body.user.toUserProfile()
                _authState.value = AuthState.Authenticated(session, profile)
                return Result.success(body)
            } else if (body != null && body.displayErrorMessage != null) {
                val errorMsg = body.displayErrorMessage ?: "Authentication error"
                _authState.value = AuthState.Error(errorMsg)
                return Result.failure(Exception(errorMsg))
            }
        }

        // Try extracting error message from error body
        val errorBodyStr = response.errorBody()?.string()
        val parsedMessage = parseErrorBody(errorBodyStr) ?: "Authentication failed (${response.code()})"
        _authState.value = AuthState.Error(parsedMessage)
        return Result.failure(Exception(parsedMessage))
    }

    private fun parseErrorBody(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = JSONObject(errorBody)
            when {
                json.has("msg") -> json.getString("msg")
                json.has("error_description") -> json.getString("error_description")
                json.has("message") -> json.getString("message")
                json.has("error") -> json.getString("error")
                else -> errorBody
            }
        } catch (_: Exception) {
            errorBody
        }
    }
}
