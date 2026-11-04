package com.example.panchayat.data.remote

import android.content.Context
import com.example.BuildConfig
import com.example.panchayat.data.remote.auth.SupabaseAuthClient
import com.example.panchayat.data.remote.realtime.RealtimeStatus
import com.example.panchayat.data.remote.realtime.SupabaseRealtimeClient
import kotlinx.coroutines.flow.StateFlow

/**
 * Supabase client configuration object to connect with the provided Supabase URL and anon key.
 *
 * Provides configuration properties, credentials management, and factory methods
 * to instantiate and configure [SupabaseClient] for backend communication.
 *
 * @property supabaseUrl The Supabase project base URL (e.g., https://xyz.supabase.co)
 * @property supabaseAnonKey The public anonymous API key (anon key) for authenticating requests
 */
open class SupabaseConfig(
    supabaseUrl: String = resolveEnvironmentUrl(),
    supabaseAnonKey: String = resolveEnvironmentApiKey()
) {
    /**
     * Helper object for accessing the Supabase client throughout the application.
     */
    val helper: SupabaseHelper.Companion
        get() = SupabaseHelper

    val rawSupabaseUrl: String = supabaseUrl.trim()
    val rawSupabaseAnonKey: String = supabaseAnonKey.trim()

    val supabaseUrl: String = SupabaseClient.normalizeUrl(rawSupabaseUrl, rawSupabaseAnonKey)
    val supabaseAnonKey: String = SupabaseClient.normalizeKey(rawSupabaseAnonKey, rawSupabaseUrl)

    /**
     * Alias for supabaseUrl.
     */
    val url: String
        get() = supabaseUrl

    /**
     * Alias for supabaseAnonKey.
     */
    val anonKey: String
        get() = supabaseAnonKey

    /**
     * Alias for supabaseAnonKey (API Key).
     */
    val apiKey: String
        get() = supabaseAnonKey

    /**
     * Alias for supabaseAnonKey (Key).
     */
    val key: String
        get() = supabaseAnonKey

    /**
     * Formatted base URL ending with a trailing slash for Retrofit / HTTP clients.
     */
    val formattedBaseUrl: String
        get() = if (supabaseUrl.endsWith("/")) supabaseUrl else "$supabaseUrl/"

    /**
     * Checks if URL and anon key are populated with valid non-sample values.
     */
    val isConfigured: Boolean
        get() = SupabaseClient.isConfigured(rawSupabaseUrl, rawSupabaseAnonKey)

    /**
     * Lazily initialized [SupabaseClient] using this configuration.
     */
    val client: SupabaseClient by lazy {
        SupabaseClient(supabaseUrl = supabaseUrl, supabaseAnonKey = supabaseAnonKey)
    }

    /**
     * Supabase PostgREST REST API service for this configuration.
     */
    val api: SupabaseApi
        get() = client.api

    /**
     * Supabase GoTrue Auth client for this configuration.
     */
    val auth: SupabaseAuthClient
        get() = client.auth

    /**
     * Creates a new [SupabaseClient] instance using this configuration.
     */
    fun createClient(): SupabaseClient =
        SupabaseClient(supabaseUrl = supabaseUrl, supabaseAnonKey = supabaseAnonKey)

    /**
     * Connects and returns the [SupabaseClient] instance.
     */
    fun connect(): SupabaseClient = client

    /**
     * Realtime WebSocket client for this configuration.
     */
    val realtime: SupabaseRealtimeClient
        get() = client.realtime

    /**
     * Realtime WebSocket endpoint URL for this configuration.
     */
    val realtimeUrl: String
        get() = client.realtimeUrl

    /**
     * Observable connection state of the Realtime WebSocket client.
     */
    val realtimeStatus: StateFlow<RealtimeStatus>
        get() = client.realtimeStatus

    /**
     * Connects to Supabase Realtime for this configuration.
     */
    fun connectRealtime(): SupabaseRealtimeClient = client.connectRealtime()

    /**
     * Disconnects from Supabase Realtime for this configuration.
     */
    fun disconnectRealtime() = client.disconnectRealtime()

    /**
     * Verifies connectivity to the Supabase backend.
     */
    suspend fun testConnection(): Result<String> = client.testConnection()

    /**
     * Verifies database connectivity to the Supabase PostgREST backend.
     */
    suspend fun testDatabaseConnectivity(): Result<String> = client.testConnection()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SupabaseConfig) return false
        return supabaseUrl == other.supabaseUrl && supabaseAnonKey == other.supabaseAnonKey
    }

    override fun hashCode(): Int {
        var result = supabaseUrl.hashCode()
        result = 31 * result + supabaseAnonKey.hashCode()
        return result
    }

    override fun toString(): String {
        val maskedKey = if (supabaseAnonKey.length > 8) {
            "${supabaseAnonKey.take(4)}...${supabaseAnonKey.takeLast(4)}"
        } else {
            "***"
        }
        return "SupabaseConfig(url='$supabaseUrl', anonKey='$maskedKey', isConfigured=$isConfigured)"
    }

    companion object {
        /**
         * Resolves the Supabase URL from environment variables, system properties, or BuildConfig.
         */
        @JvmStatic
        fun resolveEnvironmentUrl(): String {
            return System.getenv("SUPABASE_URL")?.takeIf { it.isNotBlank() }
                ?: System.getProperty("SUPABASE_URL")?.takeIf { it.isNotBlank() }
                ?: BuildConfig.SUPABASE_URL.trim()
        }

        /**
         * Resolves the Supabase API / anon key from environment variables, system properties, or BuildConfig.
         */
        @JvmStatic
        fun resolveEnvironmentApiKey(): String {
            return System.getenv("SUPABASE_ANON_KEY")?.takeIf { it.isNotBlank() }
                ?: System.getenv("SUPABASE_API_KEY")?.takeIf { it.isNotBlank() }
                ?: System.getProperty("SUPABASE_ANON_KEY")?.takeIf { it.isNotBlank() }
                ?: System.getProperty("SUPABASE_API_KEY")?.takeIf { it.isNotBlank() }
                ?: BuildConfig.SUPABASE_ANON_KEY.trim()
        }

        @Volatile
        private var currentUrl: String = resolveEnvironmentUrl()

        @Volatile
        private var currentAnonKey: String = resolveEnvironmentApiKey()

        @Volatile
        private var activeClient: SupabaseClient? = null

        /**
         * Helper object for accessing the Supabase client throughout the application.
         */
        val helper: SupabaseHelper.Companion
            get() = SupabaseHelper

        /**
         * Global configured Supabase URL.
         */
        var supabaseUrl: String
            get() = currentUrl
            set(value) {
                configure(value, currentAnonKey)
            }

        /**
         * Global configured Supabase Anon Key.
         */
        var supabaseAnonKey: String
            get() = currentAnonKey
            set(value) {
                configure(currentUrl, value)
            }

        /**
         * Global configured URL alias.
         */
        var url: String
            get() = currentUrl
            set(value) {
                supabaseUrl = value
            }

        /**
         * Global configured anon key alias.
         */
        var anonKey: String
            get() = currentAnonKey
            set(value) {
                supabaseAnonKey = value
            }

        /**
         * Global configured API key alias.
         */
        var apiKey: String
            get() = currentAnonKey
            set(value) {
                supabaseAnonKey = value
            }

        /**
         * Global configured key alias.
         */
        var key: String
            get() = currentAnonKey
            set(value) {
                supabaseAnonKey = value
            }

        /**
         * Returns whether the global Supabase configuration is valid.
         */
        val isConfigured: Boolean
            get() = currentUrl.isNotBlank() &&
                    !currentUrl.contains("sample-panchayat") &&
                    currentAnonKey.isNotBlank() &&
                    !currentAnonKey.contains("sample_anon_key")

        /**
         * Global singleton instance of [SupabaseConfig] with current settings.
         */
        val default: SupabaseConfig
            get() = SupabaseConfig(currentUrl, currentAnonKey)

        /**
         * Alias for [default].
         */
        val current: SupabaseConfig
            get() = default

        /**
         * Global connected [SupabaseClient] instance.
         */
        val client: SupabaseClient
            get() = activeClient ?: synchronized(this) {
                activeClient ?: SupabaseClient(currentUrl, currentAnonKey).also { activeClient = it }
            }

        /**
         * Global [SupabaseApi] service.
         */
        val api: SupabaseApi
            get() = client.api

        /**
         * Global [SupabaseAuthClient].
         */
        val auth: SupabaseAuthClient
            get() = client.auth

        /**
         * Realtime WebSocket client for current global configuration.
         */
        val realtime: SupabaseRealtimeClient
            get() = client.realtime

        /**
         * Realtime WebSocket URL for current global configuration.
         */
        val realtimeUrl: String
            get() = client.realtimeUrl

        /**
         * Observable connection state of the Realtime WebSocket client.
         */
        val realtimeStatus: StateFlow<RealtimeStatus>
            get() = client.realtimeStatus

        /**
         * Connects to Supabase Realtime using current global credentials.
         */
        fun connectRealtime(): SupabaseRealtimeClient = client.connectRealtime()

        /**
         * Disconnects from Supabase Realtime.
         */
        fun disconnectRealtime() = client.disconnectRealtime()

        /**
         * Initializes the Supabase client in the application to enable database connectivity
         * and real-time features using the provided SUPABASE_URL and SUPABASE_ANON_KEY.
         */
        @JvmStatic
        @JvmOverloads
        fun initialize(
            context: Context? = null,
            supabaseUrl: String = BuildConfig.SUPABASE_URL,
            supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY,
            enableRealtime: Boolean = true
        ): SupabaseClient {
            return SupabaseClient.initialize(context, supabaseUrl, supabaseAnonKey, enableRealtime)
        }

        /**
         * Verifies database connectivity to the Supabase PostgREST backend.
         */
        suspend fun testDatabaseConnectivity(): Result<String> = client.testDatabaseConnectivity()

        /**
         * Configures the global Supabase credentials and initializes a new [SupabaseClient].
         *
         * @param url The Supabase project URL
         * @param anonKey The Supabase anon key
         * @return Connected [SupabaseClient]
         */
        fun configure(url: String, anonKey: String): SupabaseClient {
            currentUrl = url.trim()
            currentAnonKey = anonKey.trim()
            val newClient = SupabaseClient(currentUrl, currentAnonKey)
            activeClient = newClient
            return newClient
        }

        /**
         * Connects with the provided Supabase URL and anon key.
         *
         * @param url The Supabase project URL
         * @param anonKey The Supabase anon key
         * @return Connected [SupabaseClient]
         */
        fun connect(url: String, anonKey: String): SupabaseClient {
            return configure(url, anonKey)
        }

        /**
         * Creates a new [SupabaseClient] instance without altering global configuration.
         */
        fun createClient(
            url: String = currentUrl,
            anonKey: String = currentAnonKey
        ): SupabaseClient = SupabaseClient(url, anonKey)

        /**
         * Creates a new [SupabaseConfig] configuration instance.
         */
        fun create(
            url: String = currentUrl,
            anonKey: String = currentAnonKey
        ): SupabaseConfig = SupabaseConfig(url, anonKey)

        /**
         * Returns the singleton configuration instance.
         */
        fun getInstance(): SupabaseConfig = default

        /**
         * Invocation operator allowing `SupabaseConfig("https://...", "anon_key")`
         * to produce a configured [SupabaseConfig].
         */
        operator fun invoke(
            url: String = currentUrl,
            anonKey: String = currentAnonKey
        ): SupabaseConfig = SupabaseConfig(url, anonKey)
    }
}

/**
 * Typealias providing alternative naming for SupabaseClientConfig.
 */
typealias SupabaseClientConfig = SupabaseConfig
