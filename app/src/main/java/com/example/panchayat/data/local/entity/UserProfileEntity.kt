package com.example.panchayat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity for user profiles including fields for username, bio,
 * and profile image URL to support user identification in the microblogging feed.
 *
 * @property id Unique identifier of the user (UUID or auth UID)
 * @property handle User's @handle / unique username identifier
 * @property name User's full display name (e.g. "Manju Devi")
 * @property username Unique username for user identification in the feed (e.g. "sarpanch_phulera")
 * @property bio Biographical summary or village role description
 * @property avatarUrl URL to avatar (alias for profileImageUrl)
 * @property profileImageUrl URL to the user's profile image / avatar
 * @property location Village, ward, or geographical district
 * @property joinedDate Date the user joined the community (e.g. "Joined September 2024")
 * @property followingCount Count of followed members
 * @property followersCount Count of followers
 * @property isVerified Whether the profile has a verified civic badge
 * @property bannerUrl Profile header banner image URL
 * @property isCurrentUser Whether this is the active local user
 * @property isFollowing Whether the current user is following this profile
 */
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val handle: String = "",
    val name: String = handle,
    val username: String = handle.ifBlank { name },
    val bio: String = "",
    val avatarUrl: String? = null,
    val profileImageUrl: String? = avatarUrl,
    val location: String = "",
    val joinedDate: String = "Joined recently",
    val followingCount: Int = 0,
    val followersCount: Int = 0,
    val isVerified: Boolean = false,
    val bannerUrl: String? = null,
    val isCurrentUser: Boolean = false,
    val isFollowing: Boolean = false
) {
    /**
     * Secondary constructor for direct creation with username, bio, and profile image URL.
     */
    @Ignore
    constructor(
        username: String,
        bio: String,
        profileImageUrl: String? = null
    ) : this(
        id = UUID.randomUUID().toString(),
        handle = username,
        name = username,
        username = username,
        bio = bio,
        avatarUrl = profileImageUrl,
        profileImageUrl = profileImageUrl
    )

    /**
     * Secondary constructor for direct creation with explicit id, username, bio, and profile image URL.
     */
    @Ignore
    constructor(
        id: String,
        username: String,
        bio: String,
        profileImageUrl: String? = null
    ) : this(
        id = id,
        handle = username,
        name = username,
        username = username,
        bio = bio,
        avatarUrl = profileImageUrl,
        profileImageUrl = profileImageUrl
    )

    /**
     * Effective username for user identification across feed cards and author headers.
     */
    val effectiveUsername: String
        get() = username.ifBlank { handle.ifBlank { name } }

    /**
     * Effective profile image URL for avatar loading in feed items.
     */
    val effectiveProfileImageUrl: String?
        get() = profileImageUrl ?: avatarUrl
}
