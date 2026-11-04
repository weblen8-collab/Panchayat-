package com.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.panchayat.data.model.Post
import com.example.panchayat.ui.screens.SupabasePostsScreen as InternalSupabasePostsScreen

/**
 * Public composable screen that displays a list of posts fetched from the Supabase 'posts' table,
 * including a card layout for each post showing the author, content, and metadata.
 */
@Composable
fun SupabasePostsScreen(
    modifier: Modifier = Modifier,
    initialPosts: List<Post>? = null,
    onNavigateBack: (() -> Unit)? = null,
    onOpenPostDetail: ((Post) -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null
) {
    InternalSupabasePostsScreen(
        modifier = modifier,
        initialPosts = initialPosts,
        onNavigateBack = onNavigateBack,
        onOpenPostDetail = onOpenPostDetail,
        onOpenSettings = onOpenSettings
    )
}
