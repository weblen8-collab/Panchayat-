package com.example.panchayat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity for 'Post' supporting the core microblogging functionality.
 * Includes fields for post content, timestamp, author metadata, and a foreign key
 * referencing the user profile table [UserProfileEntity].
 *
 * @property id Unique identifier for the post (UUID)
 * @property authorId Foreign key referencing [UserProfileEntity.id] in the 'user_profiles' table
 * @property content The text content / body of the post
 * @property timestamp Millisecond epoch timestamp when the post was created
 * @property authorName Display name of the author
 * @property authorHandle Username / handle of the author
 * @property authorAvatarUrl URL to the author's avatar / profile image
 * @property isVerified Whether the author holds a verified civic status
 * @property category Categorization tag (e.g. "General", "Gram Sabha", "Civic Notice", "Development", "Agriculture")
 * @property mediaUrl Optional remote image attachment URL
 * @property mediaDrawableRes Optional local resource drawable ID
 * @property likesCount Total count of likes
 * @property repostsCount Total count of reposts / shares
 * @property repliesCount Total count of replies / comments
 * @property viewsCount Total impression count
 * @property isLiked Whether current user has liked this post
 * @property isReposted Whether current user has reposted this post
 * @property isBookmarked Whether current user has bookmarked this post
 * @property syncedWithSupabase Whether the post is synced with Supabase remote backend
 */
@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["authorId"]),
        Index(value = ["timestamp"]),
        Index(value = ["category"])
    ]
)
data class PostEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val authorId: String,
    val authorName: String = "",
    val authorHandle: String = "",
    val authorAvatarUrl: String? = null,
    val isVerified: Boolean = false,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "General", // "General", "Gram Sabha", "Civic Notice", "Development", "Agriculture"
    val mediaUrl: String? = null,
    val mediaDrawableRes: Int? = null,
    val likesCount: Int = 0,
    val repostsCount: Int = 0,
    val repliesCount: Int = 0,
    val viewsCount: Int = 0,
    val isLiked: Boolean = false,
    val isReposted: Boolean = false,
    val isBookmarked: Boolean = false,
    val syncedWithSupabase: Boolean = false
) {
    /**
     * Alias for authorId representing the foreign key to the user profile table.
     */
    val userId: String
        get() = authorId

    /**
     * Alias for post content.
     */
    val postContent: String
        get() = content

    /**
     * Secondary constructor supporting creation with core fields:
     * post content, foreign key (authorId), and timestamp.
     */
    @Ignore
    constructor(
        content: String,
        authorId: String,
        timestamp: Long = System.currentTimeMillis(),
        id: String = UUID.randomUUID().toString()
    ) : this(
        id = id,
        authorId = authorId,
        authorName = "",
        authorHandle = "",
        content = content,
        timestamp = timestamp
    )

    /**
     * Secondary constructor supporting explicit userId as the foreign key parameter.
     */
    @Ignore
    constructor(
        id: String = UUID.randomUUID().toString(),
        userId: String,
        content: String,
        timestamp: Long = System.currentTimeMillis(),
        authorName: String = "",
        authorHandle: String = ""
    ) : this(
        id = id,
        authorId = userId,
        authorName = authorName,
        authorHandle = authorHandle,
        content = content,
        timestamp = timestamp
    )
}
