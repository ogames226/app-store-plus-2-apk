package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps WHERE isPublished = 1")
    fun getAllPublishedApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE id = :appId LIMIT 1")
    suspend fun getAppById(appId: String): AppEntity?

    @Query("SELECT * FROM apps WHERE isGame = 0 AND isPublished = 1")
    fun getAppsOnly(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isGame = 1 AND isPublished = 1")
    fun getGamesOnly(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isUpdateAvailable = 1")
    fun getAppsWithUpdates(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchApps(query: String): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity)

    @Update
    suspend fun updateApp(app: AppEntity)

    @Delete
    suspend fun deleteApp(app: AppEntity)

    @Query("DELETE FROM apps WHERE id = :appId")
    suspend fun deleteAppById(appId: String)

    @Query("DELETE FROM apps")
    suspend fun clearAllApps()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}
