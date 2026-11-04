package com.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.panchayat.data.model.Post
import com.example.panchayat.ui.screens.CreatePostScreen as InternalCreatePostScreen

/**
 * Public composable CreatePostScreen component that includes a TextField for user input
 * and a Floating Action Button to submit a new post to the Supabase database.
 */
@Composable
fun CreatePostScreen(
    modifier: Modifier = Modifier,
    initialContent: String = "",
    authorId: String = "user_default",
    authorName: String = "Gram Resident",
    authorHandle: String = "panchayat_resident",
    onPostSubmitted: ((Post) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    onSubmitPost: (suspend (Post) -> Result<Post>)? = null
) {
    InternalCreatePostScreen(
        modifier = modifier,
        initialContent = initialContent,
        authorId = authorId,
        authorName = authorName,
        authorHandle = authorHandle,
        onPostSubmitted = onPostSubmitted,
        onNavigateBack = onNavigateBack,
        onSubmitPost = onSubmitPost
    )
}
