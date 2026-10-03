package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.CategoryItem
import com.example.model.StoreItem
import com.example.ui.CatalogEvent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StoreRepository(private val context: Context) {

  private val tag = "StoreRepository"

  /**
   * Writing the demo catalog into a live Firestore project is opt-in.
   * See [seedInitialDataIfEmpty] for why.
   */
  private val seedDemoDataOnEmpty: Boolean
    get() = SEED_DEMO_DATA_ON_EMPTY

  // Cached so the fallback catalog isn't rebuilt on every emission. Building it
  // involves ~11 object allocations plus android.resource:// string building.
  private val defaultItems by lazy { buildDefaultItems() }
  private val defaultCategories by lazy { buildDefaultCategories() }

  private val _catalogEvents = MutableSharedFlow<CatalogEvent>(extraBufferCapacity = 8)
  /** Error signals for the UI; the demo catalog is never substituted for these. */
  val catalogEvents: SharedFlow<CatalogEvent> = _catalogEvents.asSharedFlow()

  private fun reportError(message: String) {
    Log.w(tag, message)
    _catalogEvents.tryEmit(CatalogEvent.Error(message))
  }

  /**
   * Uses the project's default `(default)` database.
   *
   * This previously read `R.string.firestore_database_id`, which pointed at a
   * *different* project's database; the stale value was a leftover from an
   * earlier AI Studio scaffold. Firebase resolves `(default)` from the API key
   * in google-services.json, so the app always tracks the owning project.
   */
  private val firestore: FirebaseFirestore by lazy {
    FirebaseFirestore.getInstance()
  }

  private val appsCollection = firestore.collection("apps")
  private val categoriesCollection = firestore.collection("categories")

  /**
   * Single read of the `apps/` collection, unfiltered.
   *
   * The store and admin screens previously each attached their own snapshot
   * listener to this same collection and both were subscribed from launch, so
   * startup paid for two listener registrations (and two full deserialisation
   * passes) to show one list. One upstream flow now feeds both, and the
   * published-only filter lives in the ViewModel.
   */
  fun getAllItems(): Flow<List<StoreItem>> = callbackFlow {
    // Seed the UI immediately from the local catalog so the first frame is never
    // blank while Firestore connects.
    trySend(defaultItems)

    val listener = appsCollection
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          // Do NOT substitute the demo catalog on failure. Showing fabricated
          // listings (GTA V, WhatsApp, ...) to real users during an outage is
          // worse than showing nothing; the UI renders an empty/offline state
          // from an empty list instead.
          reportError("Listen failed for items: ${error.message}")
          trySend(emptyList())
          return@addSnapshotListener
        }
        val docs = snapshot?.documents
        if (docs.isNullOrEmpty()) {
          // Genuinely empty collection: real answer is "no apps yet", not the
          // demo catalog.
          trySend(emptyList())
          return@addSnapshotListener
        }
        // Deserialising on the main thread would be the single most expensive
        // thing this app does on a catalog update; the flow is collected with
        // flowOn(Dispatchers.IO) in the ViewModel.
        val items = docs.mapNotNull { doc -> runCatching { doc.toStoreItem() }.getOrNull() }
        trySend(items)
      }
    awaitClose { listener.remove() }
  }

  /**
   * Paged fetch for the "load more" path.
   *
   * The snapshot listener in [getAllItems] materialises the whole collection,
   * which does not scale: a large catalog is a large download on install and on
   * every reconnect. Firestore listeners have no native paging, so paginated
   * reads go through an explicit paged query.
   *
   * @param pageSize documents per page (max 500 is the server ceiling; keep it
   *   small enough that a phone fetch stays quick).
   * @param startAfter exclusive start cursor from the previous page, or null.
   */
  suspend fun getItemsPage(pageSize: Int = 50, startAfter: StoreItem? = null): List<StoreItem> {
    return try {
      // No server-side filter on the published flag: documents in this database
      // exist under two spellings (`published` from the console/web side and
      // `isPublished` from the old client-side seeder), and a whereEqualTo can
      // only match one of them — the other set would vanish. Ordering and
      // limiting are safe to push down; the flag filter stays client-side.
      var query = appsCollection
        .orderBy("downloadCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
        .limit(pageSize.toLong())

      if (startAfter != null) {
        query = query.startAfter(startAfter)
      }

      query.get().await().documents.mapNotNull { doc ->
        runCatching { doc.toStoreItem() }.getOrNull()
      }
    } catch (e: Exception) {
      Log.e(tag, "getItemsPage failed: ${e.message}")
      reportError("تعذّر تحميل المزيد من التطبيقات: ${e.message}")
      emptyList()
    }
  }

  /**
   * Bounded listener: only the catalog slice the UI shows on first paint.
   *
   * [getAllItems] stays unbounded for the admin dashboard (admins legitimately
   * need the whole list), but the customer-facing path uses this so a phone
   * never downloads the entire store.
   */
  fun getPublishedItems(limit: Int = 200): Flow<List<StoreItem>> = callbackFlow {
    trySend(emptyList())

    val listener = appsCollection
      .orderBy("downloadCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
      .limit(limit.toLong())
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          reportError("Listen failed for published items: ${error.message}")
          trySend(emptyList())
          return@addSnapshotListener
        }
        val items = snapshot?.documents?.mapNotNull { doc ->
          runCatching { doc.toStoreItem() }.getOrNull()
        }.orEmpty()
        trySend(items)
      }
    awaitClose { listener.remove() }
  }

  // Observe categories
  fun getCategories(): Flow<List<CategoryItem>> = callbackFlow {
    trySend(defaultCategories)

    val listener = categoriesCollection
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          reportError("Listen failed for categories: ${error.message}")
          trySend(emptyList())
          return@addSnapshotListener
        }
        val docs = snapshot?.documents
        if (docs.isNullOrEmpty()) {
          trySend(emptyList())
          return@addSnapshotListener
        }
        val items = docs.mapNotNull { doc ->
          runCatching {
            CategoryItem(
              id = doc.id,
              name = doc.getString("name").orEmpty(),
              iconName = doc.getString("iconName").orEmpty(),
              colorHex = doc.getString("colorHex") ?: "#3B82F6",
              itemCount = (doc.getLong("itemCount") ?: 0L).toInt(),
              type = doc.getString("type") ?: "all"
            )
          }.getOrNull()
        }
        trySend(items)
      }
    awaitClose { listener.remove() }
  }

  // Get item by ID
  suspend fun getItemById(id: String): StoreItem? {
    return try {
      val doc = appsCollection.document(id).get().await()
      doc.toObject(StoreItem::class.java)?.copy(id = doc.id)
        ?: defaultItems.find { it.id == id }
    } catch (e: Exception) {
      Log.e(tag, "Failed to get item $id from Firestore", e)
      defaultItems.find { it.id == id }
    }
  }

  // Admin: Add or Edit Store Item
  suspend fun saveStoreItem(item: StoreItem): Boolean {
    return try {
      val docRef = if (item.id.isNotBlank()) {
        appsCollection.document(item.id)
      } else {
        appsCollection.document()
      }
      val itemToSave = item.copy(id = docRef.id)
      docRef.set(itemToSave).await()
      Log.i(tag, "Saved store item ${itemToSave.name} to Firestore")
      true
    } catch (e: Exception) {
      Log.e(tag, "Error saving item to Firestore", e)
      false
    }
  }

  // Admin: Delete Store Item
  suspend fun deleteStoreItem(id: String): Boolean {
    return try {
      appsCollection.document(id).delete().await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error deleting item $id", e)
      false
    }
  }

  // Admin: Toggle Published
  suspend fun togglePublish(id: String, currentStatus: Boolean): Boolean {
    return try {
      appsCollection.document(id).update("published", !currentStatus).await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error updating publish status", e)
      false
    }
  }

  // Admin: Toggle Featured
  suspend fun toggleFeatured(id: String, currentStatus: Boolean): Boolean {
    return try {
      appsCollection.document(id).update("featured", !currentStatus).await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error updating featured status", e)
      false
    }
  }

  // Admin: Save Category
  suspend fun saveCategory(category: CategoryItem): Boolean {
    return try {
      val docRef = if (category.id.isNotBlank()) {
        categoriesCollection.document(category.id)
      } else {
        categoriesCollection.document()
      }
      docRef.set(category.copy(id = docRef.id)).await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error saving category", e)
      false
    }
  }

  // Admin: Delete Category
  suspend fun deleteCategory(id: String): Boolean {
    return try {
      categoriesCollection.document(id).delete().await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error deleting category $id", e)
      false
    }
  }

  /**
   * First-run seed — DISABLED by default.
   *
   * This used to write the 11 hardcoded demo apps (GTA V, WhatsApp, ...) into
   * the live `apps` collection on first launch. Once real apps exist that is
   * destructive: it injects fake listings into the catalog every installed
   * device reads, and `isPublished: true` puts them in front of real users.
   *
   * The demo catalog is now purely local — first-frame placeholder plus offline
   * fallback. Flip the flag only against the Firestore *emulator*, never for a
   * live project.
   */
  suspend fun seedInitialDataIfEmpty() {
    if (!seedDemoDataOnEmpty) {
      Log.i(tag, "Demo seeding disabled; serving live catalog only")
      return
    }
    try {
      if (appsCollection.limit(1).get().await().isEmpty) {
        Log.w(tag, "apps collection empty - writing demo catalog")
        for (item in defaultItems) {
          appsCollection.document(item.id).set(item).await()
        }
      }
      if (categoriesCollection.limit(1).get().await().isEmpty) {
        for (cat in defaultCategories) {
          runCatching { categoriesCollection.document(cat.id).set(cat).await() }
            .onFailure { Log.w(tag, "Failed to seed category ${cat.id}: ${it.message}") }
        }
      }
    } catch (e: Exception) {
      Log.w(tag, "Initial data seeding check failed (using local defaults): ${e.message}")
    }
  }

  private fun buildDefaultItems(): List<StoreItem> {
    return listOf(
      StoreItem(
        id = "gta-v",
        name = "Grand Theft Auto V",
        packageName = "com.rockstargames.gtav",
        developer = "Rockstar Games",
        type = "game",
        category = "ألعاب",
        categoryTags = listOf("عالم مفتوح", "أكشن", "مغامرة"),
        iconUrl = "android.resource://${context.packageName}/drawable/hero_gta",
        bannerUrl = "android.resource://${context.packageName}/drawable/hero_gta",
        rating = 4.5,
        downloads = "2.1 GB",
        downloadCount = 50000000L,
        fileSize = "2.1 GB",
        version = "v1.0.8",
        versionCode = 108,
        description = "استمتع بتجربة لا مثيل لها في عالم مفتوح ضخم مليء بالإثارة والمغامرات. حيث يمكنك استكشاف المدينة والقيام بالمهام وعيش حياة الجريمة بكل تفاصيلها.",
        screenshots = listOf(
          "android.resource://${context.packageName}/drawable/screenshot_gta1",
          "android.resource://${context.packageName}/drawable/screenshot_gta2"
        ),
        downloadUrl = "https://example.com/games/gtav.apk",
        isPublished = true,
        isFeatured = true,
        isUpdateAvailable = false,
        updatedDate = "2026-09-15",
        compatibility = "Android 9.0+",
        minSdk = "Android API 28 (9.0 Pie)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "arm64-v8a",
        modInfo = "نسخة معدلة مع رسوميات فائقة الدقة مفتوحة لكافة الأجهزة"
      ),
      StoreItem(
        id = "whatsapp",
        name = "WhatsApp Messenger",
        packageName = "com.whatsapp",
        developer = "Meta Platforms",
        type = "app",
        category = "تواصل",
        categoryTags = listOf("مراسلة", "مكالمات", "أمان"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/WhatsApp.svg/512px-WhatsApp.svg.png",
        bannerUrl = "",
        rating = 4.5,
        downloads = "2.8B",
        downloadCount = 2800000000L,
        fileSize = "58 MB",
        version = "v2.25.27.78",
        versionCode = 22527,
        description = "تطبيق واتساب يتيح لك مراسلة أصدقائك وعائلتك بسهولة وأمان. مع إمكانية إرسال الصور والفيديوهات والملفات والمحادثات المشفرة تماماً.",
        screenshots = listOf(
          "android.resource://${context.packageName}/drawable/screenshot_wa1"
        ),
        downloadUrl = "https://example.com/apps/whatsapp.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = true,
        updatedDate = "2026-10-01",
        compatibility = "Android 5.0+",
        minSdk = "Android API 21 (5.0 Lollipop)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal (all ABIs)"
      ),
      StoreItem(
        id = "instagram",
        name = "Instagram",
        packageName = "com.instagram.android",
        developer = "Meta Platforms",
        type = "app",
        category = "اجتماعي",
        categoryTags = listOf("صور", "فيديو", "ريلز"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Instagram_icon.png/512px-Instagram_icon.png",
        bannerUrl = "",
        rating = 4.6,
        downloads = "1.5B",
        downloadCount = 1500000000L,
        fileSize = "64 MB",
        version = "v308.0.0",
        versionCode = 30800,
        description = "تواصل مع الأصدقاء وشارك الصور والفيديوهات والقصص اليومية مع ملايين المبدعين حول العالم.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/instagram.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = true,
        updatedDate = "2026-09-28",
        compatibility = "Android 7.0+",
        minSdk = "Android API 24 (7.0 Nougat)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal"
      ),
      StoreItem(
        id = "tiktok",
        name = "TikTok",
        packageName = "com.zhiliaoapp.musically",
        developer = "TikTok Pte. Ltd.",
        type = "app",
        category = "ترفيه",
        categoryTags = listOf("فيديوهات قصيرة", "موسيقى", "مؤثرات"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/a/a9/TikTok_logo.svg/512px-TikTok_logo.svg.png",
        bannerUrl = "",
        rating = 4.4,
        downloads = "1.2B",
        downloadCount = 1200000000L,
        fileSize = "85 MB",
        version = "v37.5.0",
        versionCode = 3750,
        description = "شاهد واستكشف مقاطع فيديو قصيرة ومسلية وصنع محتواك الخاص بمؤثرات صوتية وموسيقى رائعة.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/tiktok.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = true,
        updatedDate = "2026-09-25",
        compatibility = "Android 6.0+",
        minSdk = "Android API 23 (6.0 Marshmallow)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal"
      ),
      StoreItem(
        id = "capcut",
        name = "CapCut",
        packageName = "com.lemon.lvoverseas",
        developer = "Bytedance Pte. Ltd.",
        type = "app",
        category = "تصوير",
        categoryTags = listOf("مونتاج", "فيديو", "تصميم"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Capcut_logo.png/512px-Capcut_logo.png",
        bannerUrl = "",
        rating = 4.7,
        downloads = "892M",
        downloadCount = 892000000L,
        fileSize = "112 MB",
        version = "v12.4.0",
        versionCode = 1240,
        description = "محرر فيديو احترافي وسهل الاستخدام لصناعة مقاطع فيديو سينمائية بمؤثرات وانتقالات مذهلة.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/capcut.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = false,
        updatedDate = "2026-09-20",
        compatibility = "Android 7.0+",
        minSdk = "Android API 24 (7.0 Nougat)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "arm64-v8a, armeabi-v7a",
        modInfo = "نسخة Pro مفتوحة لجميع القوالب والمؤثرات بدون علامة مائية"
      ),
      StoreItem(
        id = "telegram",
        name = "Telegram",
        packageName = "org.telegram.messenger",
        developer = "Telegram FZ-LLC",
        type = "app",
        category = "تواصل",
        categoryTags = listOf("سريع", "آمن", "سحابي"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/Telegram_logo.svg/512px-Telegram_logo.svg.png",
        bannerUrl = "",
        rating = 4.8,
        downloads = "948M",
        downloadCount = 948000000L,
        fileSize = "48 MB",
        version = "v10.6.1",
        versionCode = 1061,
        description = "تراسل فوري عالي السرعة وبسيط وسهل الاستخدام، مع إمكانية إنشاء قنوات ومجموعات غير محدودة.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/telegram.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = false,
        updatedDate = "2026-09-18",
        compatibility = "Android 6.0+",
        minSdk = "Android API 23 (6.0 Marshmallow)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal"
      ),
      StoreItem(
        id = "spotify",
        name = "Spotify",
        packageName = "com.spotify.music",
        developer = "Spotify AB",
        type = "app",
        category = "ترفيه",
        categoryTags = listOf("موسيقى", "بودكاست", "صوتيات"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Spotify_logo_without_text.svg/512px-Spotify_logo_without_text.svg.png",
        bannerUrl = "",
        rating = 4.5,
        downloads = "692M",
        downloadCount = 692000000L,
        fileSize = "39 MB",
        version = "v8.9.18",
        versionCode = 8918,
        description = "استمع إلى أغانيك وقوائمك الموسيقية المفضلة وبودكاست بجودة صوت فائقة النقاء.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/spotify.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = false,
        updatedDate = "2026-09-10",
        compatibility = "Android 7.0+",
        minSdk = "Android API 24 (7.0 Nougat)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal",
        modInfo = "نسخة Premium بدون إعلانات مع تخطي غير محدود للأغاني"
      ),
      StoreItem(
        id = "youtube",
        name = "YouTube",
        packageName = "com.google.android.youtube",
        developer = "Google LLC",
        type = "app",
        category = "ترفيه",
        categoryTags = listOf("فيديوهات", "بث مباشر", "موسيقى"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/YouTube_full-color_icon_%282017%29.svg/512px-YouTube_full-color_icon_%282017%29.svg.png",
        bannerUrl = "",
        rating = 4.6,
        downloads = "5B+",
        downloadCount = 5000000000L,
        fileSize = "42 MB",
        version = "v19.16.36",
        versionCode = 191636,
        description = "شاهد أحدث مقاطع الفيديو والموسيقى وقنوات المحتوى المفضلة لديك.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/youtube.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = true,
        updatedDate = "2026-10-01",
        compatibility = "Android 8.0+",
        minSdk = "Android API 26 (8.0 Oreo)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal"
      ),
      StoreItem(
        id = "chrome",
        name = "Google Chrome",
        packageName = "com.android.chrome",
        developer = "Google LLC",
        type = "app",
        category = "أدوات",
        categoryTags = listOf("متصفح", "إنترنت", "بحث"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e1/Google_Chrome_icon_%28February_2022%29.svg/512px-Google_Chrome_icon_%28February_2022%29.svg.png",
        bannerUrl = "",
        rating = 4.5,
        downloads = "10B+",
        downloadCount = 10000000000L,
        fileSize = "75 MB",
        version = "v131.0.6778.200",
        versionCode = 131067,
        description = "متصفح إنترنت سريع وآمن وسهل الاستخدام من قوقل مع مزامنة علامات التبويب والحماية.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/chrome.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = true,
        updatedDate = "2026-10-01",
        compatibility = "Android 8.0+",
        minSdk = "Android API 26 (8.0 Oreo)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal"
      ),
      StoreItem(
        id = "cyberpunk",
        name = "Cyberpunk 2077 Mobile",
        packageName = "com.cdprojektred.cyberpunk",
        developer = "CD PROJEKT RED",
        type = "game",
        category = "ألعاب",
        categoryTags = listOf("أكشن", "RPG", "مستقبل"),
        iconUrl = "android.resource://${context.packageName}/drawable/app_logo",
        bannerUrl = "android.resource://${context.packageName}/drawable/screenshot_gta2",
        rating = 4.7,
        downloads = "15M",
        downloadCount = 15000000L,
        fileSize = "3.2 GB",
        version = "v2.1.0",
        versionCode = 210,
        description = "لعبة تقمص أدوار وأكشن ومغامرات تدور أحداثها في مدينة نايت سيتي المستقبلية المليئة بالتقنيات والمهام الحماسية.",
        screenshots = listOf("android.resource://${context.packageName}/drawable/screenshot_gta2"),
        downloadUrl = "https://example.com/games/cyberpunk.apk",
        isPublished = true,
        isFeatured = true,
        isUpdateAvailable = false,
        updatedDate = "2026-09-01",
        compatibility = "Android 10.0+",
        minSdk = "Android API 29 (Android 10)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "arm64-v8a",
        modInfo = "أموال ونقاط مهارة غير محدودة مع رسوميات فائقة"
      )
    )
  }

  private fun buildDefaultCategories(): List<CategoryItem> {
    return listOf(
      CategoryItem(id = "cat_apps", name = "تطبيقات", iconName = "grid", colorHex = "#3B82F6", itemCount = 480, type = "app"),
      CategoryItem(id = "cat_games", name = "ألعاب", iconName = "gamepad", colorHex = "#8B5CF6", itemCount = 320, type = "game"),
      CategoryItem(id = "cat_ent", name = "ترفيه", iconName = "play", colorHex = "#EC4899", itemCount = 190, type = "all"),
      CategoryItem(id = "cat_tools", name = "أدوات", iconName = "wrench", colorHex = "#0EA5E9", itemCount = 260, type = "app"),
      CategoryItem(id = "cat_comm", name = "تواصل", iconName = "chat", colorHex = "#10B981", itemCount = 145, type = "app"),
      CategoryItem(id = "cat_photo", name = "تصوير", iconName = "camera", colorHex = "#A855F7", itemCount = 110, type = "app"),
      CategoryItem(id = "cat_social", name = "اجتماعي", iconName = "people", colorHex = "#F43F5E", itemCount = 95, type = "app"),
      CategoryItem(id = "cat_edu", name = "تعليم", iconName = "school", colorHex = "#6366F1", itemCount = 130, type = "app"),
      CategoryItem(id = "cat_health", name = "صحي", iconName = "heart", colorHex = "#FB7185", itemCount = 75, type = "app"),
      CategoryItem(id = "cat_biz", name = "أعمال", iconName = "business", colorHex = "#F59E0B", itemCount = 88, type = "app")
    )
  }
}


/**
 * Explicit Firestore -> [StoreItem] mapping.
 *
 * `toObject(StoreItem::class.java)` relies on exact field-name matches, and the
 * live database uses `published` / `featured` / `updateAvailable` where the model
 * declares `isPublished` / `isFeatured` / `isUpdateAvailable`. Reflection-based
 * deserialisation silently dropped all three, so `isPublished` always defaulted to
 * false and the published-only filter emptied the catalog as soon as Firestore
 * responded. Logcat showed 135 such warnings per launch.
 *
 * Both spellings are accepted so existing documents and newer writes both work.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toStoreItem(): StoreItem {
  // getData() is nullable in the Kotlin SDK view even though it is non-null in
  // practice for a fetched snapshot; fall back to an empty map so flag lookup
  // degrades to the model defaults instead of throwing.
  val data = getData() ?: emptyMap<String, Any>()

  fun flag(vararg names: String): Boolean =
    names.firstOrNull { data.containsKey(it) }?.let { getBoolean(it) } ?: false

  return StoreItem(
    id = id,
    name = getString("name").orEmpty(),
    packageName = getString("packageName").orEmpty(),
    developer = getString("developer").orEmpty(),
    type = getString("type") ?: "app",
    category = getString("category") ?: "تطبيقات",
    categoryTags = getStringList("categoryTags"),
    iconUrl = getString("iconUrl").orEmpty(),
    bannerUrl = getString("bannerUrl").orEmpty(),
    rating = (getDouble("rating") ?: 4.5),
    downloads = getString("downloads") ?: "1M",
    downloadCount = (getLong("downloadCount") ?: 1_000_000L),
    fileSize = getString("fileSize") ?: "50 MB",
    version = getString("version") ?: "1.0.0",
    versionCode = (getLong("versionCode") ?: 1L).toInt(),
    description = getString("description").orEmpty(),
    screenshots = getStringList("screenshots"),
    downloadUrl = getString("downloadUrl").orEmpty(),
    isPublished = flag("isPublished", "published"),
    isFeatured = flag("isFeatured", "featured"),
    isUpdateAvailable = flag("isUpdateAvailable", "updateAvailable"),
    updatedDate = getString("updatedDate").orEmpty(),
    compatibility = getString("compatibility") ?: "Android 8.0+",
    minSdk = getString("minSdk").orEmpty(),
    targetSdk = getString("targetSdk").orEmpty(),
    cpuArchitecture = getString("cpuArchitecture") ?: "Universal",
    modInfo = getString("modInfo").orEmpty(),
    authorId = getString("authorId").orEmpty()
  )
}

private fun com.google.firebase.firestore.DocumentSnapshot.getStringList(field: String): List<String> =
  runCatching { get(field, List::class.java)?.mapNotNull { it as? String } ?: emptyList() }
    .getOrDefault(emptyList())

/** Demo seeding is off for live projects; see StoreRepository.seedInitialDataIfEmpty. */
private const val SEED_DEMO_DATA_ON_EMPTY = false
