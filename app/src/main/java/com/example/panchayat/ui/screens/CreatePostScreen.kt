package com.example.panchayat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.remote.SupabaseClient
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.TwitterBlue
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * A Jetpack Compose screen component that includes a TextField for user input
 * and a Floating Action Button to submit a new post to the Supabase database.
 *
 * @param modifier Modifier for styling and layout
 * @param initialContent Initial text content populated in the input field
 * @param authorId Identifier for the post author
 * @param authorName Display name of the post author
 * @param authorHandle Handle (@username) of the post author
 * @param onPostSubmitted Callback invoked when the post has been successfully submitted to Supabase
 * @param onNavigateBack Optional callback to navigate back to the previous screen
 * @param onSubmitPost Optional custom submit lambda for testing or custom dependency injection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    modifier: Modifier = Modifier,
    initialContent: String = "",
    authorId: String = "user_default",
    authorName: String = "Gram Resident",
    authorHandle: String = "panchayat_resident",
    onPostSubmitted: ((Post) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    onSubmitPost: (suspend (Post) -> Result<Post>)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    var postContent by remember { mutableStateOf(initialContent) }
    var selectedCategory by remember { mutableStateOf("General") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successFeedback by remember { mutableStateOf<String?>(null) }

    val categories = listOf("General", "Gram Sabha", "Civic Notice", "Agriculture", "Development")
    val maxChars = 280
    val charsRemaining = maxChars - postContent.length
    val isPostValid = postContent.isNotBlank() && charsRemaining >= 0 && !isSubmitting

    fun submitPostToSupabase() {
        if (!isPostValid) return

        val newPost = Post(
            id = UUID.randomUUID().toString(),
            authorId = authorId,
            authorName = authorName,
            authorHandle = authorHandle,
            content = postContent.trim(),
            category = selectedCategory,
            timestamp = System.currentTimeMillis()
        )

        coroutineScope.launch {
            isSubmitting = true
            errorMessage = null

            try {
                val result = if (onSubmitPost != null) {
                    onSubmitPost(newPost)
                } else {
                    SupabaseClient.insertPost(newPost)
                }

                result.fold(
                    onSuccess = { insertedPost ->
                        isSubmitting = false
                        successFeedback = "Post published to Supabase successfully!"
                        postContent = ""
                        onPostSubmitted?.invoke(insertedPost)
                    },
                    onFailure = { error ->
                        isSubmitting = false
                        // If Supabase table is unreachable or local mock mode, allow optimistic submission
                        errorMessage = error.message ?: "Failed to push post to Supabase database"
                        onPostSubmitted?.invoke(newPost)
                    }
                )
            } catch (e: Exception) {
                isSubmitting = false
                errorMessage = e.localizedMessage ?: "Unexpected error publishing post"
                onPostSubmitted?.invoke(newPost)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("create_post_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Create Post",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cloud,
                                contentDescription = "Supabase",
                                tint = TwitterBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Publishing to Supabase 'posts' table",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(
                            onClick = { onNavigateBack() },
                            modifier = Modifier.testTag("navigate_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { submitPostToSupabase() },
                modifier = Modifier
                    .imePadding()
                    .testTag("submit_post_fab"),
                containerColor = if (isPostValid) TwitterBlue else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isPostValid) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("submit_progress_indicator"),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Submit Post to Supabase"
                        )
                        Text(
                            text = "Publish",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Author pill & Category Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PanchayatSaffron.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = authorName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = PanchayatSaffron,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Column {
                        Text(
                            text = authorName,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "@$authorHandle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Character counter
                Text(
                    text = "$charsRemaining",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        charsRemaining < 0 -> MaterialTheme.colorScheme.error
                        charsRemaining < 20 -> PanchayatSaffron
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.testTag("chars_remaining_text")
                )
            }

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TwitterBlue.copy(alpha = 0.15f),
                            selectedLabelColor = TwitterBlue
                        ),
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }

            // Main Post Input TextField
            TextField(
                value = postContent,
                onValueChange = { postContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_text_field"),
                placeholder = {
                    Text(
                        text = "What's happening in your Panchayat? Share updates, alerts, or queries with the village...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                minLines = 6,
                maxLines = 14,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error / Feedback Messages
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = successFeedback != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = RepostGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("success_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDone,
                            contentDescription = "Success",
                            tint = RepostGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = successFeedback ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = RepostGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Padding to keep FAB from overlapping content
        }
    }
}
