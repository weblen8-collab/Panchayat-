package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.panchayat.data.model.Post
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PostFeedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val samplePosts = listOf(
        Post(
            id = "supabase_post_alpha",
            authorId = "author_001",
            authorName = "Manju Devi (Sarpanch)",
            authorHandle = "manju_devi",
            content = "Solar lighting installation approved for Ward 2 and Ward 4. #Development",
            category = "Development",
            likesCount = 34,
            commentsCount = 6,
            repostsCount = 4
        ),
        Post(
            id = "supabase_post_beta",
            authorId = "author_002",
            authorName = "Abhishek Tripathi",
            authorHandle = "sachiv_ji",
            content = "General Gram Sabha meeting this Sunday morning at the Panchayat Bhavan.",
            category = "Gram Sabha",
            likesCount = 52,
            commentsCount = 11,
            repostsCount = 8
        ),
        Post(
            id = "supabase_post_gamma",
            authorId = "author_003",
            authorName = "Prahlad Pandey",
            authorHandle = "prahlad_up",
            content = "New seeds distribution starts tomorrow at the cooperative center.",
            category = "Agriculture",
            likesCount = 19,
            commentsCount = 3,
            repostsCount = 2
        )
    )

    @Test
    fun postFeed_displaysLazyColumnWithPosts() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PostFeed(
                    initialPosts = samplePosts,
                    autoFetch = false
                )
            }
        }

        // Verify root container and LazyColumn
        composeTestRule.onNodeWithTag("post_feed").assertIsDisplayed()
        composeTestRule.onNodeWithTag("post_feed_list").assertIsDisplayed()

        // Verify all posts are present
        composeTestRule.onNodeWithTag("post_item_supabase_post_alpha").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manju Devi (Sarpanch)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solar lighting installation approved for Ward 2 and Ward 4. #Development").assertIsDisplayed()

        composeTestRule.onNodeWithTag("post_item_supabase_post_beta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Abhishek Tripathi").assertIsDisplayed()
    }

    @Test
    fun postFeed_onPostClick_invokesCallback() {
        var selectedPost: Post? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                PostFeed(
                    initialPosts = samplePosts,
                    autoFetch = false,
                    onPostClick = { selectedPost = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("post_item_supabase_post_alpha").performClick()
        assertNotNull(selectedPost)
        assertEquals("supabase_post_alpha", selectedPost?.id)
        assertEquals("Manju Devi (Sarpanch)", selectedPost?.authorName)
    }

    @Test
    fun postFeed_onLikeClick_invokesCallback() {
        var likedPost: Post? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                PostFeed(
                    initialPosts = samplePosts,
                    autoFetch = false,
                    onLikeClick = { likedPost = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("like_button_supabase_post_alpha").performClick()
        assertNotNull(likedPost)
        assertEquals("supabase_post_alpha", likedPost?.id)
        assertTrue(likedPost?.isLiked == true)
        assertEquals(35, likedPost?.likesCount)
    }

    @Test
    fun postFeed_emptyPosts_displaysEmptyState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PostFeed(
                    initialPosts = emptyList(),
                    autoFetch = false
                )
            }
        }

        composeTestRule.onNodeWithTag("post_feed_empty").assertIsDisplayed()
        composeTestRule.onNodeWithText("No Posts Yet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("post_feed_empty_refresh_button").assertIsDisplayed()
    }

    @Test
    fun postFeed_filtersByCategory() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PostFeed(
                    initialPosts = samplePosts,
                    autoFetch = false,
                    categoryFilter = "Gram Sabha"
                )
            }
        }

        // Matching category is shown
        composeTestRule.onNodeWithTag("post_item_supabase_post_beta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Abhishek Tripathi").assertIsDisplayed()

        // Non-matching categories are filtered out
        composeTestRule.onNodeWithTag("post_item_supabase_post_alpha").assertDoesNotExist()
        composeTestRule.onNodeWithTag("post_item_supabase_post_gamma").assertDoesNotExist()
    }

    @Test
    fun postFeed_filtersBySearchQuery() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PostFeed(
                    initialPosts = samplePosts,
                    autoFetch = false,
                    searchQuery = "seeds distribution"
                )
            }
        }

        // Matching query is shown
        composeTestRule.onNodeWithTag("post_item_supabase_post_gamma").assertIsDisplayed()
        composeTestRule.onNodeWithText("Prahlad Pandey").assertIsDisplayed()

        // Non-matching queries are filtered out
        composeTestRule.onNodeWithTag("post_item_supabase_post_alpha").assertDoesNotExist()
        composeTestRule.onNodeWithTag("post_item_supabase_post_beta").assertDoesNotExist()
    }
}
