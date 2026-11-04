package com.example.panchayat.data.repository

import android.util.Log
import com.example.panchayat.data.local.PanchayatDatabase
import com.example.panchayat.data.local.entity.NotificationEntity
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.ReplyEntity
import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.panchayat.data.model.UserProfile
import com.example.panchayat.data.remote.SupabaseClient
import com.example.panchayat.data.remote.SupabaseClientManager
import com.example.panchayat.data.remote.SupabasePostDto
import com.example.panchayat.data.remote.SupabaseUpdateLikesDto
import com.example.panchayat.data.remote.auth.AuthResponse
import com.example.panchayat.data.remote.auth.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class PanchayatRepository(
    private val database: PanchayatDatabase,
    private val supabaseManager: SupabaseClientManager
) {
    private val postDao = database.postDao()
    private val userDao = database.userDao()
    private val notificationDao = database.notificationDao()

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        if (postDao.getPostCount() == 0) {
            userDao.insertUser(SeedData.currentUser)
            userDao.insertUsers(SeedData.suggestedUsers)
            postDao.insertPosts(SeedData.getInitialPosts())
            postDao.insertReplies(SeedData.getInitialReplies())
            notificationDao.insertNotifications(SeedData.getInitialNotifications())
        }
    }

    fun getAllPosts(): Flow<List<PostEntity>> = postDao.getAllPosts()

    fun getCivicPosts(): Flow<List<PostEntity>> = postDao.getCivicPosts()

    fun getPostsByUser(userId: String): Flow<List<PostEntity>> = postDao.getPostsByUser(userId)

    fun getLikedPosts(): Flow<List<PostEntity>> = postDao.getLikedPosts()

    fun getBookmarkedPosts(): Flow<List<PostEntity>> = postDao.getBookmarkedPosts()

    fun searchPosts(query: String): Flow<List<PostEntity>> = postDao.searchPosts(query)

    fun getRepliesForPost(postId: String): Flow<List<ReplyEntity>> = postDao.getRepliesForPost(postId)

    fun getCurrentUser(): Flow<UserProfileEntity?> = userDao.getCurrentUser()

    fun getSuggestedUsers(): Flow<List<UserProfileEntity>> = userDao.getSuggestedUsers()

    fun getNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    fun getUnreadNotificationCount(): Flow<Int> = notificationDao.getUnreadCount()

    suspend fun createPost(
        content: String,
        category: String,
        mediaDrawableRes: Int? = null
    ): PostEntity = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUserDirect() ?: SeedData.currentUser
        val newPost = PostEntity(
            id = UUID.randomUUID().toString(),
            authorId = currentUser.id,
            authorName = currentUser.name,
            authorHandle = currentUser.handle,
            authorAvatarUrl = currentUser.avatarUrl,
            isVerified = currentUser.isVerified,
            content = content,
            timestamp = System.currentTimeMillis(),
            category = category,
            mediaDrawableRes = mediaDrawableRes,
            likesCount = 0,
            repostsCount = 0,
            repliesCount = 0,
            viewsCount = 1,
            isLiked = false,
            isReposted = false,
            isBookmarked = false,
            syncedWithSupabase = false
        )

        postDao.insertPost(newPost)

        // Try pushing to Supabase if connected
        val api = supabaseManager.getApi()
        if (api != null) {
            try {
                val dto = SupabasePostDto(
                    id = newPost.id,
                    authorId = newPost.authorId,
                    authorName = newPost.authorName,
                    authorHandle = newPost.authorHandle,
                    authorAvatarUrl = newPost.authorAvatarUrl,
                    isVerified = newPost.isVerified,
                    content = newPost.content,
                    category = newPost.category,
                    mediaUrl = null,
                    likesCount = 0,
                    repostsCount = 0,
                    repliesCount = 0,
                    viewsCount = 1
                )
                val response = api.createPost(dto)
                if (response.isSuccessful) {
                    postDao.insertPost(newPost.copy(syncedWithSupabase = true))
                }
            } catch (e: Exception) {
                Log.w("PanchayatRepo", "Failed to sync new post to Supabase: ${e.message}")
            }
        }

        newPost
    }

    suspend fun toggleLike(postId: String) = withContext(Dispatchers.IO) {
        val post = postDao.getPostById(postId) ?: return@withContext
        val newLiked = !post.isLiked
        val newLikesCount = if (newLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
        postDao.updateLikeStatus(postId, newLiked, newLikesCount)

        try {
            val api = supabaseManager.getApi() ?: SupabaseClient.api
            api.updatePostLikes(
                idFilter = "eq.$postId",
                body = SupabaseUpdateLikesDto(likesCount = newLikesCount)
            )
        } catch (e: Exception) {
            Log.w("PanchayatRepo", "Failed to sync like count to Supabase: ${e.message}")
        }
    }

    suspend fun toggleRepost(postId: String) = withContext(Dispatchers.IO) {
        val post = postDao.getPostById(postId) ?: return@withContext
        val newReposted = !post.isReposted
        val newRepostsCount = if (newReposted) post.repostsCount + 1 else maxOf(0, post.repostsCount - 1)
        postDao.updateRepostStatus(postId, newReposted, newRepostsCount)
    }

    suspend fun toggleBookmark(postId: String) = withContext(Dispatchers.IO) {
        val post = postDao.getPostById(postId) ?: return@withContext
        postDao.updateBookmarkStatus(postId, !post.isBookmarked)
    }

    suspend fun addReply(postId: String, content: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUserDirect() ?: SeedData.currentUser
        val newReply = ReplyEntity(
            id = UUID.randomUUID().toString(),
            postId = postId,
            authorId = currentUser.id,
            authorName = currentUser.name,
            authorHandle = currentUser.handle,
            authorAvatarUrl = currentUser.avatarUrl,
            isVerified = currentUser.isVerified,
            content = content,
            timestamp = System.currentTimeMillis(),
            likesCount = 0,
            isLiked = false
        )
        postDao.insertReply(newReply)
        postDao.incrementReplyCount(postId)
    }

    suspend fun toggleFollow(userId: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId) ?: return@withContext
        val newFollowing = !user.isFollowing
        val diff = if (newFollowing) 1 else -1
        userDao.updateFollowStatus(userId, newFollowing, diff)
    }

    suspend fun updateCurrentUserProfile(name: String, bio: String, location: String) = withContext(Dispatchers.IO) {
        val current = userDao.getCurrentUserDirect() ?: SeedData.currentUser
        val updated = current.copy(
            name = name,
            bio = bio,
            location = location
        )
        userDao.insertUser(updated)
    }

    suspend fun markNotificationsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    suspend fun syncRemoteSupabase(): Result<Int> = withContext(Dispatchers.IO) {
        val api = supabaseManager.getApi()
            ?: return@withContext Result.failure(Exception("Supabase is not configured or in Local Mode."))

        try {
            val response = api.getPosts(limit = 50)
            if (response.isSuccessful) {
                val remotePosts = response.body() ?: emptyList()
                val entities = remotePosts.map { dto ->
                    PostEntity(
                        id = dto.id,
                        authorId = dto.authorId,
                        authorName = dto.authorName,
                        authorHandle = dto.authorHandle,
                        authorAvatarUrl = dto.authorAvatarUrl,
                        isVerified = dto.isVerified,
                        content = dto.content,
                        timestamp = System.currentTimeMillis(),
                        category = dto.category,
                        mediaUrl = dto.mediaUrl,
                        likesCount = dto.likesCount,
                        repostsCount = dto.repostsCount,
                        repliesCount = dto.repliesCount,
                        viewsCount = dto.viewsCount,
                        isLiked = false,
                        isReposted = false,
                        isBookmarked = false,
                        syncedWithSupabase = true
                    )
                }
                if (entities.isNotEmpty()) {
                    postDao.insertPosts(entities)
                }
                Result.success(entities.size)
            } else {
                Result.failure(Exception("Supabase query failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val authState: Flow<AuthState> = SupabaseClient.auth.authState

    suspend fun signUpWithSupabase(
        email: String,
        password: String,
        name: String = "",
        handle: String = ""
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        val result = SupabaseClient.signUp(email, password, name, handle)
        result.onSuccess { response ->
            response.user?.let { userDto ->
                val entity = UserProfileEntity(
                    id = userDto.id,
                    name = name.ifBlank { userDto.displayName },
                    handle = handle.ifBlank { userDto.displayHandle },
                    bio = "Panchayat member exploring local civic governance",
                    location = "Phulera, Ballia",
                    joinedDate = "Joined recently",
                    followingCount = 0,
                    followersCount = 0,
                    isVerified = false,
                    isCurrentUser = true
                )
                userDao.insertUser(entity)
            }
        }
        result
    }

    suspend fun signInWithSupabase(
        email: String,
        password: String
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        val result = SupabaseClient.signIn(email, password)
        result.onSuccess { response ->
            response.user?.let { userDto ->
                val existing = userDao.getUserById(userDto.id)
                val entity = existing?.copy(isCurrentUser = true) ?: UserProfileEntity(
                    id = userDto.id,
                    name = userDto.displayName,
                    handle = userDto.displayHandle,
                    bio = "Panchayat member exploring local civic governance",
                    location = "Phulera, Ballia",
                    joinedDate = "Joined recently",
                    followingCount = 0,
                    followersCount = 0,
                    isVerified = false,
                    isCurrentUser = true
                )
                userDao.insertUser(entity)
            }
        }
        result
    }

    suspend fun signOutSupabase(): Result<Unit> = withContext(Dispatchers.IO) {
        SupabaseClient.signOut()
    }
}
