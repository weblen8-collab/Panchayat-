package com.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.panchayat.data.model.Post
import com.example.panchayat.ui.screens.FeedScreen as InternalFeedScreen

/**
 * Public composable FeedScreen layout with a LazyColumn to display
 * microblogging posts fetched from Supabase.
 */
@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    initialPosts: List<Post>? = null,
    onPostClick: ((Post) -> Unit)? = null,
    onLikeClick: ((Post) -> Unit)? = null,
    onReplyClick: ((Post) -> Unit)? = null,
    onRepostClick: ((Post) -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    InternalFeedScreen(
        modifier = modifier,
        initialPosts = initialPosts,
        onPostClick = onPostClick,
        onLikeClick = onLikeClick,
        onReplyClick = onReplyClick,
        onRepostClick = onRepostClick,
        onRefresh = onRefresh
    )
}
