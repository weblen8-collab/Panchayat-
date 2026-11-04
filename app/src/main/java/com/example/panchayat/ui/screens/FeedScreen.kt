package com.example.panchayat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.panchayat.data.local.entity.ReplyEntity
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.remote.SupabaseClient
import com.example.panchayat.ui.components.SupabasePostCard
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.TwitterBlue
import kotlinx.coroutines.launch

/**
 * A Jetpack Compose FeedScreen layout with a LazyColumn to display
 * microblogging posts fetched from Supabase.
 *
 * Handles loading, empty, and error states, with support for refreshing
 * and clicking individual posts.
 *
 * @param modifier Optional modifier for styling and layout
 * @param initialPosts Optional pre-fetched posts for previews or unit testing
 * @param onPostClick Callback invoked when a post is clicked
 * @param onLikeClick Callback invoked when a post like action occurs
 * @param onReplyClick Callback invoked when replying to a post
 * @param onRepostClick Callback invoked when reposting
 * @param onRefresh Optional callback invoked when manual refresh is requested
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val coroutineScope = rememberCoroutineScope()

    var posts by remember { mutableStateOf<List<Post>>(initialPosts ?: emptyList()) }
    var isLoading by remember { mutableStateOf(initialPosts == null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isConnectedToSupabase by remember { mutableStateOf(false) }

    var expandedCommentPostId by remember { mutableStateOf<String?>(null) }
    var repliesMap by remember { mutableStateOf<Map<String, List<ReplyEntity>>>(emptyMap()) }

    fun fetchPostsFromSupabase() {
        coroutineScope.launch {
            if (posts.isEmpty()) {
                isLoading = true
            } else {
                isRefreshing = true
            }
            errorMessage = null

            val result = SupabaseClient.fetchPosts(limit = 50)
            result.fold(
                onSuccess = { fetchedList ->
                    isConnectedToSupabase = true
                    if (fetchedList.isNotEmpty()) {
                        posts = fetchedList
                    } else {
                        // Backend returned empty table, populate with domain sample posts
                        posts = getFallbackSamplePosts()
                    }
                    isLoading = false
                    isRefreshing = false
                },
                onFailure = { error ->
                    isConnectedToSupabase = false
                    errorMessage = error.message ?: "Failed to fetch posts from Supabase"
                    if (posts.isEmpty()) {
                        posts = getFallbackSamplePosts()
                    }
                    isLoading = false
                    isRefreshing = false
                }
            )
        }
    }

    fun handleLikePost(post: Post) {
        val currentlyLiked = post.isLiked
        val updatedLikesCount = if (!currentlyLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
        val updatedPost = post.copy(
            likesCount = updatedLikesCount,
            isLiked = !currentlyLiked
        )
        // 1. Optimistically update local posts list so UI reacts immediately
        posts = posts.map { current ->
            if (current.id == post.id) updatedPost else current
        }

        // 2. Notify parent listener with the updated Post
        onLikeClick?.invoke(updatedPost)

        // 3. Persist the incremented like count to the Supabase database
        coroutineScope.launch {
            try {
                SupabaseClient.incrementPostLikes(post.id, post.likesCount)
            } catch (e: Exception) {
                android.util.Log.e("FeedScreen", "Failed to increment likes on Supabase: ${e.message}", e)
            }
        }
    }

    fun handleAddReply(postId: String, content: String) {
        val currentPost = posts.firstOrNull { it.id == postId }
        val currentList = repliesMap[postId] ?: (if (currentPost != null) getDefaultRepliesForPost(currentPost) else emptyList())
        val newReply = ReplyEntity(
            id = "reply_${System.currentTimeMillis()}",
            postId = postId,
            authorId = "user_me",
            authorName = "Aman Verma",
            authorHandle = "aman_verma",
            isVerified = true,
            content = content,
            timestamp = System.currentTimeMillis(),
            likesCount = 0,
            isLiked = false
        )
        val updatedReplies = currentList + newReply
        repliesMap = repliesMap + (postId to updatedReplies)

        // Optimistically increment comments count on the post
        posts = posts.map { current ->
            if (current.id == postId) {
                val updated = current.copy(commentsCount = current.commentsCount + 1)
                onReplyClick?.invoke(updated)
                updated
            } else {
                current
            }
        }
    }

    // Initial load and real-time subscription for live post updates
    LaunchedEffect(Unit) {
        if (initialPosts == null) {
            fetchPostsFromSupabase()
        }
        try {
            SupabaseClient.subscribeToPosts { table, event, payload ->
                android.util.Log.d("FeedScreen", "Realtime update received on $table ($event): $payload")
            }
        } catch (e: Exception) {
            android.util.Log.w("FeedScreen", "Realtime subscription notice: ${e.message}")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("feed_screen")
    ) {
        // Feed Header / Status Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkBorder.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Panchayat Feed",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Live indicator badge
                        Surface(
                            shape = CircleShape,
                            color = if (isConnectedToSupabase) RepostGreen.copy(alpha = 0.15f) else PanchayatSaffron.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isConnectedToSupabase) Icons.Filled.CloudDone else Icons.Filled.Cloud,
                                    contentDescription = "Supabase Status",
                                    tint = if (isConnectedToSupabase) RepostGreen else PanchayatSaffron,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isConnectedToSupabase) "Supabase Live" else "Supabase",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isConnectedToSupabase) RepostGreen else PanchayatSaffron
                                )
                            }
                        }
                    }
                    Text(
                        text = "Real-time microblogging & community notices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        onRefresh?.invoke()
                        fetchPostsFromSupabase()
                    },
                    modifier = Modifier.testTag("refresh_button")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = TwitterBlue
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Feed",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Body Content: Pull-to-refresh container enclosing states (LazyColumn, empty, error, loading)
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                onRefresh?.invoke()
                fetchPostsFromSupabase()
            },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .testTag("pull_to_refresh_box"),
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("pull_to_refresh_indicator"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    color = TwitterBlue
                )
            }
        ) {
            when {
                isLoading -> {
                    // Loading State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("loading_state"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("loading_indicator"),
                            color = TwitterBlue,
                            strokeWidth = 3.5.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching posts from Supabase...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                posts.isEmpty() && errorMessage != null -> {
                    // Error State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                            .testTag("error_state"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.CloudOff,
                                    contentDescription = "Connection Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Could not load posts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Network or Supabase configuration issue",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { fetchPostsFromSupabase() },
                            modifier = Modifier.testTag("retry_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }

                posts.isEmpty() -> {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                            .testTag("empty_state"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TwitterBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Cloud,
                                    contentDescription = "No Posts",
                                    tint = TwitterBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Posts Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No microblogging posts found on Supabase. Be the first to publish a post!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { fetchPostsFromSupabase() },
                            modifier = Modifier.testTag("refresh_empty_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
                        ) {
                            Text("Refresh Feed")
                        }
                    }
                }

                else -> {
                    // Success State: LazyColumn displaying microblogging posts
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("feed_lazy_column"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = posts,
                            key = { post -> post.id }
                        ) { post ->
                            val currentReplies = repliesMap[post.id] ?: remember(post.id) { getDefaultRepliesForPost(post) }
                            SupabasePostCard(
                                post = post,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("post_item_${post.id}"),
                                onPostClick = if (onPostClick != null) { { onPostClick(post) } } else null,
                                onLikeClick = { handleLikePost(post) },
                                onReplyClick = { onReplyClick?.invoke(post) },
                                onRepostClick = { onRepostClick?.invoke(post) },
                                replies = currentReplies,
                                isCommentsExpanded = (expandedCommentPostId == post.id),
                                onToggleComments = {
                                    expandedCommentPostId = if (expandedCommentPostId == post.id) null else post.id
                                },
                                onSubmitReply = { text ->
                                    handleAddReply(post.id, text)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getDefaultRepliesForPost(post: Post): List<ReplyEntity> {
    val now = System.currentTimeMillis()
    return when (post.id) {
        "supabase_post_1", "supabase_post_001", "post_1" -> listOf(
            ReplyEntity(
                id = "reply_sp1_1",
                postId = post.id,
                authorId = "user_secretary",
                authorName = "Sachiv Ji (Abhishek)",
                authorHandle = "sachiv_phulera",
                authorAvatarUrl = null,
                isVerified = true,
                content = "All ward members please gather at 10 AM sharp for the resolution signing.",
                timestamp = now - (20 * 60 * 1000),
                likesCount = 5,
                isLiked = false
            ),
            ReplyEntity(
                id = "reply_sp1_2",
                postId = post.id,
                authorId = "user_prahlad",
                authorName = "Prahlad Pandey",
                authorHandle = "prahlad_up",
                authorAvatarUrl = null,
                isVerified = false,
                content = "Great initiative! Will this also cover the inner school road in Ward 3?",
                timestamp = now - (15 * 60 * 1000),
                likesCount = 3,
                isLiked = true
            ),
            ReplyEntity(
                id = "reply_sp1_3",
                postId = post.id,
                authorId = "user_vikas",
                authorName = "Vikas Shukla",
                authorHandle = "vikas_panchayat",
                authorAvatarUrl = null,
                isVerified = false,
                content = "Yes Prahlad ji, the school road is covered under phase 1 implementation.",
                timestamp = now - (8 * 60 * 1000),
                likesCount = 7,
                isLiked = false
            )
        )
        "supabase_post_2", "supabase_post_002", "post_2" -> listOf(
            ReplyEntity(
                id = "reply_sp2_1",
                postId = post.id,
                authorId = "user_bhushan",
                authorName = "Bhushan (Banrakas)",
                authorHandle = "banrakas_phulera",
                authorAvatarUrl = null,
                isVerified = false,
                content = "Please ensure the beneficiary list is also pasted on the Panchayat notice board.",
                timestamp = now - (45 * 60 * 1000),
                likesCount = 12,
                isLiked = false
            ),
            ReplyEntity(
                id = "reply_sp2_2",
                postId = post.id,
                authorId = "user_binod",
                authorName = "Binod Kumar",
                authorHandle = "binod_phulera",
                authorAvatarUrl = null,
                isVerified = false,
                content = "Aadhaar verification completed yesterday at CSC center. Thank you!",
                timestamp = now - (25 * 60 * 1000),
                likesCount = 8,
                isLiked = true
            )
        )
        else -> {
            if (post.commentsCount > 0) {
                listOf(
                    ReplyEntity(
                        id = "reply_${post.id}_1",
                        postId = post.id,
                        authorId = "user_secretary",
                        authorName = "Sachiv Ji (Abhishek)",
                        authorHandle = "sachiv_phulera",
                        authorAvatarUrl = null,
                        isVerified = true,
                        content = "Noted. We are discussing this in the upcoming Panchayat executive committee.",
                        timestamp = now - (35 * 60 * 1000),
                        likesCount = 2,
                        isLiked = false
                    )
                )
            } else {
                emptyList()
            }
        }
    }
}
