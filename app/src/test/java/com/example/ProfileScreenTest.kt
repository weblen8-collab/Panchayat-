package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.panchayat.data.model.Post
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
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testUser = UserProfileEntity(
        id = "user_me",
        name = "Aman Verma",
        handle = "aman_verma",
        bio = "Rural tech enthusiast & community volunteer in Phulera.",
        avatarUrl = null,
        bannerUrl = null,
        location = "Phulera, Ballia",
        joinedDate = "Joined September 2024",
        followingCount = 142,
        followersCount = 389,
        isVerified = true
    )

    private val userPosts = listOf(
        Post(
            id = "user_post_1",
            authorId = "user_me",
            authorName = "Aman Verma",
            authorHandle = "aman_verma",
            content = "Excited to inaugurate the digital Panchayat literacy center today!",
            category = "Development",
            likesCount = 42,
            commentsCount = 7,
            repostsCount = 5
        ),
        Post(
            id = "user_post_2",
            authorId = "user_me",
            authorName = "Aman Verma",
            authorHandle = "aman_verma",
            content = "Solar streetlight installation completed in Ward 4.",
            category = "Civic Notice",
            likesCount = 28,
            commentsCount = 3,
            repostsCount = 2
        )
    )

    private val otherPosts = listOf(
        Post(
            id = "other_post_1",
            authorId = "user_other",
            authorName = "Sarpanch Phulera",
            authorHandle = "sarpanch_phulera",
            content = "General meeting at the community center.",
            category = "Gram Sabha"
        )
    )

    @Test
    fun profileScreen_displaysUserProfileAndDetails() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    user = testUser,
                    userPosts = userPosts
                )
            }
        }

        // Verify profile details are rendered
        composeTestRule.onNodeWithTag("profile_user_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_user_name").assertTextContains("Aman Verma")

        composeTestRule.onNodeWithTag("profile_user_handle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_user_handle").assertTextContains("@aman_verma")

        composeTestRule.onNodeWithTag("profile_user_bio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_user_bio").assertTextContains("Rural tech enthusiast & community volunteer in Phulera.")

        composeTestRule.onNodeWithTag("profile_user_location").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_user_location").assertTextContains("Phulera, Ballia")

        composeTestRule.onNodeWithTag("profile_joined_date").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_following_count").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_followers_count").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_posts_count").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysUserPostHistory() {
        val allEntities = (userPosts + otherPosts).map { it.toEntity() }

        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    user = testUser,
                    posts = allEntities
                )
            }
        }

        // Verify the user's own post history is shown
        composeTestRule.onNodeWithTag("profile_screen_scroll")
            .performScrollToNode(hasTestTag("user_post_user_post_1"))
        composeTestRule.onNodeWithTag("user_post_user_post_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Excited to inaugurate the digital Panchayat literacy center today!").assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_screen_scroll")
            .performScrollToNode(hasTestTag("user_post_user_post_2"))
        composeTestRule.onNodeWithTag("user_post_user_post_2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solar streetlight installation completed in Ward 4.").assertIsDisplayed()

        // Verify other users' posts are filtered out of the profile feed
        composeTestRule.onNodeWithTag("user_post_other_post_1").assertDoesNotExist()
    }

    @Test
    fun profileScreen_opensEditProfileDialog_andUpdatesBio() {
        var updatedBioResult = ""
        var updatedNameResult = ""

        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    user = testUser,
                    userPosts = userPosts,
                    onUpdateBio = { updatedBioResult = it },
                    onUpdateProfile = { name, bio, _ ->
                        updatedNameResult = name
                        updatedBioResult = bio
                    }
                )
            }
        }

        // Click Edit profile button
        composeTestRule.onNodeWithTag("edit_profile_button").performClick()

        // Verify Edit Profile dialog appears
        composeTestRule.onNodeWithTag("edit_profile_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("edit_bio_field").assertIsDisplayed()

        // Change bio
        val newBio = "Passionate about empowering rural communities with modern tech."
        composeTestRule.onNodeWithTag("edit_bio_field").performTextClearance()
        composeTestRule.onNodeWithTag("edit_bio_field").performTextInput(newBio)

        // Click Save
        composeTestRule.onNodeWithTag("save_profile_button").performClick()

        // Verify dialog dismissed
        composeTestRule.onNodeWithTag("edit_profile_dialog").assertDoesNotExist()

        // Verify callback was invoked with updated bio
        assertEquals(newBio, updatedBioResult)

        // Verify UI displays the updated bio
        composeTestRule.onNodeWithTag("profile_user_bio").assertTextContains(newBio)
    }

    @Test
    fun profileScreen_editBioIconButton_opensDialog() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    user = testUser,
                    userPosts = userPosts
                )
            }
        }

        // Click direct edit bio button
        composeTestRule.onNodeWithTag("edit_bio_button").performClick()

        // Verify dialog opens
        composeTestRule.onNodeWithTag("edit_profile_dialog").assertIsDisplayed()

        // Cancel dialog
        composeTestRule.onNodeWithTag("cancel_edit_profile_button").performClick()
        composeTestRule.onNodeWithTag("edit_profile_dialog").assertDoesNotExist()
    }

    @Test
    fun profileScreen_tabSwitching() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    user = testUser,
                    userPosts = userPosts
                )
            }
        }

        // Check tabs exist
        composeTestRule.onNodeWithTag("profile_tab_posts").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_replies").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_tab_likes").assertIsDisplayed()

        // Switch to Replies
        composeTestRule.onNodeWithTag("profile_tab_replies").performClick()

        // Switch to Likes
        composeTestRule.onNodeWithTag("profile_tab_likes").performClick()

        // Switch back to Posts
        composeTestRule.onNodeWithTag("profile_tab_posts").performClick()
        composeTestRule.onNodeWithTag("profile_screen_scroll")
            .performScrollToNode(hasTestTag("user_post_user_post_1"))
        composeTestRule.onNodeWithTag("user_post_user_post_1").assertIsDisplayed()
    }

    @Test
    fun profileScreen_emptyPostsState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    user = testUser,
                    posts = emptyList()
                )
            }
        }

        // In empty state, empty posts placeholder is displayed
        composeTestRule.onNodeWithTag("profile_screen_scroll")
            .performScrollToNode(hasTestTag("profile_empty_posts"))
        composeTestRule.onNodeWithTag("profile_empty_posts").assertIsDisplayed()
    }

    @Test
    fun profileScreen_supabaseSyncBadgeIsDisplayed() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    user = testUser,
                    userPosts = userPosts
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_supabase_sync_badge").assertIsDisplayed()
    }

    @Test
    fun profileScreen_worksWithDomainUserProfile() {
        val domainUser = com.example.panchayat.data.model.UserProfile(
            id = "user_me",
            name = "Aman Verma",
            handle = "aman_verma",
            bio = "Rural tech enthusiast & community volunteer in Phulera.",
            location = "Phulera, Ballia",
            followingCount = 142,
            followersCount = 389,
            isVerified = true
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    profile = domainUser,
                    userPosts = userPosts
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_user_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_user_name").assertTextContains("Aman Verma")
        composeTestRule.onNodeWithTag("profile_user_handle").assertTextContains("@aman_verma")
        composeTestRule.onNodeWithTag("profile_user_bio").assertTextContains("Rural tech enthusiast", substring = true)
    }
}
