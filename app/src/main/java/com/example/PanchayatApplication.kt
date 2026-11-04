package com.example

import android.app.Application
import android.util.Log
import com.example.panchayat.data.remote.SupabaseClient

/**
 * Base Application class for the Panchayat app.
 * Initializes the Supabase client during application startup to enable
 * database connectivity and real-time features using the provided
 * SUPABASE_URL and SUPABASE_ANON_KEY.
 */
class PanchayatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Initializing PanchayatApplication...")

        try {
            // Initialize the Supabase client using URL and API key from environment variables
            // via SupabaseHelper to enable database connectivity and real-time features.
            val client = SupabaseHelper.initialize(
                context = this,
                supabaseUrl = SupabaseHelper.resolveUrl(),
                supabaseAnonKey = SupabaseHelper.resolveApiKey(),
                enableRealtime = true
            )

            Log.i(TAG, "Supabase client initialized successfully at ${client.supabaseUrl} (realtime: ${client.realtimeUrl})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Supabase client: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "PanchayatApplication"
    }
}
