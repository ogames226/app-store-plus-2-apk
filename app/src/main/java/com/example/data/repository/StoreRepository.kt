package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDao
import com.example.data.local.AppDatabase
import com.example.data.local.AppEntity
import com.example.data.local.CategoryDao
import com.example.data.local.CategoryEntity
import com.example.data.remote.FirebaseManager
import com.example.data.remote.SampleDataProvider
import com.example.model.AppItem
import com.example.model.CategoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoreRepository(
    private val context: Context,
    private val appDao: AppDao,
    private val categoryDao: CategoryDao,
    val firebaseManager: FirebaseManager
) {
    private val TAG = "StoreRepository"
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            seedCategoriesIfNeeded()
            syncWithFirebase()
        }
    }

    private suspend fun seedCategoriesIfNeeded() {
        val existingCategories = categoryDao.getAllCategories().first()
        if (existingCategories.isEmpty()) {
            val defaultCats = SampleDataProvider.getDefaultCategories().map {
                CategoryEntity.fromCategoryItem(it)
            }
            categoryDao.insertCategories(defaultCats)
        }
    }

    suspend fun clearAllApps() {
        withContext(Dispatchers.IO) {
            appDao.clearAllApps()
        }
    }

    private suspend fun syncWithFirebase() {
        if (!firebaseManager.isFirebaseInitialized) return
        try {
            val remoteApps = firebaseManager.fetchFirestoreApps()
            if (remoteApps != null) {
                // If remote is empty, we don't insert demo apps
                if (remoteApps.isNotEmpty()) {
                    appDao.insertApps(remoteApps.map { AppEntity.fromAppItem(it) })
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "syncWithFirebase warning: ${e.message}")
        }
    }

    val allPublishedApps: Flow<List<AppItem>> = appDao.getAllPublishedApps().map { list ->
        list.map { it.toAppItem() }
    }

    val allAppsForAdmin: Flow<List<AppItem>> = appDao.getAllApps().map { list ->
        list.map { it.toAppItem() }
    }

    val appsOnly: Flow<List<AppItem>> = appDao.getAppsOnly().map { list ->
        list.map { it.toAppItem() }
    }

    val gamesOnly: Flow<List<AppItem>> = appDao.getGamesOnly().map { list ->
        list.map { it.toAppItem() }
    }

    val featuredGames: Flow<List<AppItem>> = appDao.getAllPublishedApps().map { list ->
        list.map { it.toAppItem() }.filter { it.isGame && it.isFeatured }
    }

    val mostDownloadedApps: Flow<List<AppItem>> = appDao.getAllPublishedApps().map { list ->
        list.map { it.toAppItem() }.filter { it.isMostDownloaded }
    }

    val updatesList: Flow<List<AppItem>> = appDao.getAppsWithUpdates().map { list ->
        list.map { it.toAppItem() }
    }

    val allCategories: Flow<List<CategoryItem>> = categoryDao.getAllCategories().map { list ->
        list.map { it.toCategoryItem() }
    }

    suspend fun getAppById(appId: String): AppItem? {
        return appDao.getAppById(appId)?.toAppItem()
    }

    suspend fun searchApps(query: String): List<AppItem> {
        val list = appDao.getAllApps().first()
        val q = query.trim().lowercase()
        return list.map { it.toAppItem() }.filter {
            it.name.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.developer.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }
    }

    suspend fun saveApp(app: AppItem): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Always persist in local Room database first
                appDao.insertApp(AppEntity.fromAppItem(app))

                // Also sync with Firebase Firestore
                val firebaseResult = firebaseManager.saveApp(app)
                if (firebaseResult.isFailure) {
                    Log.w(TAG, "Firestore sync warning: ${firebaseResult.exceptionOrNull()?.message}")
                }
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "saveApp error: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteApp(appId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                appDao.deleteAppById(appId)
                firebaseManager.deleteApp(appId)
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "deleteApp error: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun togglePublish(appId: String, isPublished: Boolean): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            val app = appDao.getAppById(appId)?.toAppItem() ?: return@withContext Result.failure(Exception("App not found"))
            val updated = app.copy(isPublished = isPublished)
            saveApp(updated)
        }
    }

    suspend fun saveCategory(category: CategoryItem): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                categoryDao.insertCategory(CategoryEntity.fromCategoryItem(category))
                firebaseManager.saveCategory(category)
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteCategory(category: CategoryItem): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                categoryDao.deleteCategory(CategoryEntity.fromCategoryItem(category))
                firebaseManager.deleteCategory(category.id)
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StoreRepository? = null

        fun getInstance(context: Context): StoreRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val fb = FirebaseManager(context)
                val instance = StoreRepository(context, db.appDao(), db.categoryDao(), fb)
                INSTANCE = instance
                instance
            }
        }
    }
}
