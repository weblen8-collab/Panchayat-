package com.example.panchayat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.model.UserProfile
import com.example.ui.theme.CivicAmber
import com.example.ui.theme.LikePink
import com.example.ui.theme.PanchayatSaffron
import com.example.ui.theme.RepostGreen
import com.example.ui.theme.TwitterBlue

/**
 * A reusable user profile component that displays a user's avatar, bio,
 * detailed metadata, and a list of their recent posts.
 *
 * Supports both domain [UserProfile] / [Post] models and Room database
 * entities ([UserProfileEntity] / [PostEntity]), as well as direct field arguments.
 */
@Composable
fun UserProfileComponent(
    modifier: Modifier = Modifier,
    user: UserProfileEntity? = null,
    profile: UserProfile? = null,
    name: String? = null,
    handle: String? = null,
    bio: String? = null,
    avatarUrl: String? = null,
    bannerUrl: String? = null,
    location: String? = null,
    joinedDate: String? = null,
    followingCount: Int? = null,
    followersCount: Int? = null,
    isVerified: Boolean? = null,
    recentPosts: List<Post> = emptyList(),
    posts: List<PostEntity> = emptyList(),
    scrollable: Boolean = true,
    showHeaderBanner: Boolean = true,
    showStats: Boolean = true,
    onPostClick: ((Post) -> Unit)? = null,
    onPostEntityClick: ((PostEntity) -> Unit)? = null,
    onLikeClick: ((String) -> Unit)? = null,
    onReplyClick: ((Post) -> Unit)? = null,
    onRepostClick: ((String) -> Unit)? = null,
    onShareClick: ((Post) -> Unit)? = null,
    onEditBioClick: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null,
    onFollowClick: (() -> Unit)? = null
) {
    // Resolve effective user data
    val effectiveName = name
        ?: profile?.name?.ifBlank { null }
        ?: user?.name?.ifBlank { null }
        ?: user?.username?.ifBlank { null }
        ?: "Panchayat Member"

    val effectiveHandle = handle
        ?: profile?.handle?.ifBlank { null }
        ?: user?.handle?.ifBlank { null }
        ?: user?.username?.ifBlank { null }
        ?: "member"

    val effectiveBio = bio
        ?: profile?.bio
        ?: user?.bio
        ?: ""

    val effectiveAvatarUrl = avatarUrl
        ?: profile?.avatarUrl
        ?: user?.profileImageUrl
        ?: user?.avatarUrl

    val effectiveBannerUrl = bannerUrl
        ?: profile?.bannerUrl
        ?: user?.bannerUrl

    val effectiveLocation = location
        ?: profile?.location
        ?: user?.location
        ?: ""

    val effectiveJoinedDate = joinedDate
        ?: profile?.joinedDate
        ?: user?.joinedDate
        ?: "Joined recently"

    val effectiveFollowingCount = followingCount
        ?: profile?.followingCount
        ?: user?.followingCount
        ?: 0

    val effectiveFollowersCount = followersCount
        ?: profile?.followersCount
        ?: user?.followersCount
        ?: 0

    val effectiveIsVerified = isVerified
        ?: profile?.isVerified
        ?: user?.isVerified
        ?: false

    // Resolve unified recent posts list as domain Post models
    val unifiedPosts: List<Post> = when {
        recentPosts.isNotEmpty() -> recentPosts
        posts.isNotEmpty() -> posts.map { Post.fromEntity(it) }
        else -> emptyList()
    }

    if (scrollable) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("profile_screen_scroll")
        ) {
            // Header: Banner + Avatar + Bio + Details
            item {
                UserProfileHeader(
                    name = effectiveName,
                    handle = effectiveHandle,
                    bio = effectiveBio,
                    avatarUrl = effectiveAvatarUrl,
                    bannerUrl = effectiveBannerUrl,
                    location = effectiveLocation,
                    joinedDate = effectiveJoinedDate,
                    followingCount = effectiveFollowingCount,
                    followersCount = effectiveFollowersCount,
                    postsCount = unifiedPosts.size,
                    isVerified = effectiveIsVerified,
                    showHeaderBanner = showHeaderBanner,
                    showStats = showStats,
                    onEditBioClick = onEditBioClick,
                    onAvatarClick = onAvatarClick,
                    onFollowClick = onFollowClick
                )
            }

            // Recent Posts Section Title
            item {
                RecentPostsHeader(postsCount = unifiedPosts.size)
            }

            // Posts List or Empty State
            if (unifiedPosts.isEmpty()) {
                item {
                    UserProfileEmptyPosts()
                }
            } else {
                items(
                    items = unifiedPosts,
                    key = { it.id }
                ) { post ->
                    UserProfilePostItem(
                        post = post,
                        onPostClick = {
                            onPostClick?.invoke(post)
                            onPostEntityClick?.invoke(post.toEntity())
                        },
                        onLikeClick = { onLikeClick?.invoke(post.id) },
                        onReplyClick = { onReplyClick?.invoke(post) },
                        onRepostClick = { onRepostClick?.invoke(post.id) },
                        onShareClick = { onShareClick?.invoke(post) }
                    )
                }
            }
        }
    } else {
        // Non-scrollable column layout for embedding inside other scroll containers
        Column(
            modifier = modifier
                .fillMaxWidth()
                .testTag("user_profile_component")
        ) {
            UserProfileHeader(
                name = effectiveName,
                handle = effectiveHandle,
                bio = effectiveBio,
                avatarUrl = effectiveAvatarUrl,
                bannerUrl = effectiveBannerUrl,
                location = effectiveLocation,
                joinedDate = effectiveJoinedDate,
                followingCount = effectiveFollowingCount,
                followersCount = effectiveFollowersCount,
                postsCount = unifiedPosts.size,
                isVerified = effectiveIsVerified,
                showHeaderBanner = showHeaderBanner,
                showStats = showStats,
                onEditBioClick = onEditBioClick,
                onAvatarClick = onAvatarClick,
                onFollowClick = onFollowClick
            )

            RecentPostsHeader(postsCount = unifiedPosts.size)

            if (unifiedPosts.isEmpty()) {
                UserProfileEmptyPosts()
            } else {
                unifiedPosts.forEach { post ->
                    UserProfilePostItem(
                        post = post,
                        onPostClick = {
                            onPostClick?.invoke(post)
                            onPostEntityClick?.invoke(post.toEntity())
                        },
                        onLikeClick = { onLikeClick?.invoke(post.id) },
                        onReplyClick = { onReplyClick?.invoke(post) },
                        onRepostClick = { onRepostClick?.invoke(post.id) },
                        onShareClick = { onShareClick?.invoke(post) }
                    )
                }
            }
        }
    }
}

/**
 * Header section containing banner, user avatar, name, handle, bio, and stats.
 */
@Composable
fun UserProfileHeader(
    name: String,
    handle: String,
    bio: String,
    avatarUrl: String?,
    bannerUrl: String?,
    location: String,
    joinedDate: String,
    followingCount: Int,
    followersCount: Int,
    postsCount: Int,
    isVerified: Boolean,
    showHeaderBanner: Boolean = true,
    showStats: Boolean = true,
    onEditBioClick: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null,
    onFollowClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_profile_header")
    ) {
        // Banner Image
        if (showHeaderBanner) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.panchayat_banner),
                    contentDescription = "Profile Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Avatar Row with Edit/Action buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp)
        ) {
            val avatarOffset = if (showHeaderBanner) (-36).dp else 0.dp

            // Avatar Container
            Box(
                modifier = Modifier
                    .offset(y = avatarOffset)
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .then(
                        if (onAvatarClick != null) {
                            Modifier.clickable { onAvatarClick() }
                        } else {
                            Modifier
                        }
                    )
                    .testTag("profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .testTag("user_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar of $name",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = painterResource(id = R.drawable.ic_launcher_foreground)
                        )
                    } else {
                        PanchayatAvatar(
                            name = name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Top action buttons row (Edit Profile / Edit Bio / Follow)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = if (showHeaderBanner) 10.dp else 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onEditBioClick != null) {
                    OutlinedButton(
                        onClick = onEditBioClick,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("edit_bio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Bio",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Edit Bio",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                if (onFollowClick != null) {
                    OutlinedButton(
                        onClick = onFollowClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = PanchayatSaffron,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("follow_user_button")
                    ) {
                        Text(
                            text = "Follow",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Profile Details (Name, Handle, Bio, Metadata, Stats)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = if (showHeaderBanner) 0.dp else 8.dp)
        ) {
            // Display Name and Verified Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(modifier = Modifier.testTag("profile_user_name")) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("user_name")
                    )
                }

                if (isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Member",
                        tint = TwitterBlue,
                        modifier = Modifier
                            .size(18.dp)
                            .testTag("profile_verified_badge")
                    )
                }
            }

            // User Handle
            val displayHandle = if (handle.startsWith("@")) handle else "@$handle"
            Box(modifier = Modifier.testTag("profile_user_handle")) {
                Text(
                    text = displayHandle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("user_handle")
                )
            }

            // User Bio Section
            Spacer(modifier = Modifier.height(10.dp))
            val displayBio = bio.ifBlank { "No bio yet." }
            Box(modifier = Modifier.testTag("profile_user_bio")) {
                Text(
                    text = displayBio,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 20.sp),
                    color = if (bio.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("user_bio")
                )
            }

            // Location & Joined Date Metadata Row
            if (location.isNotBlank() || joinedDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.testTag("profile_metadata_row")
                ) {
                    if (location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("profile_user_location")
                            )
                        }
                    }

                    if (joinedDate.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Joined Date",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = joinedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("profile_joined_date")
                            )
                        }
                    }
                }
            }

            // Stats Row: Following, Followers, Posts
            if (showStats) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("profile_stats_row")
                ) {
                    // Following Count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("profile_following_count")
                    ) {
                        Text(
                            text = "$followingCount",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Following",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Followers Count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("profile_followers_count")
                    ) {
                        Text(
                            text = "$followersCount",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Followers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Posts Count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("profile_posts_count")
                    ) {
                        Text(
                            text = "$postsCount",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Posts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    }
}

/**
 * Section title for the Recent Posts list.
 */
@Composable
fun RecentPostsHeader(
    postsCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("recent_posts_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Article,
                contentDescription = null,
                tint = PanchayatSaffron,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Recent Posts",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("recent_posts_title")
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.testTag("recent_posts_badge")
        ) {
            Text(
                text = "$postsCount",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Renders an individual post within the user profile recent posts list.
 */
@Composable
fun UserProfilePostItem(
    post: Post,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onRepostClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPostClick)
            .testTag("user_post_${post.id}")
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Author row with Category Chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            PanchayatAvatar(
                name = post.authorName,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (post.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Member",
                            tint = TwitterBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "@${post.authorHandle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (post.category.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PanchayatSaffron.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = post.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PanchayatSaffron,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Post Content
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("post_content_${post.id}")
        )

        // Action Bar (Reply, Repost, Like, Share)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reply
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onReplyClick)
                    .testTag("reply_button_${post.id}")
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = "Reply",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.commentsCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Repost
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onRepostClick)
                    .testTag("repost_button_${post.id}")
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repost",
                    tint = if (post.isReposted) RepostGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.repostsCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (post.isReposted) RepostGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Like
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onLikeClick)
                    .testTag("like_button_${post.id}")
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) LikePink else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.likesCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (post.isLiked) LikePink else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Share
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onShareClick)
                    .testTag("share_button_${post.id}")
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Placeholder displayed when the user does not have any recent posts.
 */
@Composable
fun UserProfileEmptyPosts(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("profile_empty_posts"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Article,
                contentDescription = null,
                tint = PanchayatSaffron,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No posts yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "When this member publishes notices, updates, or civic discussions, they will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
