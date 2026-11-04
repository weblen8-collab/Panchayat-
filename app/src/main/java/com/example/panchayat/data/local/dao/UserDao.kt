package com.example.panchayat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.panchayat.data.local.entity.UserWithPosts
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @androidx.room.Transaction
    @Query("SELECT * FROM user_profiles WHERE id = :userId")
    fun getUserWithPosts(userId: String): Flow<UserWithPosts?>

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserDirect(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE username = :username OR handle = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE id IN (:userIds)")
    suspend fun getUsersByIds(userIds: List<String>): List<UserProfileEntity>

    @Query("SELECT * FROM user_profiles ORDER BY followersCount DESC")
    fun getAllUsers(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE isCurrentUser = 0 ORDER BY followersCount DESC")
    fun getSuggestedUsers(): Flow<List<UserProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserProfileEntity>)

    @Update
    suspend fun updateUser(user: UserProfileEntity)

    @Query("UPDATE user_profiles SET isFollowing = :isFollowing, followersCount = followersCount + :diff WHERE id = :userId")
    suspend fun updateFollowStatus(userId: String, isFollowing: Boolean, diff: Int)

    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getUserCount(): Int
}

typealias UserProfileDao = UserDao

