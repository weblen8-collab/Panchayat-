package com.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.remote.SupabaseClient
import com.example.panchayat.ui.components.PostFeed as InternalPostFeed

/**
 * Public composable that fetches and displays a list of microblogging posts
 * from the Supabase backend using [androidx.compose.foundation.lazy.LazyColumn].
 */
@Composable
fun PostFeed(
    modifier: Modifier = Modifier,
    client: SupabaseClient = SupabaseClient.default,
    initialPosts: List<Post>? = null,
    autoFetch: Boolean = true,
    enableRealtime: Boolean = true,
    enablePullToRefresh: Boolean = true,
    categoryFilter: String? = null,
    searchQuery: String? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(10.dp),
    lazyListState: LazyListState = rememberLazyListState(),
    onPostClick: ((Post) -> Unit)? = null,
    onLikeClick: ((Post) -> Unit)? = null,
    onReplyClick: ((Post) -> Unit)? = null,
    onRepostClick: ((Post) -> Unit)? = null,
    onShareClick: ((Post) -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    onPostsLoaded: ((List<Post>) -> Unit)? = null
) {
    InternalPostFeed(
        modifier = modifier,
        client = client,
        initialPosts = initialPosts,
        autoFetch = autoFetch,
        enableRealtime = enableRealtime,
        enablePullToRefresh = enablePullToRefresh,
        categoryFilter = categoryFilter,
        searchQuery = searchQuery,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        lazyListState = lazyListState,
        onPostClick = onPostClick,
        onLikeClick = onLikeClick,
        onReplyClick = onReplyClick,
        onRepostClick = onRepostClick,
        onShareClick = onShareClick,
        onRefresh = onRefresh,
        onPostsLoaded = onPostsLoaded
    )
}
