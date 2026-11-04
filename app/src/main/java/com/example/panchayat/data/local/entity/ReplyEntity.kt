package com.example.panchayat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "replies")
data class ReplyEntity(
    @PrimaryKey
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String? = null,
    val isVerified: Boolean = false,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)
