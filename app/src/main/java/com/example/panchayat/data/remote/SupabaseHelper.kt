package com.example.panchayat.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.model.UserProfile
import com.example.panchayat.data.remote.auth.SupabaseAuthClient
import com.example.panchayat.data.remote.realtime.RealtimeChannel
import com.example.panchayat.data.remote.realtime.RealtimeStatus
import com.example.panchayat.data.remote.realtime.SupabaseRealtimeClient
import kotlinx.coroutines.flow.StateFlow

/**
 * A helper object for accessing the Supabase client throughout the application.
 *
 * Initializes the [SupabaseClient] using the project URL and API key from environment
 * variables (checking [System.getenv], [System.getProperty], and falling back to [BuildConfig]
 * generated from the .env configuration file).
 *
 * Provides convenient, thread-safe, singleton access to:
 * - [client]: The configured [SupabaseClient] instance
 * - [api]: The PostgREST REST API service ([SupabaseApi])
 * - [auth]: The GoTrue authentication client ([SupabaseAuthClient])
 * - [realtime]: The WebSocket real-time subscription client ([SupabaseRealtimeClient])
 * - Configuration status, credentials inspection, and connectivity checks.
 */
open class SupabaseHelper {

    companion object {
        private const val TAG = "SupabaseHelper"

        /**
         * Resolves the Supabase URL from environment variables, system properties, or BuildConfig.
         */
        fun resolveUrl(): String {
            return System.getenv("SUPABASE_URL")?.takeIf { it.isNotBlank() }
                ?: System.getProperty("SUPABASE_URL")?.takeIf { it.isNotBlank() }
                ?: BuildConfig.SUPABASE_URL.trim()
        }

        /**
         * Resolves the Supabase API / anon key from environment variables, system properties, or BuildConfig.
         */
        fun resolveApiKey(): String {
            return System.getenv("SUPABASE_ANON_KEY")?.takeIf { it.isNotBlank() }
                ?: System.getenv("SUPABASE_API_KEY")?.takeIf { it.isNotBlank() }
                ?: System.getProperty("SUPABASE_ANON_KEY")?.takeIf { it.isNotBlank() }
                ?: System.getProperty("SUPABASE_API_KEY")?.takeIf { it.isNotBlank() }
                ?: BuildConfig.SUPABASE_ANON_KEY.trim()
        }

        @Volatile
        private var configuredUrl: String = resolveUrl()

        @Volatile
        private var configuredApiKey: String = resolveApiKey()

        @Volatile
        private var clientInstance: SupabaseClient? = null

        /**
         * Global singleton instance of [SupabaseClient].
         * Initialized lazily on first access using environment variables.
         */
        val client: SupabaseClient
            get() = clientInstance ?: synchronized(this) {
                clientInstance ?: SupabaseClient(configuredUrl, configuredApiKey).also {
                    clientInstance = it
                }
            }

        /**
         * Alias for [client] returning the active [SupabaseClient] instance.
         */
        @JvmStatic
        fun getInstance(): SupabaseClient = client

        /**
         * Configuration object associated with the active client.
         */
        val config: SupabaseConfig
            get() = SupabaseConfig(configuredUrl, configuredApiKey)

        /**
         * The active Supabase project URL.
         */
        val url: String
            get() = client.supabaseUrl

        /**
         * The active Supabase API / anon key.
         */
        val apiKey: String
            get() = client.supabaseAnonKey

        /**
         * Alias for [apiKey].
         */
        val anonKey: String
            get() = apiKey

        /**
         * Indicates whether the Supabase client is configured with valid, non-placeholder credentials.
         */
        val isConfigured: Boolean
            get() = client.isConfigured

        /**
         * PostgREST REST API service for querying tables (e.g. `posts`, `profiles`).
         */
        val api: SupabaseApi
            get() = client.api

        /**
         * GoTrue Authentication client for user sign up, sign in, and session handling.
         */
        val auth: SupabaseAuthClient
            get() = client.auth

        /**
         * Realtime WebSocket client for live subscriptions.
         */
        val realtime: SupabaseRealtimeClient
            get() = client.realtime

        /**
         * Observable connection state of the Realtime WebSocket client.
         */
        val realtimeStatus: StateFlow<RealtimeStatus>
            get() = client.realtimeStatus

        /**
         * Initializes the Supabase client with application context and optional custom credentials.
         */
        @JvmStatic
        @JvmOverloads
        fun initialize(
            context: Context? = null,
            supabaseUrl: String = resolveUrl(),
            supabaseAnonKey: String = resolveApiKey(),
            enableRealtime: Boolean = true
        ): SupabaseClient {
            synchronized(this) {
                configuredUrl = supabaseUrl.trim()
                configuredApiKey = supabaseAnonKey.trim()
                val newClient = SupabaseClient.initialize(
                    context = context,
                    supabaseUrl = configuredUrl,
                    supabaseAnonKey = configuredApiKey,
                    enableRealtime = enableRealtime
                )
                clientInstance = newClient
                Log.i(TAG, "SupabaseHelper initialized: url=${newClient.supabaseUrl}, isConfigured=${newClient.isConfigured}")
                return newClient
            }
        }

        /**
         * Reconfigures the Supabase client at runtime with custom URL and API key.
         */
        @JvmStatic
        fun configure(supabaseUrl: String, supabaseAnonKey: String): SupabaseClient {
            synchronized(this) {
                configuredUrl = supabaseUrl.trim()
                configuredApiKey = supabaseAnonKey.trim()
                val newClient = SupabaseClient(configuredUrl, configuredApiKey)
                clientInstance = newClient
                return newClient
            }
        }

        /**
         * Tests database connectivity to the Supabase PostgREST backend.
         */
        suspend fun testConnection(): Result<String> = client.testConnection()

        /**
         * Tests database connectivity to the Supabase PostgREST backend.
         */
        suspend fun testDatabaseConnectivity(): Result<String> = client.testDatabaseConnectivity()

        /**
         * Convenience method to fetch posts from the Supabase backend.
         */
        suspend fun fetchPosts(limit: Int = 50): Result<List<Post>> = client.fetchPosts(limit)

        /**
         * Convenience method to insert a post into the Supabase backend.
         */
        suspend fun insertPost(post: Post): Result<Post> = client.insertPost(post)

        /**
         * Convenience method to create a post using DTO in the Supabase backend.
         */
        suspend fun createPost(post: SupabasePostDto) = client.createPost(post)

        /**
         * Convenience method to like a post.
         */
        suspend fun likePost(postId: String, likesCount: Int = 1): Result<Int> =
            client.likePost(postId, likesCount)

        /**
         * Convenience method to toggle like state and update likes count in Supabase table.
         */
        suspend fun togglePostLike(
            postId: String,
            currentLikesCount: Int,
            currentlyLiked: Boolean
        ): Result<Pair<Boolean, Int>> =
            client.togglePostLike(postId, currentLikesCount, currentlyLiked)

        /**
         * Convenience method to repost.
         */
        suspend fun repost(postId: String, repostsCount: Int = 1): Result<Int> =
            client.repost(postId, repostsCount)

        /**
         * Convenience method to fetch user profile from Supabase.
         */
        suspend fun fetchUserProfile(userId: String? = null): Result<UserProfile?> =
            client.fetchUserProfile(userId)

        /**
         * Convenience method to upsert user profile in Supabase.
         */
        suspend fun upsertUserProfile(profile: UserProfile): Result<UserProfile> =
            client.upsertUserProfile(profile)

        /**
         * Convenience method to fetch past microblog posts of a user from Supabase.
         */
        suspend fun fetchUserPosts(
            authorId: String? = null,
            authorHandle: String? = null,
            limit: Int = 50
        ): Result<List<Post>> = client.fetchUserPosts(authorId, authorHandle, limit)

        /**
         * Connects to Supabase Realtime for live updates.
         */
        fun connectRealtime(): SupabaseRealtimeClient = client.connectRealtime()

        /**
         * Disconnects from Supabase Realtime.
         */
        fun disconnectRealtime() = client.disconnectRealtime()

        /**
         * Subscribes to real-time events on the 'posts' table.
         */
        fun subscribeToPosts(
            onPostEvent: (table: String, eventType: String, payloadJson: String) -> Unit
        ): RealtimeChannel = client.subscribeToPosts(onPostEvent)
    }
}

/**
 * Typealias providing alternative naming for SupabaseClientHelper.
 */
typealias SupabaseClientHelper = SupabaseHelper
