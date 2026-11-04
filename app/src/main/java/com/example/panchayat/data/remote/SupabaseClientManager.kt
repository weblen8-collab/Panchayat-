package com.example.panchayat.data.remote

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

sealed class SupabaseStatus {
    object LocalOnly : SupabaseStatus()
    object Connecting : SupabaseStatus()
    data class Connected(val projectUrl: String) : SupabaseStatus()
    data class Error(val message: String) : SupabaseStatus()
}

class SupabaseClientManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("panchayat_supabase_prefs", Context.MODE_PRIVATE)

    private val _status = MutableStateFlow<SupabaseStatus>(SupabaseStatus.LocalOnly)
    val status: StateFlow<SupabaseStatus> = _status.asStateFlow()

    private var currentUrl: String = prefs.getString("supabase_url", null)
        ?: BuildConfig.SUPABASE_URL.ifEmpty { "https://sample-panchayat.supabase.co" }

    private var currentAnonKey: String = prefs.getString("supabase_anon_key", null)
        ?: BuildConfig.SUPABASE_ANON_KEY.ifEmpty { "sample_anon_key" }

    private var activeApi: SupabaseApi? = null

    init {
        rebuildClient()
    }

    fun getConfig(): Pair<String, String> = Pair(currentUrl, currentAnonKey)

    fun updateConfig(url: String, anonKey: String) {
        val normUrl = SupabaseClient.normalizeUrl(url, anonKey).removeSuffix("/")
        val normKey = SupabaseClient.normalizeKey(anonKey, url)
        currentUrl = normUrl
        currentAnonKey = normKey
        prefs.edit()
            .putString("supabase_url", currentUrl)
            .putString("supabase_anon_key", currentAnonKey)
            .apply()
        rebuildClient()
    }

    private fun rebuildClient() {
        val trimmedUrl = currentUrl.trim()
        val trimmedKey = currentAnonKey.trim()

        // Check if user accidentally swapped key into the URL field
        if (trimmedUrl.startsWith("sb_", ignoreCase = true) || trimmedUrl.startsWith("ey", ignoreCase = true)) {
            if (trimmedKey.startsWith("http://", ignoreCase = true) ||
                trimmedKey.startsWith("https://", ignoreCase = true) ||
                trimmedKey.contains(".supabase.co", ignoreCase = true)) {
                currentUrl = SupabaseClient.normalizeUrl(trimmedKey, "").removeSuffix("/")
                currentAnonKey = trimmedUrl
            } else {
                _status.value = SupabaseStatus.Error(
                    "Invalid Supabase Project URL: The URL was provided as an API key ('${trimmedUrl.take(8)}...'). Please enter your project URL (e.g., https://<project-ref>.supabase.co)."
                )
                activeApi = null
                return
            }
        }

        val normalizedUrl = SupabaseClient.normalizeUrl(currentUrl, currentAnonKey)
        val normalizedKey = SupabaseClient.normalizeKey(currentAnonKey, currentUrl)

        if (normalizedUrl == SupabaseClient.DEFAULT_PLACEHOLDER_URL ||
            normalizedUrl.contains("placeholder.supabase.co") ||
            normalizedUrl.contains("sample-panchayat") ||
            normalizedKey.isBlank() ||
            normalizedKey.contains("sample_anon_key")) {
            _status.value = SupabaseStatus.LocalOnly
            activeApi = null
            return
        }

        try {
            val client = SupabaseClient(normalizedUrl, normalizedKey)
            activeApi = client.api
            _status.value = SupabaseStatus.Connected(normalizedUrl.removeSuffix("/"))
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Failed to build Supabase client", e)
            _status.value = SupabaseStatus.Error(e.localizedMessage ?: "Invalid configuration")
            activeApi = null
        }
    }

    fun getApi(): SupabaseApi? = activeApi

    suspend fun testConnection(): Result<String> {
        _status.value = SupabaseStatus.Connecting
        if (currentUrl.isBlank() || currentUrl.contains("sample-panchayat")) {
            _status.value = SupabaseStatus.LocalOnly
            return Result.failure(Exception("Please enter a valid Supabase project URL & API key."))
        }

        val api = activeApi
        if (api == null) {
            _status.value = SupabaseStatus.Error("Client not initialized")
            return Result.failure(Exception("Client not initialized"))
        }

        return try {
            val response = api.getPosts(limit = 1)
            if (response.isSuccessful) {
                _status.value = SupabaseStatus.Connected(currentUrl)
                Result.success("Connected successfully to Supabase!")
            } else {
                val errorMsg = "Supabase responded with code ${response.code()}: ${response.message()}"
                _status.value = SupabaseStatus.Error(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Connection timed out"
            _status.value = SupabaseStatus.Error(msg)
            Result.failure(e)
        }
    }

    companion object {
        const val SQL_SCHEMA = """
-- Run this SQL in your Supabase SQL Editor:

CREATE TABLE IF NOT EXISTS posts (
  id TEXT PRIMARY KEY,
  author_id TEXT NOT NULL,
  author_name TEXT NOT NULL,
  author_handle TEXT NOT NULL,
  author_avatar_url TEXT,
  is_verified BOOLEAN DEFAULT FALSE,
  content TEXT NOT NULL,
  category TEXT DEFAULT 'General',
  media_url TEXT,
  likes_count INT DEFAULT 0,
  reposts_count INT DEFAULT 0,
  replies_count INT DEFAULT 0,
  views_count INT DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- Enable Row Level Security (RLS) or public read/insert for testing:
ALTER TABLE posts ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public Read" ON posts FOR SELECT USING (true);
CREATE POLICY "Public Insert" ON posts FOR INSERT WITH CHECK (true);
CREATE POLICY "Public Update" ON posts FOR UPDATE USING (true);
"""
    }
}
