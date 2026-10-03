package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.data.ApkInstaller
import com.example.data.DownloadManager
import com.example.data.StoreRepository
import com.example.model.CategoryItem
import com.example.model.DownloadProgress
import com.example.model.StoreItem
import com.example.model.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Load state of the store catalog. */
sealed class CatalogState {
  object Loading : CatalogState()
  /** Backend reachable, collection genuinely has no published apps. */
  object Empty : CatalogState()
  data class Ready(val itemCount: Int) : CatalogState()
  data class Error(val message: String) : CatalogState()
}

/** One-shot signals from the repository that the UI should react to. */
sealed class CatalogEvent {
  data class Error(val message: String) : CatalogEvent()
}

sealed class ScreenDestination {
  object Splash : ScreenDestination()
  object Home : ScreenDestination()
  object Games : ScreenDestination()
  object Apps : ScreenDestination()
  object Updates : ScreenDestination()
  object Categories : ScreenDestination()
  object Profile : ScreenDestination()
  data class Detail(val item: StoreItem) : ScreenDestination()
  object Admin : ScreenDestination()

  /** Destinations reachable from the bottom bar; tapping one resets the back stack. */
  val isRoot: Boolean
    get() = this is Home || this is Games || this is Apps || this is Updates || this is Profile
}

class StoreViewModel(application: Application) : AndroidViewModel(application) {

  private val storeRepo = StoreRepository(application)
  private val authRepo = AuthRepository(application)

  private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Splash)
  val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

  /**
   * Navigation used to be a single `currentScreen` value, so system-back from
   * *any* screen jumped straight to Home — leaving Apps via a Detail and
   * pressing back lost your place. A real stack fixes that and gives the
   * bottom bar its standard "reset to tab root" behaviour.
   *
   * Kept out of Compose state on purpose: this survives configuration changes
   * via the ViewModel, but is not part of saved instance state (we re-seed from
   * Firestore on process death, which is the correct resume behaviour here).
   */
  private val backStack = ArrayDeque<ScreenDestination>()

  val searchQuery = MutableStateFlow("")
  val selectedCategoryFilter = MutableStateFlow("الكل")

  val currentUser: StateFlow<UserAccount?> = authRepo.observeCurrentUser()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepo.getCurrentUser())

  val downloadStates: StateFlow<Map<String, DownloadProgress>> = DownloadManager.downloads

  /**
   * One upstream read of `apps/`, shared by the public catalog and the admin
   * list. Previously both registered their own snapshot listener on the same
   * collection and both were subscribed from app launch — two redundant
   * main-thread listener registrations for one collection.
   *
   * `.flowOn(Dispatchers.IO)` matters: viewModelScope is Main.immediate, and
   * Firestore listener registration plus the fallback-catalog build were
   * happening on the UI thread.
   */
  private val allItems: Flow<List<StoreItem>> =
    storeRepo.getAllItems().flowOn(Dispatchers.IO)

  /**
   * Customer-facing catalog source.
   *
   * [allItems] is unbounded because the admin dashboard needs the complete list.
   * Normal users get a bounded, server-ordered listener instead, so a phone does
   * not download an entire store on install and on every reconnect.
   */
  private val publishedItems: Flow<List<StoreItem>> =
    storeRepo.getPublishedItems().flowOn(Dispatchers.IO)

  val storeItems: StateFlow<List<StoreItem>> = publishedItems
    .map { items -> items.filter { it.isPublished } }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  /**
   * Derived purely from [storeItems] plus the repository's error signal, so
   * there is a single source of truth. An error wins; otherwise a non-empty
   * catalog is Ready; an empty one stays Loading only until the first emission
   * proves the collection is genuinely empty.
   */
  init {
    viewModelScope.launch {
      combine(storeItems, storeRepo.catalogEvents) { items, event ->
        when {
          event is CatalogEvent.Error -> CatalogState.Error(event.message)
          items.isNotEmpty() -> CatalogState.Ready(items.size)
          else -> CatalogState.Empty
        }
      }.collect { _catalogState.value = it }
    }
  }

  val adminItems: StateFlow<List<StoreItem>> = allItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val categories: StateFlow<List<CategoryItem>> = storeRepo.getCategories()
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  /**
   * Catalog load state. The UI needs to distinguish "still loading" from
   * "loaded, genuinely empty" from "backend unreachable" — collapsing these
   * is what previously let the hardcoded demo apps stand in for real data.
   */
  private val _catalogState: MutableStateFlow<CatalogState> =
    MutableStateFlow(CatalogState.Loading)
  val catalogState: StateFlow<CatalogState> = _catalogState.asStateFlow()

  init {
    viewModelScope.launch(Dispatchers.IO) {
      storeRepo.seedInitialDataIfEmpty()
    }
    viewModelScope.launch {
      storeRepo.catalogEvents.collect { event ->
        if (event is CatalogEvent.Error) {
          _catalogState.value = CatalogState.Error(event.message)
        }
      }
    }
  }

  // Filtered Apps list based on search and category filter.
  // `distinctUntilChanged` keeps identical recompositions off the hot path when
  // e.g. only an unrelated item's download state changed.
  val filteredApps = combine(storeItems, searchQuery, selectedCategoryFilter) { items, query, cat ->
    items.filter { item ->
      item.type == "app" &&
          (cat == "الكل" || item.category == cat) &&
          (query.isBlank() || item.name.contains(query, ignoreCase = true) || item.developer.contains(query, ignoreCase = true))
    }
  }.distinctUntilChanged()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Filtered Games list
  val filteredGames = combine(storeItems, searchQuery) { items, query ->
    items.filter { item ->
      item.type == "game" &&
          (query.isBlank() || item.name.contains(query, ignoreCase = true) || item.developer.contains(query, ignoreCase = true))
    }
  }.distinctUntilChanged()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Updates list. The original used `storeItems.combine(storeItems) { items, _ -> ... }`,
  // which subscribes to the same source twice for no reason.
  val updateItems = storeItems
    .map { items -> items.filter { it.isUpdateAvailable } }
    .distinctUntilChanged()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  /**
   * Navigate forward. Bottom-bar destinations clear the stack (standard tab
   * behaviour) so back from Apps returns to wherever you launched the app, not
   * to a stale Detail screen.
   */
  fun navigateTo(destination: ScreenDestination) {
    val current = _currentScreen.value
    if (current == destination) return

    if (destination.isRoot) {
      backStack.clear()
    } else if (current !is ScreenDestination.Splash) {
      backStack.addLast(current)
    }
    _currentScreen.value = destination
  }

  /** @return true if a screen was popped; false if the caller should finish the activity. */
  fun navigateBack(): Boolean {
    val previous = backStack.removeLastOrNull() ?: return false
    _currentScreen.value = previous
    return true
  }

  fun canNavigateBack(): Boolean = backStack.isNotEmpty()

  fun startDownload(item: StoreItem) {
    if (DownloadManager.isDownloading(item.id)) return
    DownloadManager.startDownload(item)
    _toastMessage.value = "بدأ تحميل ${item.name}..."
  }

  /**
   * Hand a finished download to the system package installer.
   *
   * @return false when the APK isn't on disk, so the caller can surface a
   *         message instead of silently doing nothing.
   */
  fun installDownload(appId: String): Boolean {
    val ok = ApkInstaller.install(getApplication(), appId)
    if (!ok) _toastMessage.value = "تعذر فتح ملف التثبيت"
    return ok
  }

  fun resetDownload(appId: String) = DownloadManager.reset(appId)

  /**
   * Appends the next page of the catalog.
   *
   * Firestore listeners cannot page, so this is an explicit paged query whose
   * results are merged into [extraItems]. No-ops once a short page comes back,
   * which is how the end of the catalog is detected.
   */
  fun loadMoreItems(pageSize: Int = 50) {
    if (_isLoadingMore.value) return
    _isLoadingMore.value = true
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val cursor = _extraItems.value.lastOrNull().takeIf { _extraItems.value.isNotEmpty() }
        val page = storeRepo.getItemsPage(pageSize = pageSize, startAfter = cursor)
        _extraItems.update { existing -> existing + page }
        _hasMoreItems.value = page.size == pageSize
      } finally {
        _isLoadingMore.value = false
      }
    }
  }

  /** Pages loaded on top of the bounded listener, newest first. */
  private val _extraItems = MutableStateFlow<List<StoreItem>>(emptyList())
  val extraItems: StateFlow<List<StoreItem>> = _extraItems.asStateFlow()

  private val _isLoadingMore = MutableStateFlow(false)
  val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

  private val _hasMoreItems = MutableStateFlow(true)
  val hasMoreItems: StateFlow<Boolean> = _hasMoreItems.asStateFlow()

  fun cancelDownload(appId: String) {
    DownloadManager.cancelDownload(appId)
  }

  fun signInWithGoogle() {
    viewModelScope.launch {
      val result = authRepo.signInWithGoogle()
      result.onSuccess {
        _toastMessage.value = "تم تسجيل الدخول بنجاح!"
      }.onFailure { e ->
        _toastMessage.value = "فشل تسجيل الدخول: ${e.localizedMessage}"
      }
    }
  }

  fun signOut() {
    authRepo.signOut()
    _toastMessage.value = "تم تسجيل الخروج"
  }

  fun clearToast() {
    _toastMessage.value = null
  }

  // Admin Operations
  fun saveStoreItem(item: StoreItem, onDone: () -> Unit) {
    viewModelScope.launch {
      val success = storeRepo.saveStoreItem(item)
      if (success) {
        _toastMessage.value = "تم حفظ التطبيق/اللعبة بنجاح في المتجر"
        onDone()
      } else {
        _toastMessage.value = "حدث خطأ أثناء الحفظ"
      }
    }
  }

  fun deleteStoreItem(id: String) {
    viewModelScope.launch {
      val success = storeRepo.deleteStoreItem(id)
      if (success) {
        _toastMessage.value = "تم حذف العنصر بنجاح"
      } else {
        _toastMessage.value = "فشل حذف العنصر"
      }
    }
  }

  fun togglePublish(id: String, currentStatus: Boolean) {
    viewModelScope.launch {
      storeRepo.togglePublish(id, currentStatus)
    }
  }

  fun toggleFeatured(id: String, currentStatus: Boolean) {
    viewModelScope.launch {
      storeRepo.toggleFeatured(id, currentStatus)
    }
  }

  fun saveCategory(category: CategoryItem, onDone: () -> Unit) {
    viewModelScope.launch {
      val success = storeRepo.saveCategory(category)
      if (success) {
        _toastMessage.value = "تم حفظ التصنيف بنجاح"
        onDone()
      }
    }
  }

  fun deleteCategory(id: String) {
    viewModelScope.launch {
      storeRepo.deleteCategory(id)
    }
  }
}
