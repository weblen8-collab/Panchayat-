package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panchayat.ui.FeedTab
import com.example.panchayat.ui.NavigationItem
import com.example.panchayat.ui.PanchayatViewModel
import com.example.panchayat.ui.components.ComposePostSheet
import com.example.panchayat.ui.components.PanchayatBottomNav
import com.example.panchayat.ui.components.PanchayatTopBar
import com.example.panchayat.ui.components.PostDetailSheet
import com.example.panchayat.ui.components.SupabaseSettingsSheet
import com.example.panchayat.ui.screens.ExploreScreen
import com.example.panchayat.ui.screens.HomeScreen
import com.example.panchayat.ui.screens.NotificationsScreen
import com.example.panchayat.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.RepostGreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PanchayatApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanchayatApp(viewModel: PanchayatViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentScreen by viewModel.currentScreen.collectAsState()
    val feedTab by viewModel.feedTab.collectAsState()
    val allPosts by viewModel.allPosts.collectAsState()
    val civicPosts by viewModel.civicPosts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val suggestedUsers by viewModel.suggestedUsers.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifCount by viewModel.unreadNotifCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    val authState by viewModel.authState.collectAsState()

    val selectedPost by viewModel.selectedPost.collectAsState()
    val postReplies by viewModel.postReplies.collectAsState()

    val isComposeOpen by viewModel.isComposeOpen.collectAsState()
    val isSupabaseSheetOpen by viewModel.isSupabaseSheetOpen.collectAsState()
    val isPostDetailOpen by viewModel.isPostDetailOpen.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()

    val composeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val supabaseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val postDetailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Clear sync message after 4 seconds
    LaunchedEffect(syncMessage) {
        if (syncMessage != null) {
            delay(4000)
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PanchayatTopBar(
                currentUser = currentUser,
                supabaseStatus = supabaseStatus,
                currentScreen = currentScreen,
                feedTab = feedTab,
                onFeedTabSelected = { viewModel.setFeedTab(it) },
                onAvatarClick = { viewModel.setScreen(NavigationItem.PROFILE) },
                onSupabaseClick = { viewModel.openSupabaseSheet() }
            )
        },
        bottomBar = {
            PanchayatBottomNav(
                currentScreen = currentScreen,
                unreadNotifCount = unreadNotifCount,
                onNavigate = { viewModel.setScreen(it) }
            )
        },
        floatingActionButton = {
            // Twitter/X Style Floating Compose Action Button
            if (currentScreen == NavigationItem.HOME || currentScreen == NavigationItem.EXPLORE) {
                FloatingActionButton(
                    onClick = { viewModel.openCompose() },
                    containerColor = PanchayatSaffron,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .testTag("fab_compose_panch")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "New Panch",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Panch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                NavigationItem.HOME -> {
                    HomeScreen(
                        feedTab = feedTab,
                        allPosts = allPosts,
                        civicPosts = civicPosts,
                        onPostClick = { viewModel.openPostDetail(it) },
                        onLikeClick = { viewModel.toggleLike(it) },
                        onRepostClick = { viewModel.toggleRepost(it) },
                        onBookmarkClick = { viewModel.toggleBookmark(it) },
                        onReplyClick = { viewModel.openPostDetail(it) },
                        onComposeClick = { viewModel.openCompose() }
                    )
                }

                NavigationItem.EXPLORE -> {
                    ExploreScreen(
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        suggestedUsers = suggestedUsers,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onPostClick = { viewModel.openPostDetail(it) },
                        onLikeClick = { viewModel.toggleLike(it) },
                        onRepostClick = { viewModel.toggleRepost(it) },
                        onBookmarkClick = { viewModel.toggleBookmark(it) },
                        onReplyClick = { viewModel.openPostDetail(it) },
                        onToggleFollow = { viewModel.toggleFollow(it) }
                    )
                }

                NavigationItem.NOTIFICATIONS -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onNotificationClick = { notif ->
                            val targetPost = allPosts.find { it.id == notif.postId }
                            if (targetPost != null) {
                                viewModel.openPostDetail(targetPost)
                            }
                        }
                    )
                }

                NavigationItem.PROFILE -> {
                    ProfileScreen(
                        user = currentUser,
                        allPosts = allPosts,
                        onPostClick = { viewModel.openPostDetail(it) },
                        onLikeClick = { viewModel.toggleLike(it) },
                        onRepostClick = { viewModel.toggleRepost(it) },
                        onBookmarkClick = { viewModel.toggleBookmark(it) },
                        onReplyClick = { viewModel.openPostDetail(it) },
                        onUpdateProfile = { name, bio, loc ->
                            viewModel.updateProfile(name, bio, loc)
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        },
                        authState = authState,
                        onSignIn = { email, pwd, cb ->
                            viewModel.signInWithEmail(email, pwd, cb)
                        },
                        onSignUp = { email, pwd, name, handle, cb ->
                            viewModel.signUpWithEmail(email, pwd, name, handle, cb)
                        },
                        onSignOut = { cb ->
                            viewModel.signOut(cb)
                        }
                    )
                }
            }

            // Sync Toast Alert Banner at Top
            AnimatedVisibility(
                visible = syncMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RepostGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = RepostGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = syncMessage ?: "",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Modal: Compose Post Sheet ("New Panch")
    if (isComposeOpen) {
        ComposePostSheet(
            currentUser = currentUser,
            onDismiss = { viewModel.closeCompose() },
            onSubmitPost = { content, category, mediaRes ->
                viewModel.createPost(content, category, mediaRes)
            },
            sheetState = composeSheetState
        )
    }

    // Modal: Post Detail & Community Discussion Thread
    if (isPostDetailOpen && selectedPost != null) {
        PostDetailSheet(
            post = selectedPost!!,
            replies = postReplies,
            onDismiss = { viewModel.closePostDetail() },
            onLikeClick = { viewModel.toggleLike(selectedPost!!.id) },
            onRepostClick = { viewModel.toggleRepost(selectedPost!!.id) },
            onBookmarkClick = { viewModel.toggleBookmark(selectedPost!!.id) },
            onSubmitReply = { content ->
                viewModel.addReply(selectedPost!!.id, content)
            },
            sheetState = postDetailSheetState
        )
    }

    // Modal: Supabase Settings & Live Sync
    if (isSupabaseSheetOpen) {
        val (currentUrl, currentKey) = viewModel.supabaseManager.getConfig()
        SupabaseSettingsSheet(
            supabaseStatus = supabaseStatus,
            initialUrl = currentUrl,
            initialAnonKey = currentKey,
            isSyncing = isSyncing,
            syncMessage = syncMessage,
            onSaveConfig = { url, key ->
                viewModel.saveSupabaseConfig(url, key)
            },
            onTestConnection = { callback ->
                viewModel.testSupabaseConnection(callback)
            },
            onSyncPosts = {
                viewModel.syncSupabasePosts()
            },
            onDismiss = { viewModel.closeSupabaseSheet() },
            sheetState = supabaseSheetState,
            onOpenSupabaseFeed = {
                viewModel.closeSupabaseSheet()
                viewModel.setScreen(NavigationItem.HOME)
                viewModel.setFeedTab(FeedTab.SUPABASE_FEED)
            }
        )
    }
}
