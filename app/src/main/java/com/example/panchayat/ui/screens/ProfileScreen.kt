package com.example.panchayat.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.model.UserProfile
import com.example.panchayat.data.remote.SupabaseClient
import com.example.panchayat.data.remote.auth.AuthState
import com.example.panchayat.data.repository.SeedData
import com.example.panchayat.ui.components.PanchayatAvatar
import com.example.panchayat.ui.components.PanchayatPostCard
import com.example.panchayat.ui.components.SupabaseAuthDialog
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.TwitterBlue
import kotlinx.coroutines.launch

private const val TAG = "ProfileScreen"

/**
 * Profile screen that fetches and displays the current user's information and a list
 * of their past microblog posts from Supabase.
 *
 * Features:
 * - Fetches user profile info (name, handle, bio, location, stats) from Supabase 'profiles' table.
 * - Fetches past microblog posts created by the user from Supabase 'posts' table.
 * - Real-time subscription to Supabase Realtime WebSocket for live post updates.
 * - Reactive states: Loading, Error with Retry, Empty state, and Pull-to-refresh reload.
 * - Profile and bio editing synchronized with the Supabase backend.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserProfileEntity?,
    allPosts: List<PostEntity> = emptyList(),
    client: SupabaseClient = SupabaseClient.default,
    autoFetch: Boolean = true,
    enableRealtime: Boolean = true,
    onPostClick: (PostEntity) -> Unit = {},
    onLikeClick: (String) -> Unit = {},
    onRepostClick: (String) -> Unit = {},
    onBookmarkClick: (String) -> Unit = {},
    onReplyClick: (PostEntity) -> Unit = {},
    onUpdateProfile: (String, String, String) -> Unit = { _, _, _ -> },
    onUpdateBio: ((String) -> Unit)? = null,
    authState: AuthState = AuthState.Unauthenticated,
    onSignIn: (email: String, password: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _ -> },
    onSignUp: (email: String, password: String, name: String, handle: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onSignOut: (() -> Unit) -> Unit = { it() },
    onPostsLoaded: ((List<Post>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentUserProfile by remember(user) { mutableStateOf(user ?: SeedData.currentUser) }
    var fetchedPosts by remember { mutableStateOf<List<PostEntity>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isEditProfileDialogOpen by remember { mutableStateOf(false) }
    var isAuthDialogOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val activeUser = currentUserProfile

    // Filter fallback user posts from allPosts
    val fallbackUserPosts = allPosts.filter { post ->
        val userId = activeUser?.id
        val userHandle = activeUser?.handle
        val userName = activeUser?.name
        post.authorId == (userId ?: "user_me") ||
        post.authorId == "user_me" ||
        (userHandle?.isNotBlank() == true && post.authorHandle.equals(userHandle, ignoreCase = true)) ||
        (userName?.isNotBlank() == true && post.authorName.equals(userName, ignoreCase = true))
    }

    // Effective user microblog posts (prefer fetched from Supabase, fallback to provided list)
    val effectiveUserPosts = if (fetchedPosts.isNotEmpty()) {
        fetchedPosts
    } else {
        fallbackUserPosts
    }

    val userLikes = allPosts.filter { it.isLiked }

    val displayedPosts = when (selectedTab) {
        0 -> effectiveUserPosts
        1 -> allPosts.filter { it.repliesCount > 0 && (it.authorId == (activeUser?.id ?: "user_me") || it.authorId == "user_me") }
        else -> userLikes
    }

    // Function to fetch current user's profile and past microblog posts from Supabase
    suspend fun refreshProfileAndPosts() {
        if (effectiveUserPosts.isEmpty() && fallbackUserPosts.isEmpty()) {
            isLoading = true
        }
        errorMessage = null
        try {
            // 1. Fetch user's information from Supabase
            val profileRes = client.fetchUserProfile(activeUser?.id ?: "user_me")
            if (profileRes.isSuccess && profileRes.getOrNull() != null) {
                val fetchedProfile = profileRes.getOrNull()!!
                currentUserProfile = fetchedProfile.toEntity(isCurrentUser = true)
                Log.d(TAG, "Successfully fetched profile from Supabase for ${fetchedProfile.name}")
            }

            // 2. Fetch user's past microblog posts from Supabase
            val postsRes = client.fetchUserPosts(
                authorId = activeUser?.id,
                authorHandle = activeUser?.handle
            )
            if (postsRes.isSuccess) {
                val posts = postsRes.getOrNull() ?: emptyList()
                if (posts.isNotEmpty()) {
                    fetchedPosts = posts.map { it.toEntity() }
                    onPostsLoaded?.invoke(posts)
                    Log.d(TAG, "Successfully fetched ${posts.size} user posts from Supabase")
                }
            } else {
                Log.w(TAG, "Supabase fetchUserPosts returned error: ${postsRes.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load profile and posts from Supabase: ${e.message}", e)
            errorMessage = e.localizedMessage ?: "Failed to connect to Supabase"
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    // Automatic fetching from Supabase on launch
    LaunchedEffect(client, activeUser?.id, autoFetch) {
        if (autoFetch) {
            refreshProfileAndPosts()
        }
    }

    // Real-time subscription to Supabase Realtime WebSocket for live post updates
    if (enableRealtime) {
        DisposableEffect(client, activeUser?.id) {
            val channel = client.subscribeToPosts { table, eventType, _ ->
                if (table == "posts") {
                    when (eventType) {
                        "INSERT", "UPDATE", "DELETE" -> {
                            coroutineScope.launch {
                                val postsRes = client.fetchUserPosts(
                                    authorId = activeUser?.id,
                                    authorHandle = activeUser?.handle
                                )
                                if (postsRes.isSuccess) {
                                    val posts = postsRes.getOrNull() ?: emptyList()
                                    if (posts.isNotEmpty()) {
                                        fetchedPosts = posts.map { it.toEntity() }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            onDispose {
                channel.unsubscribe()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    refreshProfileAndPosts()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_pull_to_refresh")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("profile_screen_scroll")
            ) {
                // Header Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.panchayat_banner),
                            contentDescription = "Profile Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Avatar Overlap & Action Buttons Row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Overlapping Avatar
                        Box(
                            modifier = Modifier
                                .offset(y = (-36).dp)
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                        ) {
                            PanchayatAvatar(
                                name = activeUser?.name ?: "User",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("profile_avatar")
                            )
                        }

                        // Account / Auth and Edit Profile Buttons
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isAuthDialogOpen = true },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("auth_account_button")
                            ) {
                                val authLabel = if (authState is AuthState.Authenticated) "Account" else "Sign In"
                                Text(
                                    text = authLabel,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (authState is AuthState.Authenticated) RepostGreen else PanchayatSaffron
                                )
                            }

                            OutlinedButton(
                                onClick = { isEditProfileDialogOpen = true },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("edit_profile_button")
                            ) {
                                Text(
                                    text = "Edit profile",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    // Profile Details Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeUser?.name ?: "Aman Verma",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.testTag("profile_user_name")
                            )
                            if (activeUser?.isVerified == true) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Member",
                                    tint = TwitterBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "@${activeUser?.handle ?: "aman_verma"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("profile_user_handle")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = activeUser?.bio ?: "Rural tech enthusiast & community volunteer. Exploring grassroots democracy & digital village initiatives in Panchayat 🇮🇳",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 20.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_user_bio")
                            )
                            IconButton(
                                onClick = { isEditProfileDialogOpen = true },
                                modifier = Modifier.testTag("edit_bio_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Bio",
                                    tint = PanchayatSaffron,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Location & Joined Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeUser?.location ?: "Phulera, Ballia",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.testTag("profile_user_location")
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeUser?.joinedDate ?: "Joined September 2024",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.testTag("profile_joined_date")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Following, Followers & Posts Count with inline Supabase badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${activeUser?.followingCount ?: 142}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.testTag("profile_following_count")
                                    )
                                    Text(
                                        text = "Following",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${activeUser?.followersCount ?: 389}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.testTag("profile_followers_count")
                                    )
                                    Text(
                                        text = "Followers",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${effectiveUserPosts.size}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.testTag("profile_posts_count")
                                    )
                                    Text(
                                        text = "Posts",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Supabase cloud sync badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.testTag("profile_supabase_sync_badge")
                            ) {
                                Icon(
                                    imageVector = if (client.isConfigured) Icons.Default.CloudDone else Icons.Default.Cloud,
                                    contentDescription = "Supabase Status",
                                    tint = if (client.isConfigured) RepostGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (client.isConfigured) "Supabase" else "Local",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (client.isConfigured) RepostGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Profile Tabs (Posts, Replies, Likes)
                    val tabs = listOf("Posts", "Replies", "Likes")
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("profile_tabs"),
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = PanchayatSaffron,
                                    height = 3.dp
                                )
                            }
                        },
                        divider = {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            val testTag = when (index) {
                                0 -> "profile_tab_posts"
                                1 -> "profile_tab_replies"
                                else -> "profile_tab_likes"
                            }
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.testTag(testTag)
                            )
                        }
                    }
                }

                // Loading State Indicator
                if (isLoading && effectiveUserPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp)
                                .testTag("profile_loading"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = PanchayatSaffron
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Loading past posts from Supabase...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Error State Card with Retry
                if (errorMessage != null && effectiveUserPosts.isEmpty() && !isLoading) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .testTag("profile_error"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Unable to fetch microblog posts from Supabase",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = errorMessage ?: "Check your connection and try again.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { coroutineScope.launch { refreshProfileAndPosts() } },
                                    modifier = Modifier.testTag("profile_retry_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                // Posts or Empty State
                if (displayedPosts.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp)
                                .testTag("profile_empty_posts"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "You haven't posted any Panches yet."
                                        1 -> "No reply threads found."
                                        else -> "You haven't liked any Panches yet."
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                if (selectedTab == 0 && client.isConfigured) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Your published microblog posts will appear here once saved to Supabase.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(displayedPosts, key = { it.id }) { post ->
                        Box(modifier = Modifier.testTag("user_post_${post.id}")) {
                            PanchayatPostCard(
                                post = post,
                                onPostClick = { onPostClick(post) },
                                onLikeClick = { onLikeClick(post.id) },
                                onRepostClick = { onRepostClick(post.id) },
                                onBookmarkClick = { onBookmarkClick(post.id) },
                                onReplyClick = { onReplyClick(post) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }

        // Supabase Auth Dialog
        if (isAuthDialogOpen) {
            SupabaseAuthDialog(
                authState = authState,
                onDismiss = { isAuthDialogOpen = false },
                onSignIn = onSignIn,
                onSignUp = onSignUp,
                onSignOut = onSignOut
            )
        }

        // Edit Profile Dialog
        if (isEditProfileDialogOpen) {
            EditProfileDialog(
                currentName = activeUser?.name ?: "",
                currentBio = activeUser?.bio ?: "",
                currentLocation = activeUser?.location ?: "",
                onDismiss = { isEditProfileDialogOpen = false },
                onSave = { name, bio, loc ->
                    val updatedName = name.ifBlank { activeUser?.name ?: "User" }
                    val updatedBio = bio
                    val updatedLocation = loc
                    val updatedEntity = activeUser?.copy(
                        name = updatedName,
                        bio = updatedBio,
                        location = updatedLocation
                    ) ?: UserProfileEntity(
                        id = "user_me",
                        name = updatedName,
                        handle = "user_me",
                        username = "user_me",
                        bio = updatedBio,
                        location = updatedLocation
                    )
                    currentUserProfile = updatedEntity
                    onUpdateProfile(updatedName, updatedBio, updatedLocation)
                    onUpdateBio?.invoke(updatedBio)
                    isEditProfileDialogOpen = false

                    // Persist updated profile directly to Supabase backend
                    coroutineScope.launch {
                        try {
                            client.upsertUserProfile(UserProfile.fromEntity(updatedEntity))
                            Log.d(TAG, "Successfully synced updated profile to Supabase")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to sync updated profile to Supabase: ${e.message}")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentBio: String,
    currentLocation: String,
    onDismiss: () -> Unit,
    onSave: (name: String, bio: String, location: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }
    var location by remember { mutableStateOf(currentLocation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("edit_profile_dialog"),
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_name_field")
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_bio_field")
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Village / Location") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_location_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = name.ifBlank { currentName.ifBlank { "User" } }
                    onSave(finalName.trim(), bio.trim(), location.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = PanchayatSaffron),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_edit_profile_button")
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
