package com.example.panchayat.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.panchayat.data.local.entity.ReplyEntity
import com.example.panchayat.data.model.Post
import com.example.ui.theme.CivicAmber
import com.example.ui.theme.LikePink
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.TwitterBlue

/**
 * Card layout representing an individual post fetched from the Supabase 'posts' table.
 * Displays:
 * 1. Author: Avatar, Name, @Handle, Verified Status, Category Badge
 * 2. Content: Rich post body with syntax highlighted hashtags/mentions and optional media preview
 * 3. Metadata: Timestamp, Like/Reply/Repost counts, Impression views, and Supabase live table indicator.
 */
@Composable
fun SupabasePostCard(
    post: Post,
    modifier: Modifier = Modifier,
    onPostClick: (() -> Unit)? = null,
    onLikeClick: ((String) -> Unit)? = null,
    onReplyClick: ((Post) -> Unit)? = null,
    onRepostClick: ((String) -> Unit)? = null,
    isLikedInitial: Boolean = post.isLiked,
    isRepostedInitial: Boolean = false,
    replies: List<ReplyEntity> = emptyList(),
    isCommentsExpanded: Boolean = false,
    onToggleComments: (() -> Unit)? = null,
    onSubmitReply: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var isLiked by remember(post.id, post.isLiked, isLikedInitial) { mutableStateOf(isLikedInitial || post.isLiked) }
    var likesCount by remember(post.id, post.likesCount) { mutableStateOf(post.likesCount) }
    var isReposted by remember(post.id, isRepostedInitial) { mutableStateOf(isRepostedInitial) }
    var repostsCount by remember(post.id, post.repostsCount) { mutableStateOf(post.repostsCount) }

    var internalCommentsExpanded by remember(post.id, isCommentsExpanded) { mutableStateOf(isCommentsExpanded) }
    var localReplies by remember(post.id, replies) { mutableStateOf(replies) }
    var commentsCount by remember(post.id, post.commentsCount) { mutableStateOf(post.commentsCount) }
    val effectiveCommentsExpanded = if (onToggleComments != null) isCommentsExpanded else internalCommentsExpanded

    val cardBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onPostClick != null) Modifier.clickable { onPostClick() } else Modifier)
            .testTag("supabase_post_card_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ==================== 1. AUTHOR SECTION ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author Avatar
                if (!post.authorAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(post.authorAvatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar of ${post.authorName}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                    )
                } else {
                    PanchayatAvatar(
                        name = post.authorName.ifBlank { "User" },
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Author Name, Verified, Handle, and Time
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = post.authorName.ifBlank { "Panchayat Member" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (post.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Member",
                                tint = TwitterBlue,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "· ${formatTimeAgo(post.timestamp)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "@${post.authorHandle.ifBlank { "author_${post.authorId.take(5)}" }}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Category Pill Tag
                        if (post.category.isNotBlank() && post.category != "General") {
                            SupabaseCategoryBadge(category = post.category)
                        }
                    }
                }

                // Supabase Origin Pill Indicator
                Surface(
                    color = TwitterBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Supabase Live Post",
                            tint = TwitterBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Supabase",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = TwitterBlue
                        )
                    }
                }
            }

            // ==================== 2. CONTENT SECTION ====================
            Spacer(modifier = Modifier.height(12.dp))
            StyledPostContent(
                text = post.content,
                textColor = MaterialTheme.colorScheme.onSurface
            )

            // Optional Image / Media preview
            if (!post.mediaUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(post.mediaUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Media attachment",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                )
            }

            // ==================== 3. METADATA & ACTIONS SECTION ====================
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reply / Comments count
                MetadataActionButton(
                    icon = Icons.AutoMirrored.Outlined.Chat,
                    count = commentsCount,
                    contentDescription = "Reply",
                    activeColor = TwitterBlue,
                    isActive = effectiveCommentsExpanded,
                    modifier = Modifier.testTag("comment_button_${post.id}"),
                    additionalTag = "reply_button_${post.id}",
                    countTestTag = "comment_count_${post.id}",
                    onClick = {
                        if (onToggleComments != null) {
                            onToggleComments()
                        } else {
                            internalCommentsExpanded = !internalCommentsExpanded
                        }
                        onReplyClick?.invoke(post)
                    }
                )

                // Repost / Retweet
                MetadataActionButton(
                    icon = if (isReposted) Icons.Filled.Repeat else Icons.Outlined.Repeat,
                    count = repostsCount,
                    contentDescription = "Repost",
                    activeColor = RepostGreen,
                    isActive = isReposted,
                    onClick = {
                        isReposted = !isReposted
                        repostsCount = if (isReposted) repostsCount + 1 else maxOf(0, repostsCount - 1)
                        onRepostClick?.invoke(post.id)
                    }
                )

                // Likes count with bounce animation
                val likeScale by animateFloatAsState(
                    targetValue = if (isLiked) 1.25f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "supabaseLikeScale"
                )
                MetadataActionButton(
                    icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    count = likesCount,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    activeColor = LikePink,
                    isActive = isLiked,
                    iconScale = likeScale,
                    modifier = Modifier.testTag("like_button_${post.id}"),
                    countTestTag = "like_count_${post.id}",
                    onClick = {
                        val willBeLiked = !isLiked
                        isLiked = willBeLiked
                        likesCount = if (willBeLiked) likesCount + 1 else maxOf(0, likesCount - 1)
                        onLikeClick?.invoke(post.id)
                    }
                )

                // Views / Impressions metadata
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Equalizer,
                        contentDescription = "Impressions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = formatCount(post.viewsCount),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Share Button
                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${post.authorName} on Supabase Panchayat: \"${post.content}\""
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Post via"))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Small Table Row ID Metadata pill at bottom
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Table row: ${post.id.take(8)}...",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                if (!post.createdAt.isNullOrBlank()) {
                    Text(
                        text = post.createdAt.take(10),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // ==================== 4. COMMENT / REPLY SECTION ====================
            AnimatedVisibility(
                visible = effectiveCommentsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("comment_section_${post.id}")
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        thickness = 0.8.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Header: "Comments" with count badge and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                contentDescription = null,
                                tint = TwitterBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Comments",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                color = TwitterBlue.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${localReplies.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = TwitterBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                internalCommentsExpanded = false
                                if (onToggleComments != null && isCommentsExpanded) {
                                    onToggleComments()
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("close_comments_${post.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Comments",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Existing Replies List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comments_list_${post.id}"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (localReplies.isNotEmpty()) {
                            localReplies.forEach { reply ->
                                PostCardReplyItem(reply = reply)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("empty_replies_${post.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No replies yet. Be the first to join the conversation!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reply Composer: Input Field + Submit Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reply_composer_${post.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var replyInputText by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = replyInputText,
                            onValueChange = { replyInputText = it },
                            placeholder = {
                                Text(
                                    "Write a reply...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reply_input_field"),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TwitterBlue,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            ),
                            maxLines = 3,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                        )

                        val isSendEnabled = replyInputText.isNotBlank()

                        IconButton(
                            onClick = {
                                if (isSendEnabled) {
                                    val text = replyInputText.trim()
                                    val newReply = ReplyEntity(
                                        id = "reply_${System.currentTimeMillis()}",
                                        postId = post.id,
                                        authorId = "user_me",
                                        authorName = "Aman Verma",
                                        authorHandle = "aman_verma",
                                        isVerified = true,
                                        content = text,
                                        timestamp = System.currentTimeMillis(),
                                        likesCount = 0,
                                        isLiked = false
                                    )
                                    localReplies = localReplies + newReply
                                    commentsCount += 1
                                    replyInputText = ""
                                    onSubmitReply?.invoke(text)
                                }
                            },
                            enabled = isSendEnabled,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSendEnabled) TwitterBlue else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("reply_submit_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Reply",
                                tint = if (isSendEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCardReplyItem(
    reply: ReplyEntity,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        modifier = modifier
            .fillMaxWidth()
            .testTag("reply_item_${reply.id}")
            .testTag("comment_item")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = PanchayatSaffron.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = reply.authorName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = PanchayatSaffron,
                        fontSize = 13.sp
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = reply.authorName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (reply.isVerified) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified",
                            tint = TwitterBlue,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "@${reply.authorHandle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " · ${formatTimeAgo(reply.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = reply.content,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("comment_content_${reply.id}")
                )
            }
        }
    }
}

@Composable
private fun SupabaseCategoryBadge(category: String) {
    val (bgColor, textColor) = when (category) {
        "Gram Sabha" -> Pair(PanchayatSaffron.copy(alpha = 0.15f), PanchayatSaffron)
        "Civic Notice" -> Pair(CivicAmber.copy(alpha = 0.15f), CivicAmber)
        "Development" -> Pair(TwitterBlue.copy(alpha = 0.15f), TwitterBlue)
        "Agriculture" -> Pair(RepostGreen.copy(alpha = 0.15f), RepostGreen)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = category.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.4.sp
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
        )
    }
}

@Composable
private fun StyledPostContent(text: String, textColor: Color) {
    val annotated = buildAnnotatedString {
        val words = text.split(" ")
        for (i in words.indices) {
            val word = words[i]
            if (word.startsWith("#") || word.startsWith("@")) {
                withStyle(
                    SpanStyle(
                        color = TwitterBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(word)
                }
            } else {
                withStyle(SpanStyle(color = textColor)) {
                    append(word)
                }
            }
            if (i < words.size - 1) append(" ")
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = 22.sp,
            fontSize = 15.sp
        )
    )
}

@Composable
private fun MetadataActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    contentDescription: String,
    activeColor: Color,
    isActive: Boolean,
    iconScale: Float = 1.0f,
    modifier: Modifier = Modifier,
    additionalTag: String? = null,
    countTestTag: String? = null,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "actionColor"
    )

    Box(
        modifier = if (additionalTag != null) {
            Modifier.testTag(additionalTag).clickable(onClick = onClick)
        } else {
            Modifier
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = animatedColor,
                modifier = Modifier
                    .size(17.dp)
                    .scale(iconScale)
            )
            if (count > 0) {
                Text(
                    text = formatCount(count),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = animatedColor,
                    modifier = if (countTestTag != null) Modifier.testTag(countTestTag) else Modifier
                )
            }
        }
    }
}
