package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.panchayat.data.local.PanchayatDatabase
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.UserProfileEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PostEntityTest {

    private lateinit var db: PanchayatDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PanchayatDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testPostEntity_hasCoreMicrobloggingFields() {
        val now = System.currentTimeMillis()
        val post = PostEntity(
            id = "post_test_1",
            authorId = "user_sarpanch",
            content = "Gram Sabha meeting scheduled for Saturday at 10 AM.",
            timestamp = now
        )

        assertEquals("post_test_1", post.id)
        assertEquals("user_sarpanch", post.authorId)
        assertEquals("user_sarpanch", post.userId)
        assertEquals("Gram Sabha meeting scheduled for Saturday at 10 AM.", post.content)
        assertEquals("Gram Sabha meeting scheduled for Saturday at 10 AM.", post.postContent)
        assertEquals(now, post.timestamp)
    }

    @Test
    fun testPostEntity_secondaryConstructorWithContentAuthorIdTimestamp() {
        val now = System.currentTimeMillis()
        val post = PostEntity(
            content = "Solar street lights approved for West Tola!",
            authorId = "user_me",
            timestamp = now
        )

        assertNotNull(post.id)
        assertEquals("user_me", post.authorId)
        assertEquals("user_me", post.userId)
        assertEquals("Solar street lights approved for West Tola!", post.content)
        assertEquals(now, post.timestamp)
    }

    @Test
    fun testPostEntity_secondaryConstructorWithUserId() {
        val customId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val post = PostEntity(
            id = customId,
            userId = "user_secretary",
            content = "Digital Panchayat center high-speed fiber is now live.",
            timestamp = now
        )

        assertEquals(customId, post.id)
        assertEquals("user_secretary", post.authorId)
        assertEquals("user_secretary", post.userId)
        assertEquals("Digital Panchayat center high-speed fiber is now live.", post.content)
        assertEquals(now, post.timestamp)
    }

    @Test
    fun testPostEntity_roomDatabaseForeignKeyIntegration() = runBlocking {
        val user = UserProfileEntity(
            id = "user_sarpanch",
            username = "sarpanch_phulera",
            name = "Manju Devi",
            handle = "sarpanch_phulera",
            bio = "Elected Sarpanch of Phulera Gram Panchayat"
        )
        db.userDao().insertUser(user)

        val post = PostEntity(
            id = "post_cuj_1",
            authorId = user.id,
            content = "Panchayat development work starting this week.",
            timestamp = System.currentTimeMillis()
        )
        db.postDao().insertPost(post)

        val retrieved = db.postDao().getPostById("post_cuj_1")
        assertNotNull(retrieved)
        assertEquals("post_cuj_1", retrieved?.id)
        assertEquals(user.id, retrieved?.authorId)
        assertEquals(user.id, retrieved?.userId)
        assertEquals("Panchayat development work starting this week.", retrieved?.content)
    }

    @Test
    fun testPostEntity_rootTypealias() {
        val post: com.example.PostEntity = PostEntity(
            content = "Microblogging post via root typealias",
            authorId = "user_1"
        )
        assertEquals("user_1", post.authorId)
        assertEquals("Microblogging post via root typealias", post.content)
    }
}

