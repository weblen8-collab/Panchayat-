package com.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.remote.SupabaseClient
import com.example.panchayat.ui.components.LikeButton as InternalLikeButton
import com.example.ui.theme.LikePink

/**
 * Public composable LikeButton that displays a reactive 'Like' button for an individual
 * microblog post and synchronizes the updated likes count with the Supabase table.
 */
@Composable
fun LikeButton(
    postId: String,
    initialLikesCount: Int = 0,
    initialIsLiked: Boolean = false,
    client: SupabaseClient = SupabaseClient.default,
    modifier: Modifier = Modifier,
    activeColor: Color = LikePink,
    iconSize: Dp = 18.dp,
    showCount: Boolean = true,
    syncWithSupabase: Boolean = true,
    onLikeChanged: ((isLiked: Boolean, newLikesCount: Int) -> Unit)? = null
) {
    InternalLikeButton(
        postId = postId,
        initialLikesCount = initialLikesCount,
        initialIsLiked = initialIsLiked,
        client = client,
        modifier = modifier,
        activeColor = activeColor,
        iconSize = iconSize,
        showCount = showCount,
        syncWithSupabase = syncWithSupabase,
        onLikeChanged = onLikeChanged
    )
}

/**
 * Convenient overload accepting a [Post] domain model.
 */
@Composable
fun LikeButton(
    post: Post,
    client: SupabaseClient = SupabaseClient.default,
    modifier: Modifier = Modifier,
    activeColor: Color = LikePink,
    showCount: Boolean = true,
    syncWithSupabase: Boolean = true,
    onLikeClick: ((Post) -> Unit)? = null
) {
    InternalLikeButton(
        post = post,
        client = client,
        modifier = modifier,
        activeColor = activeColor,
        showCount = showCount,
        syncWithSupabase = syncWithSupabase,
        onLikeClick = onLikeClick
    )
}
