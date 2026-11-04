package com.example.panchayat.ui.components

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.remote.SupabaseClient
import com.example.ui.theme.LikePink
import kotlinx.coroutines.launch

private const val TAG = "LikeButton"

/**
 * A dedicated 'Like' button for individual microblog posts that updates the 'likes' count
 * in the Supabase table.
 *
 * Features:
 * - Immediate optimistic UI update (heart toggles and count increments/decrements).
 * - Smooth spring bouncing heart animation.
 * - Accessible minimum touch target (48dp x 48dp).
 * - Directly updates the 'likes_count' in the Supabase 'posts' table via [SupabaseClient.updatePostLikes]
 *   or [SupabaseClient.incrementPostLikes].
 * - Emits local realtime updates for immediate cross-component reactivity.
 * - Supports custom test tags: `like_button_${postId}` and `like_count_${postId}`.
 */
@Composable
fun LikeButton(
    postId: String,
    initialLikesCount: Int = 0,
    initialIsLiked: Boolean = false,
    client: SupabaseClient = SupabaseClient.default,
    modifier: Modifier = Modifier,
    activeColor: Color = LikePink,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    iconSize: Dp = 18.dp,
    showCount: Boolean = true,
    syncWithSupabase: Boolean = true,
    onLikeChanged: ((isLiked: Boolean, newLikesCount: Int) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var isLiked by remember(postId, initialIsLiked) { mutableStateOf(initialIsLiked) }
    var likesCount by remember(postId, initialLikesCount) { mutableIntStateOf(initialLikesCount) }

    // Bouncing spring animation for the heart icon
    val iconScale by animateFloatAsState(
        targetValue = if (isLiked) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "likeScale_$postId"
    )

    val contentDescription = if (isLiked) "Unlike" else "Like"

    Surface(
        color = Color.Transparent,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .semantics {
                this.contentDescription = contentDescription
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                role = Role.Button,
                onClickLabel = contentDescription
            ) {
                val newLikedState = !isLiked
                val newCount = if (newLikedState) likesCount + 1 else maxOf(0, likesCount - 1)

                // 1. Optimistic UI update
                isLiked = newLikedState
                likesCount = newCount

                // 2. Invoke callback
                onLikeChanged?.invoke(newLikedState, newCount)

                // 3. Persist updated likes count to Supabase backend table
                if (syncWithSupabase) {
                    coroutineScope.launch {
                        try {
                            val result = client.updatePostLikes(postId, newCount)
                            if (result.isSuccess) {
                                Log.d(TAG, "Successfully updated likes count for $postId in Supabase to $newCount")
                            } else {
                                Log.w(TAG, "Supabase likes update: ${result.exceptionOrNull()?.message}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing like count to Supabase: ${e.message}", e)
                        }
                    }
                }
            }
            .testTag("like_button_$postId")
            .testTag("post_like_button_$postId")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (isLiked) activeColor else inactiveColor,
                modifier = Modifier
                    .size(iconSize)
                    .scale(iconScale)
            )

            if (showCount) {
                Text(
                    text = "$likesCount",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = if (isLiked) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isLiked) activeColor else inactiveColor,
                    modifier = Modifier.testTag("like_count_$postId")
                )
            }
        }
    }
}

/**
 * Convenient overload accepting a domain [Post] model.
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
    LikeButton(
        postId = post.id,
        initialLikesCount = post.likesCount,
        initialIsLiked = post.isLiked,
        client = client,
        modifier = modifier,
        activeColor = activeColor,
        showCount = showCount,
        syncWithSupabase = syncWithSupabase,
        onLikeChanged = { newLiked, newCount ->
            val updatedPost = post.copy(
                isLiked = newLiked,
                likesCount = newCount
            )
            onLikeClick?.invoke(updatedPost)
        }
    )
}
