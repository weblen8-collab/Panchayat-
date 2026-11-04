package com.example.panchayat.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relation data class representing a 1-to-many relationship between a user profile
 * and their authored posts in the microblogging feed via the foreign key constraint.
 */
data class UserWithPosts(
    @Embedded
    val user: UserProfileEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "authorId"
    )
    val posts: List<PostEntity> = emptyList()
)
