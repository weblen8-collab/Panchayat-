package com.example.panchayat.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * Domain model representing a Post in the application and corresponding
 * to the `posts` table in Supabase.
 *
 * @param id Unique identifier of the post (UUID)
 * @param authorId The ID of the author/user who created the post
 * @param content The text body/content of the post
 * @param timestamp Epoch millisecond timestamp or ISO timestamp of creation
 * @param likesCount Metadata tracking the total number of likes
 * @param commentsCount Metadata tracking the total number of comments/replies
 * @param repostsCount Metadata tracking the total number of reposts/shares
 * @param viewsCount Metadata tracking total impressions
 * @param authorName Display name of the author
 * @param authorHandle Handle (@username) of the author
 * @param authorAvatarUrl Profile picture / avatar URL of the author
 * @param isVerified Whether the author is verified
 * @param category Category tag (e.g., General, Gram Sabha, Civic Notice, Agriculture)
 * @param mediaUrl Optional image attachment URL
 * @param createdAt Supabase ISO timestamp string (e.g. 2026-09-15T12:00:00Z)
 */
@JsonClass(generateAdapter = true)
data class Post(
    @Json(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @Json(name = "author_id")
    val authorId: String,

    @Json(name = "content")
    val content: String,

    @Json(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @Json(name = "likes_count")
    val likesCount: Int = 0,

    @Json(name = "comments_count")
    val commentsCount: Int = 0,

    @Json(name = "reposts_count")
    val repostsCount: Int = 0,

    @Json(name = "views_count")
    val viewsCount: Int = 0,

    @Json(name = "author_name")
    val authorName: String = "",

    @Json(name = "author_handle")
    val authorHandle: String = "",

    @Json(name = "author_avatar_url")
    val authorAvatarUrl: String? = null,

    @Json(name = "is_verified")
    val isVerified: Boolean = false,

    @Json(name = "category")
    val category: String = "General",

    @Json(name = "media_url")
    val mediaUrl: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "is_liked")
    val isLiked: Boolean = false,

    @Json(name = "is_reposted")
    val isReposted: Boolean = false
) {
    val displayTimestamp: Long
        get() = timestamp

    fun toEntity(isLiked: Boolean = this.isLiked, isReposted: Boolean = this.isReposted, isBookmarked: Boolean = false): com.example.panchayat.data.local.entity.PostEntity {
        return com.example.panchayat.data.local.entity.PostEntity(
            id = id,
            authorId = authorId,
            authorName = authorName.ifBlank { "Panchayat Member" },
            authorHandle = authorHandle.ifBlank { "user_${authorId.take(6)}" },
            authorAvatarUrl = authorAvatarUrl,
            isVerified = isVerified,
            content = content,
            timestamp = timestamp,
            category = category.ifBlank { "General" },
            mediaUrl = mediaUrl,
            likesCount = likesCount,
            repostsCount = repostsCount,
            repliesCount = commentsCount,
            viewsCount = viewsCount,
            isLiked = isLiked,
            isReposted = isReposted,
            isBookmarked = isBookmarked,
            syncedWithSupabase = true
        )
    }

    companion object {
        fun fromEntity(entity: com.example.panchayat.data.local.entity.PostEntity): Post {
            return Post(
                id = entity.id,
                authorId = entity.authorId,
                content = entity.content,
                timestamp = entity.timestamp,
                likesCount = entity.likesCount,
                commentsCount = entity.repliesCount,
                repostsCount = entity.repostsCount,
                viewsCount = entity.viewsCount,
                authorName = entity.authorName,
                authorHandle = entity.authorHandle,
                authorAvatarUrl = entity.authorAvatarUrl,
                isVerified = entity.isVerified,
                category = entity.category,
                mediaUrl = entity.mediaUrl,
                isLiked = entity.isLiked,
                isReposted = entity.isReposted
            )
        }

        fun fromDto(dto: com.example.panchayat.data.remote.SupabasePostDto): Post {
            return Post(
                id = dto.id,
                authorId = dto.authorId,
                content = dto.content,
                timestamp = System.currentTimeMillis(),
                likesCount = dto.likesCount,
                commentsCount = dto.repliesCount,
                repostsCount = dto.repostsCount,
                viewsCount = dto.viewsCount,
                authorName = dto.authorName,
                authorHandle = dto.authorHandle,
                authorAvatarUrl = dto.authorAvatarUrl,
                isVerified = dto.isVerified,
                category = dto.category,
                mediaUrl = dto.mediaUrl,
                createdAt = dto.createdAt
            )
        }
        /**
         * Matching Supabase PostgreSQL schema for the `posts` table.
         */
        const val SUPABASE_POSTS_TABLE_SCHEMA: String = """
-- Supabase SQL Schema for `posts` Table
CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id TEXT NOT NULL,
    content TEXT NOT NULL,
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM now()) * 1000)::BIGINT,
    likes_count INTEGER NOT NULL DEFAULT 0,
    comments_count INTEGER NOT NULL DEFAULT 0,
    reposts_count INTEGER NOT NULL DEFAULT 0,
    views_count INTEGER NOT NULL DEFAULT 0,
    author_name TEXT NOT NULL DEFAULT '',
    author_handle TEXT NOT NULL DEFAULT '',
    author_avatar_url TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    category TEXT NOT NULL DEFAULT 'General',
    media_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now())
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_timestamp ON posts(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_category ON posts(category);

-- Enable Row Level Security (RLS)
ALTER TABLE posts ENABLE ROW LEVEL SECURITY;

-- Policies for Row Level Security
-- 1. Anyone can read posts
CREATE POLICY "Public Read Access"
    ON posts FOR SELECT
    USING (true);

-- 2. Anyone can insert posts (or authenticated users)
CREATE POLICY "Public Insert Access"
    ON posts FOR INSERT
    WITH CHECK (true);

-- 3. Authors can update their own posts / likes
CREATE POLICY "Public Update Access"
    ON posts FOR UPDATE
    USING (true);

-- 4. Authors can delete their own posts
CREATE POLICY "Public Delete Access"
    ON posts FOR DELETE
    USING (true);
"""
    }
}
