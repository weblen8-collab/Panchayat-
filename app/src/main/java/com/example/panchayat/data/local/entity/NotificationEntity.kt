package com.example.panchayat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "LIKE", "REPOST", "REPLY", "CIVIC_NOTICE", "MENTION"
    val actorName: String,
    val actorHandle: String,
    val actorAvatarUrl: String? = null,
    val message: String,
    val targetPostSnippet: String? = null,
    val postId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
