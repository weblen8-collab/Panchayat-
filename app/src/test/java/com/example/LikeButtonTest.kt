package com.example

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.remote.SupabaseClient
import com.example.panchayat.ui.components.LikeButton as InternalLikeButton
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LikeButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun likeButton_rendersInitialUnlikedStateAndCount() {
        composeTestRule.setContent {
            MyApplicationTheme {
                LikeButton(
                    postId = "post_alpha_1",
                    initialLikesCount = 12,
                    initialIsLiked = false,
                    syncWithSupabase = false
                )
            }
        }

        // Verify button and count are rendered with initial values
        composeTestRule.onNodeWithTag("like_button_post_alpha_1")
            .assertIsDisplayed()
            .assertContentDescriptionEquals("Like")

        composeTestRule.onNodeWithTag("like_count_post_alpha_1", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextEquals("12")
    }

    @Test
    fun likeButton_clickOptimisticallyIncrementsAndTriggersCallback() {
        var callbackLiked: Boolean? = null
        var callbackCount: Int? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                LikeButton(
                    postId = "post_beta_2",
                    initialLikesCount = 20,
                    initialIsLiked = false,
                    syncWithSupabase = false,
                    onLikeChanged = { isLiked, newCount ->
                        callbackLiked = isLiked
                        callbackCount = newCount
                    }
                )
            }
        }

        // Click the like button
        composeTestRule.onNodeWithTag("like_button_post_beta_2").performClick()

        // Verify optimistic UI update
        composeTestRule.onNodeWithTag("like_count_post_beta_2", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextEquals("21")

        composeTestRule.onNodeWithTag("like_button_post_beta_2")
            .assertContentDescriptionEquals("Unlike")

        // Verify callback was invoked with updated values
        assertTrue(callbackLiked == true)
        assertEquals(21, callbackCount)
    }

    @Test
    fun likeButton_clickOnLikedDecrementsAndReverts() {
        var callbackLiked: Boolean? = null
        var callbackCount: Int? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                LikeButton(
                    postId = "post_gamma_3",
                    initialLikesCount = 15,
                    initialIsLiked = true,
                    syncWithSupabase = false,
                    onLikeChanged = { isLiked, newCount ->
                        callbackLiked = isLiked
                        callbackCount = newCount
                    }
                )
            }
        }

        // Initial state is liked
        composeTestRule.onNodeWithTag("like_button_post_gamma_3")
            .assertContentDescriptionEquals("Unlike")

        composeTestRule.onNodeWithTag("like_count_post_gamma_3", useUnmergedTree = true)
            .assertTextEquals("15")

        // Click to unlike
        composeTestRule.onNodeWithTag("like_button_post_gamma_3").performClick()

        // Verify optimistic UI decrement
        composeTestRule.onNodeWithTag("like_count_post_gamma_3", useUnmergedTree = true)
            .assertTextEquals("14")

        composeTestRule.onNodeWithTag("like_button_post_gamma_3")
            .assertContentDescriptionEquals("Like")

        // Verify callback
        assertFalse(callbackLiked == true)
        assertEquals(14, callbackCount)
    }

    @Test
    fun likeButton_postOverload_invokesOnLikeClickWithUpdatedPost() {
        val testPost = Post(
            id = "post_delta_4",
            authorId = "author_4",
            authorName = "Sarpanch Ji",
            content = "New water pump sanctioned for Ward 3.",
            likesCount = 42,
            isLiked = false
        )

        var resultPost: Post? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                LikeButton(
                    post = testPost,
                    syncWithSupabase = false,
                    onLikeClick = { updatedPost ->
                        resultPost = updatedPost
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("like_button_post_delta_4").performClick()

        assertNotNull(resultPost)
        assertEquals("post_delta_4", resultPost?.id)
        assertTrue(resultPost?.isLiked == true)
        assertEquals(43, resultPost?.likesCount)
    }

    @Test
    fun supabaseClient_togglePostLikeLogic() = runBlocking {
        val client = SupabaseClient.default

        // Toggle like from unliked to liked
        val likedResult = client.togglePostLike(
            postId = "mock_post_100",
            currentLikesCount = 5,
            currentlyLiked = false
        )

        assertTrue(likedResult.isSuccess)
        val pairLiked = likedResult.getOrNull()
        assertNotNull(pairLiked)
        val (newLiked, newCount) = pairLiked!!
        assertTrue(newLiked)
        assertEquals(6, newCount)

        // Toggle back from liked to unliked
        val unlikedResult = client.togglePostLike(
            postId = "mock_post_100",
            currentLikesCount = 6,
            currentlyLiked = true
        )

        assertTrue(unlikedResult.isSuccess)
        val pairUnliked = unlikedResult.getOrNull()
        assertNotNull(pairUnliked)
        val (revertedLiked, revertedCount) = pairUnliked!!
        assertFalse(revertedLiked)
        assertEquals(5, revertedCount)
    }
}
