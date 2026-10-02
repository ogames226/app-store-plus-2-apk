package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppItem
import com.example.model.CategoryItem
import com.example.ui.components.BottomNavBar
import com.example.ui.components.NavTab
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AppDetailsScreen
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.AuthDialog
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.UpdatesScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val language by viewModel.language.collectAsStateWithLifecycle()
            val primaryColorHex by viewModel.primaryColorHex.collectAsStateWithLifecycle()
            val cardStyle by viewModel.cardStyle.collectAsStateWithLifecycle()

            val primaryColor = try {
                Color(android.graphics.Color.parseColor(primaryColorHex))
            } catch (e: Exception) {
                Color(0xFF3B82F6)
            }

            val layoutDirection = if (language == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

            MyApplicationTheme(
                themeMode = themeMode,
                customPrimaryColor = primaryColor,
                cardStyle = cardStyle
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    AppStorePlusApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppStorePlusApp(viewModel: StoreViewModel) {
    val currentScreen by viewModel.screen.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val allPublishedApps by viewModel.allPublishedApps.collectAsStateWithLifecycle()
    val allAppsForAdmin by viewModel.allAppsForAdmin.collectAsStateWithLifecycle()
    val appsOnly by viewModel.appsOnly.collectAsStateWithLifecycle()
    val gamesOnly by viewModel.gamesOnly.collectAsStateWithLifecycle()
    val featuredGames by viewModel.featuredGames.collectAsStateWithLifecycle()
    val mostDownloadedApps by viewModel.mostDownloadedApps.collectAsStateWithLifecycle()
    val updatesList by viewModel.updatesList.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()

    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val primaryColorHex by viewModel.primaryColorHex.collectAsStateWithLifecycle()
    val cardStyle by viewModel.cardStyle.collectAsStateWithLifecycle()
    val coAdmins by viewModel.coAdmins.collectAsStateWithLifecycle()

    var showAuthDialog by remember { mutableStateOf(false) }
    var selectedCategoryForFilter by remember { mutableStateOf<CategoryItem?>(null) }

    when (val screen = currentScreen) {
        StoreScreen.Splash -> {
            SplashScreen(
                onStartClick = { viewModel.navigateTo(StoreScreen.Main) },
                onSkipClick = { viewModel.navigateTo(StoreScreen.Main) }
            )
        }

        is StoreScreen.AppDetails -> {
            AppDetailsScreen(
                app = screen.app,
                downloadItem = downloads[screen.app.id],
                onBackClick = { viewModel.navigateBack() },
                onInstallApp = { viewModel.installApp(it) },
                onCancelDownload = { viewModel.cancelDownload(it) },
                onOpenApp = { viewModel.openApp(it) }
            )
        }

        StoreScreen.Search -> {
            SearchScreen(
                allApps = allPublishedApps,
                downloads = downloads,
                onAppClick = { viewModel.navigateTo(StoreScreen.AppDetails(it)) },
                onInstallApp = { viewModel.installApp(it) },
                onCancelDownload = { viewModel.cancelDownload(it) },
                onOpenApp = { viewModel.openApp(it) },
                onBackClick = { viewModel.navigateBack() }
            )
        }

        StoreScreen.AdminDashboard -> {
            AdminDashboardScreen(
                apps = allAppsForAdmin,
                categories = categories,
                coAdmins = coAdmins,
                onSaveApp = { viewModel.saveApp(it) },
                onDeleteApp = { viewModel.deleteApp(it) },
                onTogglePublish = { id, pub -> viewModel.togglePublish(id, pub) },
                onSaveCategory = { viewModel.saveCategory(it) },
                onDeleteCategory = { viewModel.deleteCategory(it) },
                onAddCoAdmin = { viewModel.addCoAdmin(it) },
                onRemoveCoAdmin = { viewModel.removeCoAdmin(it) },
                onBackClick = { viewModel.navigateBack() }
            )
        }

        StoreScreen.Settings -> {
            SettingsScreen(
                currentThemeMode = themeMode,
                currentLanguage = language,
                currentPrimaryColor = primaryColorHex,
                currentCardStyle = cardStyle,
                onSelectThemeMode = { viewModel.setThemeMode(it) },
                onSelectLanguage = { viewModel.setLanguage(it) },
                onSelectPrimaryColor = { viewModel.setPrimaryColor(it) },
                onSelectCardStyle = { viewModel.setCardStyle(it) },
                onBackClick = { viewModel.navigateBack() }
            )
        }

        is StoreScreen.CategoryApps -> {
            AppsScreen(
                apps = allPublishedApps,
                downloads = downloads,
                isGamesOnly = screen.category.isGameCategory,
                initialCategory = screen.category.title,
                onAppClick = { viewModel.navigateTo(StoreScreen.AppDetails(it)) },
                onInstallApp = { viewModel.installApp(it) },
                onCancelDownload = { viewModel.cancelDownload(it) },
                onOpenApp = { viewModel.openApp(it) }
            )
            BackHandler {
                viewModel.navigateBack()
            }
        }

        StoreScreen.Main -> {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg),
                bottomBar = {
                    BottomNavBar(
                        selectedTab = selectedTab,
                        updatesCount = updatesList.size,
                        onTabSelected = { tab -> viewModel.selectTab(tab) }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        NavTab.HOME -> {
                            val heroGame = featuredGames.firstOrNull() ?: allPublishedApps.firstOrNull { it.isGame }
                            HomeScreen(
                                featuredGame = heroGame,
                                mostDownloadedApps = mostDownloadedApps,
                                featuredGames = featuredGames,
                                downloads = downloads,
                                onAppClick = { viewModel.navigateTo(StoreScreen.AppDetails(it)) },
                                onInstallApp = { viewModel.installApp(it) },
                                onCancelDownload = { viewModel.cancelDownload(it) },
                                onOpenApp = { viewModel.openApp(it) },
                                onSearchClick = { viewModel.navigateTo(StoreScreen.Search) },
                                onNavigateToUpdates = { viewModel.selectTab(NavTab.UPDATES) },
                                onNavigateToApps = { viewModel.selectTab(NavTab.APPS) },
                                onNavigateToGames = { viewModel.selectTab(NavTab.GAMES) },
                                onNavigateToCategories = {
                                    selectedCategoryForFilter = null
                                    viewModel.selectTab(NavTab.APPS)
                                },
                                onNavigateToAdmin = { viewModel.navigateTo(StoreScreen.AdminDashboard) },
                                onProfileClick = { viewModel.selectTab(NavTab.MORE) }
                            )
                        }

                        NavTab.GAMES -> {
                            AppsScreen(
                                apps = gamesOnly,
                                downloads = downloads,
                                isGamesOnly = true,
                                onAppClick = { viewModel.navigateTo(StoreScreen.AppDetails(it)) },
                                onInstallApp = { viewModel.installApp(it) },
                                onCancelDownload = { viewModel.cancelDownload(it) },
                                onOpenApp = { viewModel.openApp(it) }
                            )
                        }

                        NavTab.APPS -> {
                            if (selectedCategoryForFilter != null) {
                                AppsScreen(
                                    apps = allPublishedApps,
                                    downloads = downloads,
                                    initialCategory = selectedCategoryForFilter?.title,
                                    onAppClick = { viewModel.navigateTo(StoreScreen.AppDetails(it)) },
                                    onInstallApp = { viewModel.installApp(it) },
                                    onCancelDownload = { viewModel.cancelDownload(it) },
                                    onOpenApp = { viewModel.openApp(it) }
                                )
                                BackHandler {
                                    selectedCategoryForFilter = null
                                }
                            } else {
                                CategoriesScreen(
                                    categories = categories,
                                    onCategoryClick = { cat ->
                                        selectedCategoryForFilter = cat
                                    }
                                )
                            }
                        }

                        NavTab.UPDATES -> {
                            UpdatesScreen(
                                updates = updatesList,
                                downloads = downloads,
                                onAppClick = { viewModel.navigateTo(StoreScreen.AppDetails(it)) },
                                onUpdateApp = { viewModel.installApp(it) },
                                onCancelDownload = { viewModel.cancelDownload(it) },
                                onOpenApp = { viewModel.openApp(it) }
                            )
                        }

                        NavTab.MORE -> {
                            ProfileScreen(
                                userProfile = currentUser,
                                onNavigateToAdmin = { viewModel.navigateTo(StoreScreen.AdminDashboard) },
                                onNavigateToSettings = { viewModel.navigateTo(StoreScreen.Settings) },
                                onSignOutClick = { viewModel.signOut() },
                                onSignInClick = { showAuthDialog = true }
                            )
                        }
                    }
                }
            }

            BackHandler(enabled = selectedTab != NavTab.HOME) {
                viewModel.selectTab(NavTab.HOME)
            }
        }
    }

    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            onSignIn = { email, pass -> viewModel.signIn(email, pass) },
            onSignUp = { email, pass, name -> viewModel.signUp(email, pass, name) },
            onGoogleSignIn = { email, name -> viewModel.signInWithGoogle(email, name) },
            onAuthSuccess = { }
        )
    }
}
