package com.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.panchayat.data.model.Post
import com.example.panchayat.data.model.UserProfile
import com.example.panchayat.data.remote.SupabaseClient
import com.example.panchayat.data.repository.SeedData
import com.example.panchayat.ui.screens.ProfileScreen as InternalProfileScreen

/**
 * Public composable ProfileScreen component that fetches and displays the current user's
 * information and a list of their past microblog posts from Supabase.
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    user: UserProfileEntity? = null,
    profile: UserProfile? = null,
    posts: List<PostEntity> = emptyList(),
    userPosts: List<Post>? = null,
    client: SupabaseClient = SupabaseClient.default,
    autoFetch: Boolean = true,
    enableRealtime: Boolean = true,
    onUpdateBio: ((String) -> Unit)? = null,
    onUpdateProfile: ((name: String, bio: String, location: String) -> Unit)? = null,
    onPostClick: ((PostEntity) -> Unit)? = null,
    onLikeClick: ((String) -> Unit)? = null,
    onRepostClick: ((String) -> Unit)? = null,
    onBookmarkClick: ((String) -> Unit)? = null,
    onReplyClick: ((PostEntity) -> Unit)? = null,
    onPostsLoaded: ((List<Post>) -> Unit)? = null
) {
    val effectiveUser = when {
        user != null -> user
        profile != null -> profile.toEntity(isCurrentUser = true)
        else -> SeedData.currentUser
    }
    val effectivePosts = if (userPosts != null) {
        userPosts.map { it.toEntity() }
    } else {
        posts
    }

    InternalProfileScreen(
        user = effectiveUser,
        allPosts = effectivePosts,
        client = client,
        autoFetch = autoFetch,
        enableRealtime = enableRealtime,
        onPostClick = { onPostClick?.invoke(it) },
        onLikeClick = { onLikeClick?.invoke(it) },
        onRepostClick = { onRepostClick?.invoke(it) },
        onBookmarkClick = { onBookmarkClick?.invoke(it) },
        onReplyClick = { onReplyClick?.invoke(it) },
        onUpdateProfile = { name, bio, loc ->
            onUpdateProfile?.invoke(name, bio, loc)
            onUpdateBio?.invoke(bio)
        },
        onUpdateBio = onUpdateBio,
        onPostsLoaded = onPostsLoaded,
        modifier = modifier
    )
}
