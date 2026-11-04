package com.example.panchayat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.ReplyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE category = 'Civic Notice' OR category = 'Gram Sabha' ORDER BY timestamp DESC")
    fun getCivicPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE authorId = :userId ORDER BY timestamp DESC")
    fun getPostsByUser(userId: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isLiked = 1 ORDER BY timestamp DESC")
    fun getLikedPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: String): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("UPDATE posts SET isLiked = :isLiked, likesCount = :likesCount WHERE id = :postId")
    suspend fun updateLikeStatus(postId: String, isLiked: Boolean, likesCount: Int)

    @Query("UPDATE posts SET isReposted = :isReposted, repostsCount = :repostsCount WHERE id = :postId")
    suspend fun updateRepostStatus(postId: String, isReposted: Boolean, repostsCount: Int)

    @Query("UPDATE posts SET isBookmarked = :isBookmarked WHERE id = :postId")
    suspend fun updateBookmarkStatus(postId: String, isBookmarked: Boolean)

    @Query("UPDATE posts SET repliesCount = repliesCount + 1 WHERE id = :postId")
    suspend fun incrementReplyCount(postId: String)

    @Query("SELECT * FROM replies WHERE postId = :postId ORDER BY timestamp ASC")
    fun getRepliesForPost(postId: String): Flow<List<ReplyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: ReplyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplies(replies: List<ReplyEntity>)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getPostCount(): Int

    @Query("SELECT * FROM posts WHERE content LIKE '%' || :query || '%' OR authorName LIKE '%' || :query || '%' OR authorHandle LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchPosts(query: String): Flow<List<PostEntity>>
}
