package com.example

import com.example.panchayat.data.remote.SupabaseClient
import com.example.panchayat.data.remote.SupabaseConfig
import com.example.panchayat.data.remote.SupabaseHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SupabaseHelper] and Supabase client configuration.
 *
 * Verifies that the client initializes using environment variables, provides
 * singleton access throughout the application, and exposes all essential services.
 */
class SupabaseHelperTest {

    @Test
    fun testSupabaseHelper_initializesWithEnvironmentVariables() {
        val client = SupabaseHelper.client
        assertNotNull("Supabase client should not be null", client)
        assertEquals("getInstance() should return the singleton client", client, SupabaseHelper.getInstance())

        assertNotNull("URL should be resolved", SupabaseHelper.url)
        assertTrue("URL should not be empty", SupabaseHelper.url.isNotBlank())

        assertNotNull("API key should be resolved", SupabaseHelper.apiKey)
        assertTrue("API key should not be empty", SupabaseHelper.apiKey.isNotBlank())
        assertEquals("anonKey should match apiKey", SupabaseHelper.apiKey, SupabaseHelper.anonKey)

        assertNotNull("REST API service should be accessible", SupabaseHelper.api)
        assertNotNull("GoTrue Auth client should be accessible", SupabaseHelper.auth)
        assertNotNull("Realtime client should be accessible", SupabaseHelper.realtime)
        assertNotNull("Realtime status StateFlow should be accessible", SupabaseHelper.realtimeStatus)
        assertNotNull("Config object should be accessible", SupabaseHelper.config)
    }

    @Test
    fun testSupabaseHelper_configure_updatesClientWithCustomCredentials() {
        val customUrl = "https://panchayat-custom-test.supabase.co"
        val customKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.custom_test_key_123"

        val customClient = SupabaseHelper.configure(
            supabaseUrl = customUrl,
            supabaseAnonKey = customKey
        )

        assertNotNull("Configured client should not be null", customClient)
        assertEquals(customUrl, SupabaseHelper.url)
        assertEquals(customKey, SupabaseHelper.apiKey)
        assertEquals(customKey, SupabaseHelper.anonKey)
        assertEquals(customUrl, SupabaseHelper.client.supabaseUrl)
        assertEquals(customKey, SupabaseHelper.client.supabaseAnonKey)
        assertTrue("Valid non-placeholder credentials should be configured", SupabaseHelper.isConfigured)
    }

    @Test
    fun testSupabaseHelper_rootPackageAccess_andTypealias() {
        // Accessing through root package typealiases
        val rootClient = com.example.SupabaseHelper.client
        assertNotNull(rootClient)
        assertEquals(SupabaseHelper.client, rootClient)

        val clientFromHelper = com.example.SupabaseClientHelper.getInstance()
        assertNotNull(clientFromHelper)
        assertEquals(SupabaseHelper.client, clientFromHelper)
    }

    @Test
    fun testSupabaseConfig_andSupabaseClient_exposeHelper() {
        assertNotNull("SupabaseConfig companion should expose helper", SupabaseConfig.helper)
        assertNotNull("SupabaseClient companion should expose helper", SupabaseClient.helper)
        assertNotNull("SupabaseConfig instance should expose helper", SupabaseConfig().helper)

        assertEquals(
            "Helper accessed via SupabaseConfig should return the same client",
            SupabaseHelper.client,
            SupabaseConfig.helper.client
        )
        assertEquals(
            "Helper accessed via SupabaseClient should return the same client",
            SupabaseHelper.client,
            SupabaseClient.helper.client
        )
    }

    @Test
    fun testSupabaseHelper_resolveEnvironmentVariables() {
        val resolvedUrl = SupabaseHelper.resolveUrl()
        val resolvedKey = SupabaseHelper.resolveApiKey()

        assertNotNull("Resolved URL should not be null", resolvedUrl)
        assertTrue("Resolved URL should not be blank", resolvedUrl.isNotBlank())

        assertNotNull("Resolved API key should not be null", resolvedKey)
        assertTrue("Resolved API key should not be blank", resolvedKey.isNotBlank())
    }

    @Test
    fun testSupabaseHelper_initialize_withCustomParameters() {
        val testUrl = "https://panchayat-village.supabase.co"
        val testKey = "village_anon_key_456"

        val initializedClient = SupabaseHelper.initialize(
            context = null,
            supabaseUrl = testUrl,
            supabaseAnonKey = testKey,
            enableRealtime = false
        )

        assertNotNull(initializedClient)
        assertEquals(testUrl, SupabaseHelper.url)
        assertEquals(testKey, SupabaseHelper.apiKey)
        assertEquals(initializedClient, SupabaseHelper.client)
    }
}
