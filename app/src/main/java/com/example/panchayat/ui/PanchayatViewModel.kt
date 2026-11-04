package com.example.panchayat.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.panchayat.data.local.PanchayatDatabase
import com.example.panchayat.data.local.entity.NotificationEntity
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.ReplyEntity
import com.example.panchayat.data.local.entity.UserProfileEntity
import com.example.panchayat.data.remote.SupabaseClientManager
import com.example.panchayat.data.remote.SupabaseStatus
import com.example.panchayat.data.remote.auth.AuthResponse
import com.example.panchayat.data.remote.auth.AuthState
import com.example.panchayat.data.repository.PanchayatRepository
import com.example.panchayat.data.repository.SeedData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FeedTab {
    FOR_YOU,
    FOLLOWING,
    CIVIC_PANCHAYAT,
    SUPABASE_FEED
}

enum class NavigationItem {
    HOME,
    EXPLORE,
    NOTIFICATIONS,
    PROFILE
}

class PanchayatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PanchayatDatabase.getDatabase(application)
    val supabaseManager = SupabaseClientManager(application)
    private val repository = PanchayatRepository(database, supabaseManager)

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow(NavigationItem.HOME)
    val currentScreen: StateFlow<NavigationItem> = _currentScreen.asStateFlow()

    // Home Feed Tab
    private val _feedTab = MutableStateFlow(FeedTab.FOR_YOU)
    val feedTab: StateFlow<FeedTab> = _feedTab.asStateFlow()

    // All posts
    val allPosts: StateFlow<List<PostEntity>> = repository.getAllPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Civic posts
    val civicPosts: StateFlow<List<PostEntity>> = repository.getCivicPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current user
    val currentUser: StateFlow<UserProfileEntity?> = repository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedData.currentUser)

    // Suggested accounts to follow
    val suggestedUsers: StateFlow<List<UserProfileEntity>> = repository.getSuggestedUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications: StateFlow<List<NotificationEntity>> = repository.getNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = repository.getUnreadNotificationCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Search query & results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<PostEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) flowOf(emptyList()) else repository.searchPosts(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active post for thread view
    private val _selectedPost = MutableStateFlow<PostEntity?>(null)
    val selectedPost: StateFlow<PostEntity?> = _selectedPost.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val postReplies: StateFlow<List<ReplyEntity>> = _selectedPost.flatMapLatest { post ->
        if (post == null) flowOf(emptyList()) else repository.getRepliesForPost(post.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Modals & sheets state
    private val _isComposeOpen = MutableStateFlow(false)
    val isComposeOpen: StateFlow<Boolean> = _isComposeOpen.asStateFlow()

    private val _isSupabaseSheetOpen = MutableStateFlow(false)
    val isSupabaseSheetOpen: StateFlow<Boolean> = _isSupabaseSheetOpen.asStateFlow()

    private val _isPostDetailOpen = MutableStateFlow(false)
    val isPostDetailOpen: StateFlow<Boolean> = _isPostDetailOpen.asStateFlow()

    private val _isEditProfileOpen = MutableStateFlow(false)
    val isEditProfileOpen: StateFlow<Boolean> = _isEditProfileOpen.asStateFlow()

    // Sync state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    val supabaseStatus: StateFlow<SupabaseStatus> = supabaseManager.status

    val authState: StateFlow<AuthState> = repository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Unauthenticated)

    init {
        viewModelScope.launch {
            repository.initializeIfNeeded()
        }
    }

    fun setScreen(screen: NavigationItem) {
        _currentScreen.value = screen
        if (screen == NavigationItem.NOTIFICATIONS) {
            viewModelScope.launch {
                repository.markNotificationsRead()
            }
        }
    }

    fun setFeedTab(tab: FeedTab) {
        _feedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openCompose() {
        _isComposeOpen.value = true
    }

    fun closeCompose() {
        _isComposeOpen.value = false
    }

    fun openSupabaseSheet() {
        _isSupabaseSheetOpen.value = true
    }

    fun closeSupabaseSheet() {
        _isSupabaseSheetOpen.value = false
    }

    fun openPostDetail(post: PostEntity) {
        _selectedPost.value = post
        _isPostDetailOpen.value = true
    }

    fun closePostDetail() {
        _isPostDetailOpen.value = false
        _selectedPost.value = null
    }

    fun openEditProfile() {
        _isEditProfileOpen.value = true
    }

    fun closeEditProfile() {
        _isEditProfileOpen.value = false
    }

    fun createPost(content: String, category: String, mediaRes: Int? = null) {
        viewModelScope.launch {
            repository.createPost(content, category, mediaRes)
            _isComposeOpen.value = false
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleLike(postId)
            // Update selected post if currently viewing detail
            val current = _selectedPost.value
            if (current?.id == postId) {
                val newLiked = !current.isLiked
                val newCount = if (newLiked) current.likesCount + 1 else maxOf(0, current.likesCount - 1)
                _selectedPost.value = current.copy(isLiked = newLiked, likesCount = newCount)
            }
        }
    }

    fun toggleRepost(postId: String) {
        viewModelScope.launch {
            repository.toggleRepost(postId)
            val current = _selectedPost.value
            if (current?.id == postId) {
                val newReposted = !current.isReposted
                val newCount = if (newReposted) current.repostsCount + 1 else maxOf(0, current.repostsCount - 1)
                _selectedPost.value = current.copy(isReposted = newReposted, repostsCount = newCount)
            }
        }
    }

    fun toggleBookmark(postId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(postId)
            val current = _selectedPost.value
            if (current?.id == postId) {
                _selectedPost.value = current.copy(isBookmarked = !current.isBookmarked)
            }
        }
    }

    fun addReply(postId: String, content: String) {
        viewModelScope.launch {
            repository.addReply(postId, content)
            val current = _selectedPost.value
            if (current?.id == postId) {
                _selectedPost.value = current.copy(repliesCount = current.repliesCount + 1)
            }
        }
    }

    fun toggleFollow(userId: String) {
        viewModelScope.launch {
            repository.toggleFollow(userId)
        }
    }

    fun updateProfile(name: String, bio: String, location: String) {
        viewModelScope.launch {
            repository.updateCurrentUserProfile(name, bio, location)
            _isEditProfileOpen.value = false
        }
    }

    fun saveSupabaseConfig(url: String, key: String) {
        supabaseManager.updateConfig(url, key)
    }

    fun testSupabaseConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = supabaseManager.testConnection()
            result.onSuccess { msg ->
                onResult(true, msg)
            }.onFailure { err ->
                onResult(false, err.message ?: "Connection failed")
            }
        }
    }

    fun syncSupabasePosts() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Connecting to Supabase PostgREST..."
            val result = repository.syncRemoteSupabase()
            result.onSuccess { count ->
                _syncMessage.value = "Synced $count posts from Supabase database!"
            }.onFailure { err ->
                _syncMessage.value = "Sync note: ${err.message}"
            }
            _isSyncing.value = false
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun signUpWithEmail(
        email: String,
        password: String,
        name: String = "",
        handle: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.signUpWithSupabase(email, password, name, handle)
            result.onSuccess {
                _syncMessage.value = "Account created & signed in to Supabase!"
                onResult(true, "Successfully registered!")
            }.onFailure { err ->
                onResult(false, err.message ?: "Sign up failed")
            }
        }
    }

    fun signInWithEmail(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.signInWithSupabase(email, password)
            result.onSuccess {
                _syncMessage.value = "Welcome back! Signed in to Supabase."
                onResult(true, "Signed in successfully!")
            }.onFailure { err ->
                onResult(false, err.message ?: "Sign in failed")
            }
        }
    }

    fun signOut(onResult: () -> Unit) {
        viewModelScope.launch {
            repository.signOutSupabase()
            _syncMessage.value = "Signed out of Supabase"
            onResult()
        }
    }
}
