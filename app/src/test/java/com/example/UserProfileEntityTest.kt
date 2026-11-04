package com.example

import com.example.panchayat.data.local.entity.UserProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class UserProfileEntityTest {

    @Test
    fun testUserProfileEntity_withUsernameBioAndProfileImageUrl() {
        val user = UserProfileEntity(
            username = "sarpanch_phulera",
            bio = "Elected Sarpanch of Phulera Gram Panchayat",
            profileImageUrl = "https://example.com/avatar.png"
        )

        assertEquals("sarpanch_phulera", user.username)
        assertEquals("Elected Sarpanch of Phulera Gram Panchayat", user.bio)
        assertEquals("https://example.com/avatar.png", user.profileImageUrl)
        assertNotNull(user.id)
    }

    @Test
    fun testUserProfileEntity_withExplicitId() {
        val customId = UUID.randomUUID().toString()
        val user = UserProfileEntity(
            id = customId,
            username = "sachiv_ji",
            bio = "Panchayat Secretary handling village affairs",
            profileImageUrl = "https://example.com/sachiv.png"
        )

        assertEquals(customId, user.id)
        assertEquals("sachiv_ji", user.username)
        assertEquals("Panchayat Secretary handling village affairs", user.bio)
        assertEquals("https://example.com/sachiv.png", user.profileImageUrl)
        assertEquals("sachiv_ji", user.effectiveUsername)
        assertEquals("https://example.com/sachiv.png", user.effectiveProfileImageUrl)
    }

    @Test
    fun testUserProfileEntity_fallbackProperties() {
        val user = UserProfileEntity(
            id = "user_123",
            handle = "krishi_vikas",
            name = "Gram Krishi Kendra",
            bio = "Mandi rates and agricultural advisory",
            avatarUrl = "https://example.com/krishi.png"
        )

        assertEquals("krishi_vikas", user.username)
        assertEquals("krishi_vikas", user.handle)
        assertEquals("Gram Krishi Kendra", user.name)
        assertEquals("Mandi rates and agricultural advisory", user.bio)
        assertEquals("https://example.com/krishi.png", user.profileImageUrl)
        assertEquals("https://example.com/krishi.png", user.avatarUrl)
    }

    @Test
    fun testUserProfileEntity_nullableProfileImageUrl() {
        val user = UserProfileEntity(
            username = "pradhan_ji",
            bio = "Preserving village heritage",
            profileImageUrl = null
        )

        assertEquals("pradhan_ji", user.username)
        assertEquals("Preserving village heritage", user.bio)
        assertNull(user.profileImageUrl)
    }
}
