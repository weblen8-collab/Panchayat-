package com.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.model.UserProfile
import com.example.panchayat.ui.components.UserProfileComponent as InternalUserProfileComponent

/**
 * Public UserProfileComponent for easy import and usage throughout the app.
 * Displays a user's avatar, bio, and a list of their recent posts.
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
    InternalUserProfileComponent(
        modifier = modifier,
        user = user,
        profile = profile,
        name = name,
        handle = handle,
        bio = bio,
        avatarUrl = avatarUrl,
        bannerUrl = bannerUrl,
        location = location,
        joinedDate = joinedDate,
        followingCount = followingCount,
        followersCount = followersCount,
        isVerified = isVerified,
        recentPosts = recentPosts,
        posts = posts,
        scrollable = scrollable,
        showHeaderBanner = showHeaderBanner,
        showStats = showStats,
        onPostClick = onPostClick,
        onPostEntityClick = onPostEntityClick,
        onLikeClick = onLikeClick,
        onReplyClick = onReplyClick,
        onRepostClick = onRepostClick,
        onShareClick = onShareClick,
        onEditBioClick = onEditBioClick,
        onAvatarClick = onAvatarClick,
        onFollowClick = onFollowClick
    )
}
