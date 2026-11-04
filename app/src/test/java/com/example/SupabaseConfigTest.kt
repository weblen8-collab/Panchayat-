package com.example

import com.example.panchayat.data.remote.SupabaseConfig
import com.example.panchayat.data.remote.SupabaseClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseConfigTest {

    @Test
    fun testSupabaseConfig_initializesWithDefaults() {
        val config = SupabaseConfig.default
        assertNotNull(config)
        assertNotNull(config.url)
        assertNotNull(config.supabaseUrl)
        assertNotNull(config.anonKey)
        assertNotNull(config.supabaseAnonKey)
        assertNotNull(config.apiKey)
        assertNotNull(config.client)
        assertNotNull(config.api)
        assertNotNull(config.auth)
        assertTrue(config.formattedBaseUrl.endsWith("/"))
    }

    @Test
    fun testSupabaseConfig_customInstance_connectsWithProvidedCredentials() {
        val testUrl = "https://panchayat-test-project.supabase.co"
        val testKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test_anon_key_payload"

        val config = SupabaseConfig(
            supabaseUrl = testUrl,
            supabaseAnonKey = testKey
        )

        assertEquals(testUrl, config.url)
        assertEquals(testUrl, config.supabaseUrl)
        assertEquals(testKey, config.anonKey)
        assertEquals(testKey, config.supabaseAnonKey)
        assertEquals(testKey, config.apiKey)
        assertEquals(testKey, config.key)
        assertEquals("https://panchayat-test-project.supabase.co/", config.formattedBaseUrl)
        assertTrue(config.isConfigured)

        val client = config.client
        assertNotNull(client)
        assertEquals(testUrl, client.supabaseUrl)
        assertEquals(testKey, client.supabaseAnonKey)
        assertNotNull(client.api)
        assertNotNull(client.auth)
    }

    @Test
    fun testSupabaseConfig_companionObject_connectAndConfigure() {
        val testUrl = "https://village-panchayat-cloud.supabase.co"
        val testKey = "anon_secret_key_abc_123"

        val connectedClient = SupabaseConfig.connect(testUrl, testKey)
        assertNotNull(connectedClient)
        assertEquals(testUrl, connectedClient.supabaseUrl)
        assertEquals(testKey, connectedClient.supabaseAnonKey)
        assertEquals(testUrl, SupabaseConfig.url)
        assertEquals(testKey, SupabaseConfig.anonKey)
        assertEquals(testUrl, SupabaseConfig.supabaseUrl)
        assertEquals(testKey, SupabaseConfig.supabaseAnonKey)
        assertEquals(connectedClient, SupabaseConfig.client)

        val configInstance = SupabaseConfig(testUrl, testKey)
        val createdClient = configInstance.createClient()
        assertNotNull(createdClient)
        assertEquals(testUrl, createdClient.supabaseUrl)
        assertEquals(testKey, createdClient.supabaseAnonKey)
    }

    @Test
    fun testSupabaseConfig_rootTypealias_andClientConfig() {
        val testUrl = "https://root-test.supabase.co"
        val testKey = "root_anon_key_999"

        val rootConfig: com.example.SupabaseConfig = com.example.SupabaseConfig(testUrl, testKey)
        assertEquals(testUrl, rootConfig.url)
        assertEquals(testKey, rootConfig.anonKey)
        assertNotNull(rootConfig.client)

        val clientConfig: com.example.SupabaseClientConfig = com.example.SupabaseClientConfig(testUrl, testKey)
        assertEquals(testUrl, clientConfig.supabaseUrl)
        assertEquals(testKey, clientConfig.supabaseAnonKey)
    }

    @Test
    fun testSupabaseClient_exposesConfigObject() {
        val client = SupabaseClient(
            supabaseUrl = "https://phulera-panchayat.supabase.co",
            supabaseAnonKey = "phulera_anon_key"
        )
        val config = client.config
        assertNotNull(config)
        assertEquals("https://phulera-panchayat.supabase.co", config.supabaseUrl)
        assertEquals("phulera_anon_key", config.supabaseAnonKey)
        assertEquals("https://phulera-panchayat.supabase.co", config.url)
        assertEquals("phulera_anon_key", config.anonKey)

        assertNotNull(SupabaseClient.config)
    }

    @Test
    fun testSupabaseConfig_isConfiguredLogic() {
        val sampleConfig = SupabaseConfig(
            supabaseUrl = "https://sample-panchayat.supabase.co",
            supabaseAnonKey = "sample_anon_key"
        )
        assertFalse(sampleConfig.isConfigured)

        val emptyConfig = SupabaseConfig(
            supabaseUrl = "",
            supabaseAnonKey = ""
        )
        assertFalse(emptyConfig.isConfigured)

        val validConfig = SupabaseConfig(
            supabaseUrl = "https://actual-app.supabase.co",
            supabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid_anon_token"
        )
        assertTrue(validConfig.isConfigured)
    }

    @Test
    fun testSupabaseConfig_initialize_andRealtimeProperties() {
        val testUrl = "https://panchayat-realtime.supabase.co"
        val testKey = "anon_key_for_test"

        val client = SupabaseConfig.initialize(
            supabaseUrl = testUrl,
            supabaseAnonKey = testKey,
            enableRealtime = false
        )
        assertNotNull(client)
        assertEquals(testUrl, SupabaseConfig.url)
        assertEquals(testKey, SupabaseConfig.anonKey)
        assertNotNull(SupabaseConfig.realtime)
        assertTrue(SupabaseConfig.realtimeUrl.contains("panchayat-realtime.supabase.co"))
        assertNotNull(SupabaseConfig.realtimeStatus)
    }
}
