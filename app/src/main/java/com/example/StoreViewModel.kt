package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SettingsManager
import com.example.data.local.ThemeMode
import com.example.data.repository.DownloadRepository
import com.example.data.repository.StoreRepository
import com.example.model.AdminMember
import com.example.model.AppItem
import com.example.model.CategoryItem
import com.example.model.DownloadItem
import com.example.model.UserProfile
import com.example.ui.components.NavTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class StoreScreen {
    object Splash : StoreScreen()
    object Main : StoreScreen()
    data class AppDetails(val app: AppItem) : StoreScreen()
    object Search : StoreScreen()
    object AdminDashboard : StoreScreen()
    object Settings : StoreScreen()
    data class CategoryApps(val category: CategoryItem) : StoreScreen()
}

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val storeRepository = StoreRepository.getInstance(application)
    private val downloadRepository = DownloadRepository.getInstance(application)
    private val settingsManager = SettingsManager.getInstance(application)

    // Navigation state
    private val _screen = MutableStateFlow<StoreScreen>(StoreScreen.Splash)
    val screen: StateFlow<StoreScreen> = _screen.asStateFlow()

    private val _selectedTab = MutableStateFlow(NavTab.HOME)
    val selectedTab: StateFlow<NavTab> = _selectedTab.asStateFlow()

    // Settings & Theme
    val themeMode: StateFlow<ThemeMode> = settingsManager.themeMode
    val language: StateFlow<String> = settingsManager.language
    val primaryColorHex: StateFlow<String> = settingsManager.primaryColorHex
    val cardStyle: StateFlow<String> = settingsManager.cardStyle
    val coAdmins: StateFlow<List<AdminMember>> = settingsManager.coAdmins

    fun setThemeMode(mode: ThemeMode) = settingsManager.setThemeMode(mode)
    fun setLanguage(lang: String) = settingsManager.setLanguage(lang)
    fun setPrimaryColor(hex: String) = settingsManager.setPrimaryColor(hex)
    fun setCardStyle(style: String) = settingsManager.setCardStyle(style)
    fun addCoAdmin(member: AdminMember) = settingsManager.addCoAdmin(member)
    fun removeCoAdmin(email: String) = settingsManager.removeCoAdmin(email)

    // User session - Primary Admin is zaim9002@gmail.com
    private val _currentUser = MutableStateFlow(
        UserProfile(
            uid = "user_zaim",
            displayName = "Zaim Admin",
            email = "zaim9002@gmail.com",
            isAdmin = true
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    init {
        // Ensure no fake/demo apps remain: purge any past demo content
        viewModelScope.launch {
            // Check if any apps are demo apps
            val apps = storeRepository.getAppById("game_gtav")
            if (apps != null) {
                storeRepository.clearAllApps()
            }
        }
    }

    // Store data flows
    val allPublishedApps: StateFlow<List<AppItem>> = storeRepository.allPublishedApps
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allAppsForAdmin: StateFlow<List<AppItem>> = storeRepository.allAppsForAdmin
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val appsOnly: StateFlow<List<AppItem>> = storeRepository.appsOnly
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val gamesOnly: StateFlow<List<AppItem>> = storeRepository.gamesOnly
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val featuredGames: StateFlow<List<AppItem>> = storeRepository.featuredGames
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mostDownloadedApps: StateFlow<List<AppItem>> = storeRepository.mostDownloadedApps
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val updatesList: StateFlow<List<AppItem>> = storeRepository.updatesList
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories: StateFlow<List<CategoryItem>> = storeRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val downloads: StateFlow<Map<String, DownloadItem>> = downloadRepository.downloads

    // Navigation actions
    fun navigateTo(screen: StoreScreen) {
        _screen.value = screen
    }

    fun selectTab(tab: NavTab) {
        _selectedTab.value = tab
        _screen.value = StoreScreen.Main
    }

    fun navigateBack() {
        when (_screen.value) {
            is StoreScreen.AppDetails,
            is StoreScreen.Search,
            is StoreScreen.AdminDashboard,
            is StoreScreen.Settings,
            is StoreScreen.CategoryApps -> {
                _screen.value = StoreScreen.Main
            }
            is StoreScreen.Main -> {
                if (_selectedTab.value != NavTab.HOME) {
                    _selectedTab.value = NavTab.HOME
                }
            }
            StoreScreen.Splash -> {}
        }
    }

    // Downloads
    fun installApp(app: AppItem) {
        downloadRepository.startDownload(app)
    }

    fun cancelDownload(appId: String) {
        downloadRepository.cancelDownload(appId)
    }

    fun openApp(app: AppItem) {
        val download = downloads.value[app.id]
        if (download?.localFilePath != null) {
            downloadRepository.installApk(java.io.File(download.localFilePath))
        }
    }

    // Admin operations
    fun saveApp(app: AppItem) {
        viewModelScope.launch {
            storeRepository.saveApp(app)
        }
    }

    fun deleteApp(appId: String) {
        viewModelScope.launch {
            storeRepository.deleteApp(appId)
        }
    }

    fun togglePublish(appId: String, isPublished: Boolean) {
        viewModelScope.launch {
            storeRepository.togglePublish(appId, isPublished)
        }
    }

    fun saveCategory(category: CategoryItem) {
        viewModelScope.launch {
            storeRepository.saveCategory(category)
        }
    }

    fun deleteCategory(category: CategoryItem) {
        viewModelScope.launch {
            storeRepository.deleteCategory(category)
        }
    }

    // Auth
    suspend fun signInWithGoogle(email: String, name: String): UserProfile? {
        val user = storeRepository.firebaseManager.signInWithGoogle("", email, name)
        if (user != null) {
            _currentUser.value = user
        }
        return user
    }

    suspend fun signIn(email: String, pass: String): UserProfile? {
        val user = storeRepository.firebaseManager.signIn(email, pass)
        if (user != null) {
            _currentUser.value = user
        }
        return user
    }

    suspend fun signUp(email: String, pass: String, name: String): UserProfile? {
        val user = storeRepository.firebaseManager.signUp(email, pass, name)
        if (user != null) {
            _currentUser.value = user
        }
        return user
    }

    fun signOut() {
        storeRepository.firebaseManager.signOut()
        _currentUser.value = UserProfile(
            uid = "guest_${System.currentTimeMillis()}",
            displayName = "ضيف المتجر",
            email = "guest@appstoreplus.com",
            isAdmin = false
        )
    }
}
