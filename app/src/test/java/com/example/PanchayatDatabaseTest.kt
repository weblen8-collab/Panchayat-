package com.example

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import com.example.panchayat.data.local.PanchayatDatabase
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.UserProfileEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Modifier

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PanchayatDatabaseTest {

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
    fun testPanchayatDatabase_extendsRoomDatabaseAndIsAbstract() {
        assertTrue(
            "PanchayatDatabase must extend RoomDatabase",
            RoomDatabase::class.java.isAssignableFrom(PanchayatDatabase::class.java)
        )
        assertTrue(
            "PanchayatDatabase must be an abstract class",
            Modifier.isAbstract(PanchayatDatabase::class.java.modifiers)
        )
    }

    @Test
    fun testPanchayatDatabase_containsUserProfileAndPostTables() {
        val cursor = db.openHelper.readableDatabase.query("SELECT name FROM sqlite_master WHERE type='table'")
        val tables = mutableListOf<String>()
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0))
        }
        cursor.close()
        assertTrue("Database should contain 'posts' table", tables.contains("posts"))
        assertTrue("Database should contain 'user_profiles' table", tables.contains("user_profiles"))
    }

    @Test
    fun testPanchayatDatabase_exposesDaosThroughAbstractMethods() {
        val postDaoMethod = PanchayatDatabase::class.java.getMethod("postDao")
        assertNotNull("PanchayatDatabase must have postDao method", postDaoMethod)
        assertTrue(
            "postDao method must be abstract",
            Modifier.isAbstract(postDaoMethod.modifiers)
        )

        val userDaoMethod = PanchayatDatabase::class.java.getMethod("userDao")
        assertNotNull("PanchayatDatabase must have userDao method", userDaoMethod)
        assertTrue(
            "userDao method must be abstract",
            Modifier.isAbstract(userDaoMethod.modifiers)
        )

        val userProfileDaoMethod = PanchayatDatabase::class.java.getMethod("userProfileDao")
        assertNotNull("PanchayatDatabase must have userProfileDao method", userProfileDaoMethod)
    }

    @Test
    fun testPanchayatDatabase_daoInstancesAccessibleAndFunctional() = runBlocking {
        assertNotNull("postDao instance should not be null", db.postDao())
        assertNotNull("userDao instance should not be null", db.userDao())
        assertNotNull("userProfileDao instance should not be null", db.userProfileDao())

        val user = UserProfileEntity(
            id = "user_test_db",
            username = "db_user",
            bio = "Testing database functionality",
            profileImageUrl = "https://example.com/db_user.png"
        )
        db.userDao().insertUser(user)

        val retrievedUser = db.userProfileDao().getUserById("user_test_db")
        assertNotNull(retrievedUser)
        assertEquals("db_user", retrievedUser?.username)

        val post = PostEntity(
            id = "post_test_db",
            authorId = user.id,
            content = "Testing post dao functionality in PanchayatDatabase",
            timestamp = System.currentTimeMillis()
        )
        db.postDao().insertPost(post)

        val retrievedPost = db.postDao().getPostById("post_test_db")
        assertNotNull(retrievedPost)
        assertEquals("post_test_db", retrievedPost?.id)
        assertEquals("Testing post dao functionality in PanchayatDatabase", retrievedPost?.content)
    }

    @Test
    fun testPanchayatDatabase_singletonAccessor() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val instance1 = PanchayatDatabase.getDatabase(context)
        val instance2 = PanchayatDatabase.getDatabase(context)
        assertNotNull(instance1)
        assertEquals("getDatabase should return the same singleton instance", instance1, instance2)
    }
}
