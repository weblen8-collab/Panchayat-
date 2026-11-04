package com.example.panchayat.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabasePostDto(
    val id: String,
    @Json(name = "author_id") val authorId: String,
    @Json(name = "author_name") val authorName: String,
    @Json(name = "author_handle") val authorHandle: String,
    @Json(name = "author_avatar_url") val authorAvatarUrl: String? = null,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    val content: String,
    val category: String = "General",
    @Json(name = "media_url") val mediaUrl: String? = null,
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "reposts_count") val repostsCount: Int = 0,
    @Json(name = "replies_count") val repliesCount: Int = 0,
    @Json(name = "views_count") val viewsCount: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseProfileDto(
    val id: String,
    val name: String,
    val handle: String,
    val bio: String,
    val location: String,
    @Json(name = "joined_date") val joinedDate: String,
    @Json(name = "following_count") val followingCount: Int = 0,
    @Json(name = "followers_count") val followersCount: Int = 0,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    @Json(name = "avatar_url") val avatarUrl: String? = null
) {
    fun toUserProfile(): com.example.panchayat.data.model.UserProfile =
        com.example.panchayat.data.model.UserProfile(
            id = id,
            name = name,
            handle = handle,
            bio = bio,
            location = location,
            joinedDate = joinedDate,
            followingCount = followingCount,
            followersCount = followersCount,
            isVerified = isVerified,
            avatarUrl = avatarUrl
        )

    fun toEntity(isCurrentUser: Boolean = false): com.example.panchayat.data.local.entity.UserProfileEntity =
        com.example.panchayat.data.local.entity.UserProfileEntity(
            id = id,
            name = name,
            handle = handle,
            username = handle,
            bio = bio,
            location = location,
            joinedDate = joinedDate,
            followingCount = followingCount,
            followersCount = followersCount,
            isVerified = isVerified,
            avatarUrl = avatarUrl,
            isCurrentUser = isCurrentUser
        )
}

@JsonClass(generateAdapter = true)
data class SupabaseLikeDto(
    @Json(name = "post_id") val postId: String,
    @Json(name = "user_id") val userId: String
)

@JsonClass(generateAdapter = true)
data class SupabaseUpdateLikesDto(
    @Json(name = "likes_count") val likesCount: Int
)
