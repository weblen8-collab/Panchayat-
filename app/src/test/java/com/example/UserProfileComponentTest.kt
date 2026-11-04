package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.model.UserProfile
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserProfileComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testUser = UserProfileEntity(
        id = "user_test_101",
        name = "Devi Lal",
        handle = "devi_lal",
        bio = "Ward member advocating for clean water & village education.",
        avatarUrl = null,
        location = "Ballia, UP",
        joinedDate = "Joined October 2024",
        followingCount = 85,
        followersCount = 210,
        isVerified = true
    )

    private val sampleRecentPosts = listOf(
        Post(
            id = "recent_post_1",
            authorId = "user_test_101",
            authorName = "Devi Lal",
            authorHandle = "devi_lal",
            content = "Gram Sabha meeting scheduled for this Sunday at 10 AM.",
            category = "Gram Sabha",
            likesCount = 15,
            commentsCount = 4,
            repostsCount = 2
        ),
        Post(
            id = "recent_post_2",
            authorId = "user_test_101",
            authorName = "Devi Lal",
            authorHandle = "devi_lal",
            content = "Clean drinking water pipeline inspection completed in Ward 2.",
            category = "Development",
            likesCount = 33,
            commentsCount = 7,
            repostsCount = 5
        )
    )

    @Test
    fun userProfileComponent_displaysAvatarBioAndRecentPosts() {
        composeTestRule.setContent {
            MyApplicationTheme {
                UserProfileComponent(
                    user = testUser,
                    recentPosts = sampleRecentPosts
                )
            }
        }

        // 1. Verify user's avatar is displayed
        composeTestRule.onNodeWithTag("user_avatar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_avatar").assertIsDisplayed()

        // 2. Verify display name and handle
        composeTestRule.onNodeWithTag("user_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("user_name").assertTextContains("Devi Lal")
        composeTestRule.onNodeWithTag("user_handle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("user_handle").assertTextContains("@devi_lal")

        // 3. Verify user's bio is displayed
        composeTestRule.onNodeWithTag("user_bio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("user_bio").assertTextContains("Ward member advocating for clean water & village education.")

        // 4. Verify location and stats
        composeTestRule.onNodeWithTag("profile_user_location").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_user_location").assertTextContains("Ballia, UP")
        composeTestRule.onNodeWithTag("profile_following_count").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_followers_count").assertIsDisplayed()

        // 5. Verify recent posts section and list of recent posts
        composeTestRule.onNodeWithTag("recent_posts_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("recent_posts_title").assertIsDisplayed()

        // Scroll to recent posts and verify contents
        composeTestRule.onNodeWithTag("profile_screen_scroll")
            .performScrollToNode(hasTestTag("user_post_recent_post_1"))
        composeTestRule.onNodeWithTag("user_post_recent_post_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gram Sabha meeting scheduled for this Sunday at 10 AM.").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_screen_scroll")
            .performScrollToNode(hasTestTag("user_post_recent_post_2"))
        composeTestRule.onNodeWithTag("user_post_recent_post_2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clean drinking water pipeline inspection completed in Ward 2.").assertIsDisplayed()
    }

    @Test
    fun userProfileComponent_displaysEmptyPostsState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                UserProfileComponent(
                    user = testUser,
                    recentPosts = emptyList()
                )
            }
        }

        // Verify avatar and bio are displayed
        composeTestRule.onNodeWithTag("user_avatar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("user_bio").assertIsDisplayed()

        // Verify empty posts placeholder
        composeTestRule.onNodeWithTag("profile_screen_scroll")
            .performScrollToNode(hasTestTag("profile_empty_posts"))
        composeTestRule.onNodeWithTag("profile_empty_posts").assertIsDisplayed()
        composeTestRule.onNodeWithText("No posts yet").assertIsDisplayed()
    }

    @Test
    fun userProfileComponent_postInteractions_invokeCallbacks() {
        var clickedPostId: String? = null
        var likedPostId: String? = null
        var repliedPostId: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                UserProfileComponent(
                    user = testUser,
                    recentPosts = sampleRecentPosts,
                    onPostClick = { clickedPostId = it.id },
                    onLikeClick = { likedPostId = it },
                    onReplyClick = { repliedPostId = it.id }
                )
            }
        }

        // Click Like on post 1
        composeTestRule.onNodeWithTag("profile_screen_scroll")
            .performScrollToNode(hasTestTag("like_button_recent_post_1"))
        composeTestRule.onNodeWithTag("like_button_recent_post_1").performClick()
        assertEquals("recent_post_1", likedPostId)

        // Click Reply on post 1
        composeTestRule.onNodeWithTag("reply_button_recent_post_1").performClick()
        assertEquals("recent_post_1", repliedPostId)

        // Click post card 1
        composeTestRule.onNodeWithTag("user_post_recent_post_1").performClick()
        assertEquals("recent_post_1", clickedPostId)
    }

    @Test
    fun userProfileComponent_worksWithDirectFields() {
        var editBioClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                UserProfileComponent(
                    name = "Sarpanch Phulera",
                    handle = "sarpanch_official",
                    bio = "Serving Gram Panchayat Phulera with transparency and dedication.",
                    avatarUrl = null,
                    recentPosts = sampleRecentPosts,
                    onEditBioClick = { editBioClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("user_avatar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("user_name").assertTextContains("Sarpanch Phulera")
        composeTestRule.onNodeWithTag("user_handle").assertTextContains("@sarpanch_official")
        composeTestRule.onNodeWithTag("user_bio").assertTextContains("Serving Gram Panchayat Phulera", substring = true)

        // Click Edit Bio button
        composeTestRule.onNodeWithTag("edit_bio_button").performClick()
        assertTrue(editBioClicked)
    }

    @Test
    fun userProfileComponent_worksWithDomainUserProfile() {
        val domainProfile = UserProfile(
            id = "user_domain_001",
            name = "Vikas Kumar",
            handle = "vikas_sec",
            bio = "Panchayat Secretary assisting rural administration.",
            location = "Ballia Block",
            followingCount = 45,
            followersCount = 520,
            isVerified = true
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                UserProfileComponent(
                    profile = domainProfile,
                    recentPosts = sampleRecentPosts
                )
            }
        }

        composeTestRule.onNodeWithTag("user_avatar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("user_name").assertTextContains("Vikas Kumar")
        composeTestRule.onNodeWithTag("user_bio").assertTextContains("Panchayat Secretary", substring = true)
    }
}
