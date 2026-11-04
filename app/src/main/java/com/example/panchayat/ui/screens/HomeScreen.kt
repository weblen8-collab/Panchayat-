package com.example.panchayat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.ui.FeedTab
import com.example.panchayat.ui.components.PanchayatPostCard
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.TwitterBlue

@Composable
fun HomeScreen(
    feedTab: FeedTab,
    allPosts: List<PostEntity>,
    civicPosts: List<PostEntity>,
    onPostClick: (PostEntity) -> Unit,
    onLikeClick: (String) -> Unit,
    onRepostClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onReplyClick: (PostEntity) -> Unit,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (feedTab == FeedTab.SUPABASE_FEED) {
        FeedScreen(
            modifier = modifier,
            onPostClick = { post ->
                onPostClick(post.toEntity())
            },
            onLikeClick = { post ->
                onLikeClick(post.id)
            },
            onReplyClick = { post ->
                onReplyClick(post.toEntity())
            },
            onRepostClick = { post ->
                onRepostClick(post.id)
            }
        )
        return
    }

    val displayedPosts = when (feedTab) {
        FeedTab.FOR_YOU -> allPosts
        FeedTab.FOLLOWING -> allPosts.filter { it.authorId != "user_me" }
        FeedTab.CIVIC_PANCHAYAT -> civicPosts
        FeedTab.SUPABASE_FEED -> allPosts // unreachable due to early return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (displayedPosts.isEmpty()) {
            EmptyFeedState(
                feedTab = feedTab,
                onComposeClick = onComposeClick,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_posts_list")
            ) {
                items(
                    items = displayedPosts,
                    key = { it.id }
                ) { post ->
                    PanchayatPostCard(
                        post = post,
                        onPostClick = { onPostClick(post) },
                        onLikeClick = { onLikeClick(post.id) },
                        onRepostClick = { onRepostClick(post.id) },
                        onBookmarkClick = { onBookmarkClick(post.id) },
                        onReplyClick = { onReplyClick(post) }
                    )
                }
                // Padding for bottom nav & FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyFeedState(
    feedTab: FeedTab,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (feedTab == FeedTab.CIVIC_PANCHAYAT) Icons.Default.Campaign else Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            tint = if (feedTab == FeedTab.CIVIC_PANCHAYAT) PanchayatSaffron else TwitterBlue,
            modifier = Modifier.height(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (feedTab) {
                FeedTab.FOR_YOU -> "No Panches yet"
                FeedTab.FOLLOWING -> "No posts from followed members"
                FeedTab.CIVIC_PANCHAYAT -> "No official Gram Sabha notices"
                FeedTab.SUPABASE_FEED -> "No Supabase posts found"
            },
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (feedTab) {
                FeedTab.FOR_YOU -> "Be the first to share an update or issue from your village."
                FeedTab.FOLLOWING -> "Explore community members and official leaders to follow their updates."
                FeedTab.CIVIC_PANCHAYAT -> "Stay tuned for verified Gram Panchayat resolutions, meetings, and project announcements."
                FeedTab.SUPABASE_FEED -> "The Supabase 'posts' table has no entries yet."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onComposeClick,
            colors = ButtonDefaults.buttonColors(containerColor = PanchayatSaffron)
        ) {
            Text("Write a Panch", fontWeight = FontWeight.Bold)
        }
    }
}
