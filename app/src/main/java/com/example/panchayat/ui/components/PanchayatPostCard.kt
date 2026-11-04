package com.example.panchayat.ui.components

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.panchayat.data.local.entity.PostEntity
import com.example.ui.theme.CivicAmber
import com.example.ui.theme.LikePink
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.TwitterBlue

@Composable
fun PanchayatPostCard(
    post: PostEntity,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onRepostClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPostClick)
            .testTag("post_card_${post.id}")
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // User Avatar
            PanchayatAvatar(
                name = post.authorName,
                modifier = Modifier.size(44.dp)
            )

            // Post Content Column
            Column(modifier = Modifier.weight(1f)) {
                // Header Row (Name, Verified, Handle, Time Ago, Category Badge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (post.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Official",
                            tint = TwitterBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "@${post.authorHandle}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Text(
                        text = " · ${formatTimeAgo(post.timestamp)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Category Tag Pill
                if (post.category.isNotEmpty() && post.category != "General") {
                    Spacer(modifier = Modifier.height(4.dp))
                    CategoryPill(category = post.category)
                }

                // Main Text Content with syntax highlighting for hashtags and handles
                Spacer(modifier = Modifier.height(8.dp))
                StyledPostText(
                    text = post.content,
                    textColor = MaterialTheme.colorScheme.onSurface
                )

                // Optional Attached Media Preview
                if (post.mediaDrawableRes != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(
                        painter = painterResource(id = post.mediaDrawableRes),
                        contentDescription = "Post attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    )
                }

                // Action Bar (Reply, Repost, Like, Share, Views)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reply
                    ActionButton(
                        icon = Icons.AutoMirrored.Outlined.Chat,
                        count = post.repliesCount,
                        contentDescription = "Reply",
                        activeColor = TwitterBlue,
                        isActive = false,
                        modifier = Modifier.testTag("reply_button_${post.id}"),
                        additionalTag = "comment_button_${post.id}",
                        countTestTag = "reply_count_${post.id}",
                        onClick = onReplyClick
                    )

                    // Repost
                    ActionButton(
                        icon = if (post.isReposted) Icons.Filled.Repeat else Icons.Outlined.Repeat,
                        count = post.repostsCount,
                        contentDescription = "Repost",
                        activeColor = RepostGreen,
                        isActive = post.isReposted,
                        modifier = Modifier.testTag("repost_button_${post.id}"),
                        countTestTag = "repost_count_${post.id}",
                        onClick = onRepostClick
                    )

                    // Like (with bounce)
                    val likeScale by animateFloatAsState(
                        targetValue = if (post.isLiked) 1.25f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "likeScale"
                    )
                    ActionButton(
                        icon = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        count = post.likesCount,
                        contentDescription = if (post.isLiked) "Unlike" else "Like",
                        activeColor = LikePink,
                        isActive = post.isLiked,
                        iconScale = likeScale,
                        modifier = Modifier.testTag("like_button_${post.id}"),
                        countTestTag = "like_count_${post.id}",
                        onClick = onLikeClick
                    )

                    // Views
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Equalizer,
                            contentDescription = "Views",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = formatCount(post.viewsCount),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    // Bookmark
                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (post.isBookmarked) TwitterBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${post.authorName} on Panchayat: \"${post.content}\"")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Panch via"))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider(
        thickness = 0.6.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    )
}

@Composable
fun CategoryPill(category: String) {
    val (bgColor, textColor) = when (category) {
        "Gram Sabha" -> Pair(PanchayatSaffron.copy(alpha = 0.18f), PanchayatSaffron)
        "Civic Notice" -> Pair(CivicAmber.copy(alpha = 0.18f), CivicAmber)
        "Development" -> Pair(TwitterBlue.copy(alpha = 0.18f), TwitterBlue)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = category.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun StyledPostText(text: String, textColor: Color) {
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
            lineHeight = 21.sp,
            fontSize = 15.sp
        )
    )
}

@Composable
fun PanchayatAvatar(name: String, modifier: Modifier = Modifier) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
    val avatarColors = listOf(
        Color(0xFFFF6D00),
        Color(0xFF1D9BF0),
        Color(0xFF00BA7C),
        Color(0xFF7952B3),
        Color(0xFFE91E63)
    )
    val colorIndex = kotlin.math.abs(name.hashCode()) % avatarColors.size
    val bgColor = avatarColors[colorIndex]

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun ActionButton(
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

fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> "${days / 7}w"
    }
}

fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
