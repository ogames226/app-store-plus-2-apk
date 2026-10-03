package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ScreenDestination
import com.example.ui.StoreViewModel
import com.example.ui.components.StoreBottomBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.UpdatesScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          AppStorePlusApp()
        }
      }
    }
  }
}

@Composable
fun AppStorePlusApp(viewModel: StoreViewModel = viewModel()) {
  val context = LocalContext.current
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val storeItems by viewModel.storeItems.collectAsStateWithLifecycle()
  val adminItems by viewModel.adminItems.collectAsStateWithLifecycle()
  val categories by viewModel.categories.collectAsStateWithLifecycle()
  val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
  val filteredGames by viewModel.filteredGames.collectAsStateWithLifecycle()
  val updateItems by viewModel.updateItems.collectAsStateWithLifecycle()
  val downloadStates by viewModel.downloadStates.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedCatFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
  val catalogState by viewModel.catalogState.collectAsStateWithLifecycle()
  val extraItems by viewModel.extraItems.collectAsStateWithLifecycle()
  val hasMoreItems by viewModel.hasMoreItems.collectAsStateWithLifecycle()
  val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  // Toast -> Snackbar. Toasts ignore RTL layout, can't be styled, and cover
  // content on gesture-nav devices.
  LaunchedEffect(toastMessage) {
    toastMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearToast()
    }
  }

  // Increments on bottom-nav re-tap so the active tab can scroll itself home.
  var homeScrollToken by remember { mutableIntStateOf(0) }
  var appsScrollToken by remember { mutableIntStateOf(0) }
  var gamesScrollToken by remember { mutableIntStateOf(0) }
  var updatesScrollToken by remember { mutableIntStateOf(0) }
  var profileScrollToken by remember { mutableIntStateOf(0) }

  val navigateFromBar: (ScreenDestination) -> Unit = { destination ->
    // Re-tapping the active tab is a scroll-to-top, not a no-op.
    when {
      currentScreen is ScreenDestination.Home && destination is ScreenDestination.Home ->
        homeScrollToken++
      currentScreen is ScreenDestination.Apps && destination is ScreenDestination.Apps ->
        appsScrollToken++
      currentScreen is ScreenDestination.Games && destination is ScreenDestination.Games ->
        gamesScrollToken++
      currentScreen is ScreenDestination.Updates && destination is ScreenDestination.Updates ->
        updatesScrollToken++
      currentScreen is ScreenDestination.Profile && destination is ScreenDestination.Profile ->
        profileScrollToken++
    }
    viewModel.navigateTo(destination)
  }

  // Real back stack: pop the stack, and only let the activity finish when the
  // stack is empty. The old global handler always jumped to Home.
  BackHandler(enabled = currentScreen !is ScreenDestination.Splash) {
    if (!viewModel.navigateBack()) {
      (context as? android.app.Activity)?.finish()
    }
  }

  // One place that knows "this item finished downloading -> open the installer".
  val openInstaller: (com.example.model.StoreItem) -> Unit = { item ->
    viewModel.installDownload(item.id)
  }

  val showBottomBar = currentScreen !is ScreenDestination.Splash &&
    currentScreen !is ScreenDestination.Admin &&
    currentScreen !is ScreenDestination.Detail

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = BackgroundDark,
    // Scaffold handles the bottom bar; the top inset is applied to the content
    // below. `enableEdgeToEdge()` + targetSdk 36 draws under the status bar, and
    // nothing was consuming it, so headers sat underneath the clock.
    contentWindowInsets = WindowInsets.safeDrawing.only(
      androidx.compose.foundation.layout.WindowInsetsSides.Horizontal
    ),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (showBottomBar) {
        StoreBottomBar(
          currentScreen = currentScreen,
          updatesCount = updateItems.size,
          onNavigate = navigateFromBar
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .statusBarsPadding()
    ) {
      // No AnimatedContent here on purpose. It keeps both screens composed at
      // once and remeasures/relayouts both every frame, which measured as the
      // slowest interaction in the app (24% janky, 113ms p99 on open). A plain
      // swap composes one tree.
      currentScreen.let { screen ->
        when (screen) {
          is ScreenDestination.Splash -> {
            SplashScreen(
              onStartClick = { viewModel.navigateTo(ScreenDestination.Home) },
              onSkipClick = { viewModel.navigateTo(ScreenDestination.Home) }
            )
          }

          is ScreenDestination.Home -> {
            HomeScreen(
              storeItems = storeItems,
              searchQuery = searchQuery,
              onSearchChange = { viewModel.searchQuery.value = it },
              downloadStates = downloadStates,
              onItemClick = { item -> viewModel.navigateTo(ScreenDestination.Detail(item)) },
              onInstallClick = { item -> viewModel.startDownload(item) },
              onOpenClick = openInstaller,
              onNavigate = { destination -> viewModel.navigateTo(destination) },
              scrollToTopToken = homeScrollToken,
              // Submit (keyboard search) navigates; typing does not. Previously
              // every keystroke tore down Home and lost keyboard focus.
              onSearchSubmit = { query ->
                viewModel.searchQuery.value = query
                if (query.isNotBlank()) viewModel.navigateTo(ScreenDestination.Apps)
              }
            )
          }

          is ScreenDestination.Apps -> {
            AppsScreen(
              apps = filteredApps + extraItems.filter { item ->
                filteredApps.none { it.id == item.id }
              },
              searchQuery = searchQuery,
              onSearchChange = { viewModel.searchQuery.value = it },
              selectedCategory = selectedCatFilter,
              onCategorySelect = { viewModel.selectedCategoryFilter.value = it },
              downloadStates = downloadStates,
              onItemClick = { item -> viewModel.navigateTo(ScreenDestination.Detail(item)) },
              onInstallClick = { item -> viewModel.startDownload(item) },
              onOpenClick = openInstaller,
              scrollToTopToken = appsScrollToken,
              catalogState = catalogState,
              hasMoreItems = hasMoreItems,
              isLoadingMore = isLoadingMore,
              onLoadMore = { viewModel.loadMoreItems() }
            )
          }

          is ScreenDestination.Games -> {
            GamesScreen(
              games = filteredGames,
              searchQuery = searchQuery,
              onSearchChange = { viewModel.searchQuery.value = it },
              downloadStates = downloadStates,
              onItemClick = { item -> viewModel.navigateTo(ScreenDestination.Detail(item)) },
              onInstallClick = { item -> viewModel.startDownload(item) },
              onOpenClick = openInstaller,
              scrollToTopToken = gamesScrollToken
            )
          }

          is ScreenDestination.Categories -> {
            CategoriesScreen(
              categories = categories,
              onCategoryClick = { category ->
                viewModel.selectedCategoryFilter.value = category.name
                if (category.type == "game") {
                  viewModel.navigateTo(ScreenDestination.Games)
                } else {
                  viewModel.navigateTo(ScreenDestination.Apps)
                }
              }
            )
          }

          is ScreenDestination.Updates -> {
            UpdatesScreen(
              updateItems = updateItems,
              downloadStates = downloadStates,
              onItemClick = { item -> viewModel.navigateTo(ScreenDestination.Detail(item)) },
              onUpdateClick = { item -> viewModel.startDownload(item) },
              onOpenClick = openInstaller,
              scrollToTopToken = updatesScrollToken
            )
          }

          is ScreenDestination.Profile -> {
            ProfileScreen(
              user = currentUser,
              onSignInClick = { viewModel.signInWithGoogle() },
              onSignOutClick = { viewModel.signOut() },
              onNavigate = { destination -> viewModel.navigateTo(destination) },
              scrollToTopToken = profileScrollToken
            )
          }

          is ScreenDestination.Detail -> {
            DetailScreen(
              item = screen.item,
              downloadProgress = downloadStates[screen.item.id],
              onBack = { viewModel.navigateBack() },
              onInstallClick = { item -> viewModel.startDownload(item) },
              onOpenClick = { item -> viewModel.installDownload(item.id) }
            )
          }

          is ScreenDestination.Admin -> {
            AdminDashboardScreen(
              items = adminItems,
              categories = categories,
              onBack = { viewModel.navigateBack() },
              onSaveItem = { item, onDone -> viewModel.saveStoreItem(item, onDone) },
              onDeleteItem = { id -> viewModel.deleteStoreItem(id) },
              onTogglePublish = { id, current -> viewModel.togglePublish(id, current) },
              onToggleFeatured = { id, current -> viewModel.toggleFeatured(id, current) },
              onSaveCategory = { cat, onDone -> viewModel.saveCategory(cat, onDone) },
              onDeleteCategory = { id -> viewModel.deleteCategory(id) }
            )
          }
        }
      }
    }
  }
}
