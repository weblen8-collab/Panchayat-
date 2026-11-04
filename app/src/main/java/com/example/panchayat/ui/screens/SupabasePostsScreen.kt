package com.example.panchayat.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.remote.SupabaseClient
import com.example.panchayat.ui.components.SupabasePostCard
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.TwitterBlue
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * A composable screen that displays a list of posts fetched directly from the Supabase 'posts' table.
 * Includes a rich card layout for each post showing the author, content, and metadata.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabasePostsScreen(
    modifier: Modifier = Modifier,
    initialPosts: List<Post>? = null,
    onNavigateBack: (() -> Unit)? = null,
    onOpenPostDetail: ((Post) -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var posts by remember { mutableStateOf<List<Post>>(initialPosts ?: emptyList()) }
    var isLoading by remember { mutableStateOf(initialPosts == null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isConnectedToSupabase by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isCreateDialogOpen by remember { mutableStateOf(false) }

    val categories = listOf("All", "Gram Sabha", "Civic Notice", "Agriculture", "Development", "General")

    // Function to fetch posts directly from Supabase 'posts' table
    fun loadSupabasePosts() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            val result = SupabaseClient.fetchPosts(limit = 50)
            result.fold(
                onSuccess = { fetchedList ->
                    isConnectedToSupabase = true
                    if (fetchedList.isNotEmpty()) {
                        posts = fetchedList
                    } else {
                        // Table is reachable but empty, seed sample posts if needed
                        posts = getFallbackSamplePosts()
                    }
                    isLoading = false
                },
                onFailure = { error ->
                    isConnectedToSupabase = false
                    errorMessage = error.message ?: "Failed to connect to Supabase 'posts' table"
                    // If network or credentials issue, show fallback sample posts so screen is fully interactive
                    if (posts.isEmpty()) {
                        posts = getFallbackSamplePosts()
                    }
                    isLoading = false
                }
            )
        }
    }

    // Initial fetch on launch
    LaunchedEffect(Unit) {
        if (initialPosts == null) {
            loadSupabasePosts()
        }
    }

    // Filter posts based on selected category and search query
    val filteredPosts = remember(posts, selectedCategory, searchQuery) {
        posts.filter { post ->
            val matchesCategory = (selectedCategory == "All" || post.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                post.content.contains(searchQuery, ignoreCase = true) ||
                post.authorName.contains(searchQuery, ignoreCase = true) ||
                post.authorHandle.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("supabase_posts_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Supabase Posts Feed",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (isConnectedToSupabase) RepostGreen.copy(alpha = 0.15f) else TwitterBlue.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isConnectedToSupabase) Icons.Default.CloudDone else Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = if (isConnectedToSupabase) RepostGreen else TwitterBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isConnectedToSupabase) "Live (posts)" else "posts table",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isConnectedToSupabase) RepostGreen else TwitterBlue
                                    )
                                }
                            }
                        }
                        Text(
                            text = "SELECT * FROM public.posts ORDER BY created_at DESC",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { loadSupabasePosts() },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("btn_refresh_supabase_posts")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Posts",
                                tint = TwitterBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreateDialogOpen = true },
                containerColor = PanchayatSaffron,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_supabase_post")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Supabase Post"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ==================== SEARCH & CATEGORY FILTERS ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = 8.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter posts by keyword, author, or #tag...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TwitterBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("supabase_posts_search_input")
                )

                // Category Chips Carousel
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TwitterBlue.copy(alpha = 0.15f),
                                selectedLabelColor = TwitterBlue
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) TwitterBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }

            // Info banner if offline or using fallback
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = PanchayatSaffron,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline / Local Mode Active",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Showing local posts matching 'posts' table schema. Tap refresh to retry connection.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        TextButton(onClick = { loadSupabasePosts() }) {
                            Text("Retry", color = TwitterBlue)
                        }
                    }
                }
            }

            // ==================== POSTS LIST ====================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading && posts.isEmpty()) {
                    // Loading State
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = TwitterBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Querying Supabase 'posts' table...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (filteredPosts.isEmpty()) {
                    // Empty State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = TwitterBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No posts found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != "All") {
                                "No posts match the current filter or search criteria."
                            } else {
                                "The Supabase 'posts' table has no entries yet. Add the first post!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { isCreateDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
                        ) {
                            Text("Insert Post to Supabase", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Populated Posts List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("supabase_posts_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Showing ${filteredPosts.size} posts from 'posts' table",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }

                        items(
                            items = filteredPosts,
                            key = { it.id }
                        ) { post ->
                            SupabasePostCard(
                                post = post,
                                onPostClick = { onOpenPostDetail?.invoke(post) },
                                onLikeClick = { postId ->
                                    val currentPost = posts.find { it.id == postId }
                                    val wasLiked = currentPost?.isLiked ?: false
                                    val currentLikes = currentPost?.likesCount ?: 0
                                    val newLiked = !wasLiked
                                    val newLikes = if (newLiked) currentLikes + 1 else maxOf(0, currentLikes - 1)
                                    // Optimistic update
                                    posts = posts.map { p ->
                                        if (p.id == postId) p.copy(likesCount = newLikes, isLiked = newLiked) else p
                                    }
                                    coroutineScope.launch {
                                        SupabaseClient.updatePostLikes(postId, newLikes)
                                    }
                                    Toast.makeText(
                                        context,
                                        if (newLiked) "Liked post on Supabase" else "Unliked post on Supabase",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onRepostClick = { postId ->
                                    posts = posts.map { p ->
                                        if (p.id == postId) p.copy(repostsCount = p.repostsCount + 1) else p
                                    }
                                    Toast.makeText(context, "Reposted post", Toast.LENGTH_SHORT).show()
                                },
                                onReplyClick = { p ->
                                    onOpenPostDetail?.invoke(p)
                                }
                            )
                        }

                        // Bottom Spacer for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // ==================== CREATE POST DIALOG ====================
    if (isCreateDialogOpen) {
        CreateSupabasePostDialog(
            onDismiss = { isCreateDialogOpen = false },
            onSubmit = { newPost ->
                coroutineScope.launch {
                    val insertResult = SupabaseClient.insertPost(newPost)
                    if (insertResult.isSuccessful()) {
                        Toast.makeText(context, "Post published to Supabase!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Saved post locally (Supabase unconfigured)", Toast.LENGTH_SHORT).show()
                    }
                    posts = listOf(newPost) + posts
                    isCreateDialogOpen = false
                }
            }
        )
    }
}

/**
 * Dialog allowing user to compose and insert a post directly into the Supabase 'posts' table.
 */
@Composable
fun CreateSupabasePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (Post) -> Unit
) {
    var authorName by remember { mutableStateOf("Abhishek Tripathi") }
    var authorHandle by remember { mutableStateOf("sachiv_ji") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Gram Sabha") }
    val categories = listOf("Gram Sabha", "Civic Notice", "Agriculture", "Development", "General")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = TwitterBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Post to Supabase",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Inserts a new row into the 'posts' table via REST API.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Author Name Field
                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("Author Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Author Handle Field
                OutlinedTextField(
                    value = authorHandle,
                    onValueChange = { authorHandle = it },
                    label = { Text("Handle (@username)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Post Content Field
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Post Content") },
                    placeholder = { Text("What's happening in your village or community?") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector
                Text(
                    text = "Category:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (content.isNotBlank()) {
                                val newPost = Post(
                                    id = UUID.randomUUID().toString(),
                                    authorId = "user_${authorHandle.replace("@", "")}",
                                    authorName = authorName.ifBlank { "Panchayat Member" },
                                    authorHandle = authorHandle.removePrefix("@"),
                                    content = content.trim(),
                                    category = category,
                                    isVerified = true,
                                    timestamp = System.currentTimeMillis(),
                                    likesCount = 0,
                                    commentsCount = 0,
                                    repostsCount = 0,
                                    viewsCount = 1
                                )
                                onSubmit(newPost)
                            }
                        },
                        enabled = content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue)
                    ) {
                        Text("Post to Supabase", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Fallback sample posts reflecting the Supabase 'posts' table schema.
 */
fun getFallbackSamplePosts(): List<Post> {
    val now = System.currentTimeMillis()
    return listOf(
        Post(
            id = "supabase_post_001",
            authorId = "author_manju_devi",
            authorName = "Manju Devi",
            authorHandle = "manju_devi",
            isVerified = true,
            content = "Gram Sabha announcement: Solar street light installation begins this Monday across wards 1-4. Feedback or requests can be shared at the Panchayat office. #GramSabha #PhuleraProgress",
            category = "Gram Sabha",
            likesCount = 48,
            commentsCount = 12,
            repostsCount = 9,
            viewsCount = 540,
            timestamp = now - (35 * 60 * 1000) // 35 minutes ago
        ),
        Post(
            id = "supabase_post_002",
            authorId = "author_abhishek",
            authorName = "Abhishek Tripathi",
            authorHandle = "sachiv_ji",
            isVerified = true,
            content = "PM-Kisan 17th installment disbursement list is now updated on the community portal. All eligible farmers please verify your Aadhaar seeding. #Agriculture #PMKisan",
            category = "Agriculture",
            likesCount = 84,
            commentsCount = 21,
            repostsCount = 34,
            viewsCount = 1280,
            timestamp = now - (2 * 3600 * 1000) // 2 hours ago
        ),
        Post(
            id = "supabase_post_003",
            authorId = "author_prahlad",
            authorName = "Prahlad Pandey",
            authorHandle = "prahlad_up",
            isVerified = false,
            content = "The new pond desilting project under Jal Jeevan Mission has reached 80% completion before monsoon. Water tables are already improving! #JalJeevan #WaterConservation",
            category = "Civic Notice",
            likesCount = 32,
            commentsCount = 6,
            repostsCount = 4,
            viewsCount = 390,
            timestamp = now - (5 * 3600 * 1000) // 5 hours ago
        ),
        Post(
            id = "supabase_post_004",
            authorId = "author_bikas",
            authorName = "Vikas Kumar",
            authorHandle = "vikas_office",
            isVerified = false,
            content = "Panchayat Bhavan WiFi router has been upgraded. High-speed internet is available free of charge for students and civil service aspirants every evening from 5 PM to 8 PM.",
            category = "Development",
            likesCount = 61,
            commentsCount = 15,
            repostsCount = 11,
            viewsCount = 890,
            timestamp = now - (18 * 3600 * 1000) // 18 hours ago
        )
    )
}

// Extension helper for Result
private fun <T> Result<T>.isSuccessful(): Boolean = isSuccess
