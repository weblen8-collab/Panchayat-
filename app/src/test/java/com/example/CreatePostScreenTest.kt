package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class CreatePostScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun createPostScreen_rendersTextFieldAndFAB() {
        composeTestRule.setContent {
            MyApplicationTheme {
                CreatePostScreen(
                    authorName = "Sarpanch Phulera",
                    authorHandle = "sarpanch_phulera"
                )
            }
        }

        // Verify Title and Subtitle
        composeTestRule.onNodeWithText("Create Post").assertIsDisplayed()
        composeTestRule.onNodeWithText("Publishing to Supabase 'posts' table").assertIsDisplayed()

        // Verify TextField exists and is displayed
        composeTestRule.onNodeWithTag("post_text_field").assertIsDisplayed()

        // Verify Floating Action Button exists and is displayed
        composeTestRule.onNodeWithTag("submit_post_fab").assertIsDisplayed()

        // Verify Category chips exist
        composeTestRule.onNodeWithTag("category_chip_Gram Sabha").assertIsDisplayed()
    }

    @Test
    fun createPostScreen_enteringTextAndClickingFAB_submitsPostToSupabase() {
        var submittedPost: Post? = null
        val testContent = "New solar powered street lights will be installed in Ward 5 this week."

        composeTestRule.setContent {
            MyApplicationTheme {
                CreatePostScreen(
                    authorId = "author_test_123",
                    authorName = "Sarpanch Phulera",
                    authorHandle = "sarpanch_phulera",
                    onSubmitPost = { post ->
                        submittedPost = post
                        Result.success(post)
                    },
                    onPostSubmitted = { post ->
                        submittedPost = post
                    }
                )
            }
        }

        // Enter text into TextField
        composeTestRule.onNodeWithTag("post_text_field").performTextInput(testContent)

        // Click Floating Action Button to submit to Supabase
        composeTestRule.onNodeWithTag("submit_post_fab").performClick()

        // Verify submitted post has entered content and author metadata
        assertNotNull(submittedPost)
        assertEquals(testContent, submittedPost?.content)
        assertEquals("author_test_123", submittedPost?.authorId)
        assertEquals("Sarpanch Phulera", submittedPost?.authorName)
        assertEquals("sarpanch_phulera", submittedPost?.authorHandle)
        assertTrue((submittedPost?.timestamp ?: 0) > 0)
    }

    @Test
    fun createPostScreen_withInitialContent_populatesTextField() {
        val initialText = "Draft notice regarding Gram Sabha venue change."

        composeTestRule.setContent {
            MyApplicationTheme {
                CreatePostScreen(
                    initialContent = initialText
                )
            }
        }

        composeTestRule.onNodeWithText(initialText).assertIsDisplayed()
    }
}
