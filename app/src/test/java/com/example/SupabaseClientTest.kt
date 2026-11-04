package com.example

import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.panchayat.data.model.UserProfile
import com.example.panchayat.data.remote.SupabaseClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseClientTest {

    @Test
    fun supabaseClient_initializesWithEnvValues() {
        val client = SupabaseClient.default
        assertNotNull(client)
        assertNotNull(client.supabaseUrl)
        assertNotNull(client.supabaseAnonKey)
        assertNotNull(client.api)
        assertNotNull(client.authApi)
        assertNotNull(client.auth)
        assertNotNull(client.okHttpClient)
        assertTrue(client.formattedBaseUrl.endsWith("/"))
    }

    @Test
    fun supabaseClient_customInstance_formatsBaseUrl() {
        val customClient = SupabaseClient(
            supabaseUrl = "https://custom-project.supabase.co",
            supabaseAnonKey = "custom_secret_key"
        )
        assertTrue(customClient.formattedBaseUrl == "https://custom-project.supabase.co/")
        assertTrue(customClient.isConfigured)
        assertNotNull(customClient.auth)
    }

    @Test
    fun post_dataClass_initializesWithMetadata() {
        val post = Post(
            authorId = "user_123",
            content = "Exciting panchayat community update!",
            likesCount = 14,
            commentsCount = 3
        )
        assertNotNull(post.id)
        assertTrue(post.content == "Exciting panchayat community update!")
        assertTrue(post.authorId == "user_123")
        assertTrue(post.likesCount == 14)
        assertTrue(post.commentsCount == 3)
        assertTrue(post.timestamp > 0)
        assertTrue(Post.SUPABASE_POSTS_TABLE_SCHEMA.contains("CREATE TABLE IF NOT EXISTS posts"))
    }

    @Test
    fun userProfile_dataClass_initializesAndMapsToEntity() {
        val profile = UserProfile(
            id = "user_uuid_456",
            email = "sarpanch@panchayat.org",
            name = "Manju Devi",
            handle = "manju_devi",
            bio = "Pradhan of Gram Panchayat Phulera",
            location = "Phulera, Ballia",
            isVerified = true,
            followingCount = 12,
            followersCount = 1250
        )

        assertEquals("user_uuid_456", profile.id)
        assertEquals("sarpanch@panchayat.org", profile.email)
        assertEquals("Manju Devi", profile.name)
        assertEquals("manju_devi", profile.handle)
        assertTrue(profile.isVerified)

        val entity = profile.toEntity(isCurrentUser = true)
        assertEquals("user_uuid_456", entity.id)
        assertEquals("Manju Devi", entity.name)
        assertEquals("manju_devi", entity.handle)
        assertTrue(entity.isCurrentUser)

        val reconstructed = UserProfile.fromEntity(entity, email = "sarpanch@panchayat.org")
        assertEquals(profile.id, reconstructed.id)
        assertEquals(profile.email, reconstructed.email)
        assertEquals(profile.name, reconstructed.name)
        assertTrue(UserProfile.SUPABASE_PROFILES_TABLE_SCHEMA.contains("CREATE TABLE IF NOT EXISTS public.profiles"))
    }

    @Test
    fun supabaseAuth_validatesEmptyEmailAndPassword() = runBlocking {
        val client = SupabaseClient.default
        val emptyEmailResult = client.signUp("", "password123")
        assertTrue(emptyEmailResult.isFailure)

        val shortPasswordResult = client.signUp("test@example.com", "123")
        assertTrue(shortPasswordResult.isFailure)

        val emptySignInResult = client.signIn("", "")
        assertTrue(emptySignInResult.isFailure)
    }

    @Test
    fun post_mapsToEntityAndFromEntity_preservesAllMetadata() {
        val original = Post(
            id = "supabase-post-999",
            authorId = "author-777",
            authorName = "Sarpanch Ji",
            authorHandle = "sarpanch_official",
            authorAvatarUrl = "https://example.com/avatar.jpg",
            isVerified = true,
            content = "Announcement regarding road construction project in Sector 4. #Development",
            category = "Development",
            mediaUrl = "https://example.com/road.jpg",
            likesCount = 42,
            commentsCount = 8,
            repostsCount = 5,
            viewsCount = 350
        )

        assertEquals("supabase-post-999", original.id)
        assertEquals("Sarpanch Ji", original.authorName)
        assertEquals("sarpanch_official", original.authorHandle)
        assertEquals("Development", original.category)
        assertTrue(original.isVerified)
        assertEquals(42, original.likesCount)
        assertEquals(8, original.commentsCount)
        assertEquals(5, original.repostsCount)
        assertEquals(350, original.viewsCount)

        val entity = original.toEntity(isLiked = true, isReposted = false, isBookmarked = true)
        assertEquals("supabase-post-999", entity.id)
        assertEquals("Sarpanch Ji", entity.authorName)
        assertEquals("sarpanch_official", entity.authorHandle)
        assertEquals("Development", entity.category)
        assertEquals(42, entity.likesCount)
        assertEquals(8, entity.repliesCount)
        assertTrue(entity.isLiked)
        assertTrue(entity.isBookmarked)

        val reconstructed = Post.fromEntity(entity)
        assertEquals(original.id, reconstructed.id)
        assertEquals(original.authorName, reconstructed.authorName)
        assertEquals(original.authorHandle, reconstructed.authorHandle)
        assertEquals(original.content, reconstructed.content)
        assertEquals(original.likesCount, reconstructed.likesCount)
        assertEquals(original.commentsCount, reconstructed.commentsCount)
        assertEquals(original.viewsCount, reconstructed.viewsCount)
    }

    @Test
    fun getFallbackSamplePosts_returnsRichMetadataPosts() {
        val samplePosts = com.example.panchayat.ui.screens.getFallbackSamplePosts()
        assertTrue(samplePosts.isNotEmpty())
        samplePosts.forEach { post ->
            assertTrue(post.id.isNotEmpty())
            assertTrue(post.authorName.isNotEmpty())
            assertTrue(post.authorHandle.isNotEmpty())
            assertTrue(post.content.isNotEmpty())
            assertTrue(post.category.isNotEmpty())
            assertTrue(post.timestamp > 0)
        }
    }

    @Test
    fun supabaseClient_initialize_configuresDatabaseConnectivityAndRealtime() {
        val testUrl = "https://panchayat-cloud.supabase.co"
        val testKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test_anon_key_123"

        val client = SupabaseClient.initialize(
            supabaseUrl = testUrl,
            supabaseAnonKey = testKey,
            enableRealtime = true
        )

        assertNotNull(client)
        assertEquals(testUrl, client.supabaseUrl)
        assertEquals(testKey, client.supabaseAnonKey)
        assertEquals("https://panchayat-cloud.supabase.co/", client.formattedBaseUrl)
        assertTrue(client.isConfigured)

        // Database connectivity components
        assertNotNull(client.api)
        assertNotNull(client.authApi)
        assertNotNull(client.auth)
        assertNotNull(client.okHttpClient)

        // Real-time components
        assertNotNull(client.realtime)
        assertTrue(client.realtimeUrl.startsWith("wss://panchayat-cloud.supabase.co/realtime/v1/websocket"))
        assertTrue(client.realtimeUrl.contains("apikey=$testKey"))
        assertTrue(client.realtimeUrl.contains("vsn=1.0.0"))
        assertNotNull(client.realtimeStatus)

        // Real-time table subscription
        var receivedPostEvent = false
        val channel = client.subscribeToPosts { table, event, payload ->
            if (event == "INSERT") {
                receivedPostEvent = true
            }
        }
        assertTrue(channel.isSubscribed)
        assertEquals("realtime:public:posts", channel.topic)

        // Dispatch local event to verify listener dispatch
        client.realtime.dispatchLocalEvent("realtime:public:posts", "INSERT", """{"id":"post-1"}""")
        assertTrue(receivedPostEvent)
    }

    @Test
    fun supabaseClient_realtimeUrl_handlesHttpAndHttpsSchemes() {
        val httpsClient = SupabaseClient("https://example.supabase.co", "key_1")
        assertEquals("wss://example.supabase.co/realtime/v1/websocket?apikey=key_1&vsn=1.0.0", httpsClient.realtimeUrl)

        val httpClient = SupabaseClient("http://localhost:54321", "key_2")
        assertEquals("ws://localhost:54321/realtime/v1/websocket?apikey=key_2&vsn=1.0.0", httpClient.realtimeUrl)
    }
}
