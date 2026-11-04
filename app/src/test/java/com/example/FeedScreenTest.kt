package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
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
class FeedScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val samplePosts = listOf(
        Post(
            id = "supabase_post_1",
            authorId = "author_1",
            authorName = "Sarpanch Phulera",
            authorHandle = "sarpanch_phulera",
            content = "Gram Sabha meeting scheduled for this Sunday at 10 AM regarding water supply project.",
            category = "Gram Sabha",
            likesCount = 25,
            commentsCount = 4,
            repostsCount = 6
        ),
        Post(
            id = "supabase_post_2",
            authorId = "author_2",
            authorName = "Gram Sachiv",
            authorHandle = "sachiv_ji",
            content = "Notice: All PM Kisan Samman Nidhi beneficiaries please complete eKYC by next week.",
            category = "Civic Notice",
            likesCount = 18,
            commentsCount = 2,
            repostsCount = 3
        )
    )

    @Test
    fun feedScreen_displaysLazyColumnWithPosts() {
        composeTestRule.setContent {
            MyApplicationTheme {
                FeedScreen(
                    initialPosts = samplePosts
                )
            }
        }

        // Verify Header
        composeTestRule.onNodeWithText("Panchayat Feed").assertIsDisplayed()

        // Verify LazyColumn is displayed
        composeTestRule.onNodeWithTag("feed_lazy_column").assertIsDisplayed()

        // Verify individual post contents
        composeTestRule.onNodeWithText("Sarpanch Phulera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gram Sabha meeting scheduled for this Sunday at 10 AM regarding water supply project.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gram Sachiv").assertIsDisplayed()
    }

    @Test
    fun feedScreen_postClick_triggersCallback() {
        var clickedPost: Post? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                FeedScreen(
                    initialPosts = samplePosts,
                    onPostClick = { post -> clickedPost = post }
                )
            }
        }

        composeTestRule.onNodeWithTag("post_item_supabase_post_1").performClick()
        assertEquals("supabase_post_1", clickedPost?.id)
        assertEquals("Sarpanch Phulera", clickedPost?.authorName)
    }

    @Test
    fun feedScreen_emptyPosts_displaysEmptyState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                FeedScreen(
                    initialPosts = emptyList()
                )
            }
        }

        composeTestRule.onNodeWithTag("empty_state").assertIsDisplayed()
        composeTestRule.onNodeWithText("No Posts Yet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("refresh_empty_button").assertIsDisplayed()
    }

    @Test
    fun feedScreen_refreshButton_isClickable() {
        var refreshTriggered = false

        composeTestRule.setContent {
            MyApplicationTheme {
                FeedScreen(
                    initialPosts = samplePosts,
                    onRefresh = { refreshTriggered = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("refresh_button").assertIsDisplayed().performClick()
        assertTrue(refreshTriggered)
    }

    @Test
    fun feedScreen_pullToRefreshBox_isDisplayed_andSwipeTriggersRefresh() {
        var refreshTriggered = false

        composeTestRule.setContent {
            MyApplicationTheme {
                FeedScreen(
                    initialPosts = samplePosts,
                    onRefresh = { refreshTriggered = true }
                )
            }
        }

        // Verify the pull-to-refresh container is displayed
        composeTestRule.onNodeWithTag("pull_to_refresh_box").assertIsDisplayed()

        // Perform swipe down gesture on the feed to trigger pull-to-refresh
        composeTestRule.onNodeWithTag("feed_lazy_column").performTouchInput {
            swipeDown()
        }
        composeTestRule.waitForIdle()

        assertTrue(refreshTriggered)
    }

    @Test
    fun feedScreen_displaysLikeButtonOnEachPost() {
        composeTestRule.setContent {
            MyApplicationTheme {
                FeedScreen(
                    initialPosts = samplePosts
                )
            }
        }

        // Verify Like button is displayed on each post in the FeedScreen
        composeTestRule.onNodeWithTag("like_button_supabase_post_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("25").assertIsDisplayed()

        // Verify Like button exists on second post and is displayed when scrolled
        composeTestRule.onNodeWithTag("like_button_supabase_post_2")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("18").assertIsDisplayed()
    }

    @Test
    fun feedScreen_clickingLikeButton_incrementsLikeCount_andTriggersCallback() {
        var likedPost: Post? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                FeedScreen(
                    initialPosts = samplePosts,
                    onLikeClick = { post -> likedPost = post }
                )
            }
        }

        // Verify initial count
        composeTestRule.onNodeWithText("25").assertIsDisplayed()

        // Click the Like button on the first post
        composeTestRule.onNodeWithTag("like_button_supabase_post_1").performClick()

        // Verify the callback was triggered with the incremented post
        assertEquals("supabase_post_1", likedPost?.id)
        assertEquals(26, likedPost?.likesCount)

        // Verify the incremented count is displayed on the screen
        composeTestRule.onNodeWithText("26").assertIsDisplayed()
    }

    @Test
    fun feedScreen_displaysCommentButton_andOpensCommentSectionWithExistingReplies() {
        composeTestRule.setContent {
            MyApplicationTheme {
                FeedScreen(
                    initialPosts = samplePosts
                )
            }
        }

        // Verify initial comment count is displayed
        composeTestRule.onNodeWithText("4").assertIsDisplayed()

        // Click the comment button to expand the comment section
        composeTestRule.onNodeWithTag("comment_button_supabase_post_1").performClick()
        composeTestRule.mainClock.advanceTimeBy(1000)
        composeTestRule.waitForIdle()

        // Verify the comment section container is displayed
        composeTestRule.onNodeWithTag("comment_section_supabase_post_1", useUnmergedTree = true).assertIsDisplayed()

        // Verify the replies list is displayed
        composeTestRule.onNodeWithTag("comments_list_supabase_post_1", useUnmergedTree = true).assertIsDisplayed()

        // Verify existing reply content is displayed
        composeTestRule.onNodeWithText("Sachiv Ji (Abhishek)", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("All ward members please gather at 10 AM sharp for the resolution signing.", useUnmergedTree = true).assertExists()

        // Verify reply input field and submit button are present
        composeTestRule.onNodeWithTag("reply_input_field", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("reply_submit_button", useUnmergedTree = true).assertExists()
    }

    @Test
    fun feedScreen_submittingReply_addsReplyToList_andIncrementsCommentCount() {
        composeTestRule.setContent {
            MyApplicationTheme {
                FeedScreen(
                    initialPosts = samplePosts
                )
            }
        }

        // Expand comments on post 1
        composeTestRule.onNodeWithTag("comment_button_supabase_post_1").performClick()
        composeTestRule.mainClock.advanceTimeBy(1000)
        composeTestRule.waitForIdle()

        // Enter a new reply
        val newCommentText = "We need additional drinking water taps near the primary health center."
        composeTestRule.onNodeWithTag("reply_input_field", useUnmergedTree = true)
            .performScrollTo()
            .performTextInput(newCommentText)
        composeTestRule.waitForIdle()

        // Click submit reply
        composeTestRule.onNodeWithTag("reply_submit_button", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.waitForIdle()

        // Verify the newly submitted reply is displayed in the list
        composeTestRule.onNodeWithText(newCommentText, useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()

        // Verify the comment count incremented from 4 to 5
        composeTestRule.onNodeWithText("5", useUnmergedTree = true).assertExists()
    }
}
