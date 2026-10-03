package com.example.data

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.model.DownloadProgress
import com.example.model.DownloadStatus
import com.example.model.StoreItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.content.FileProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Real APK downloader.
 *
 * The previous implementation was a timer that incremented a float to 1.0 — it
 * never touched the network, so every progress bar in the app was displaying
 * fiction. This does a real streaming download to app-private storage and
 * reports genuine byte-level progress.
 *
 * Downloads run on [Dispatchers.IO]; progress is published as a single map
 * update per chunk so only the affected rows recompose.
 */
object DownloadManager {

  private const val TAG = "DownloadManager"

  private val scope = CoroutineScope(Dispatchers.IO)
  private val activeJobs = mutableMapOf<String, Job>()

  /**
   * Emitted at most this often regardless of how many chunks arrive, so a fast
   * connection can't flood the UI with recompositions.
   */
  private const val MIN_PROGRESS_INTERVAL_MS = 100L

  private val _downloads = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
  val downloads: StateFlow<Map<String, DownloadProgress>> = _downloads.asStateFlow()

  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

  private lateinit var appContext: Context

  /** Call once from Application.onCreate. */
  fun init(context: Context) {
    appContext = context.applicationContext
  }

  fun isDownloading(appId: String): Boolean = activeJobs[appId]?.isActive == true

  fun completedFile(appId: String): File? =
    _downloads.value[appId]?.takeIf { it.status == DownloadStatus.COMPLETED }?.let { progress ->
      File(appContext.filesDir, progress.localPath.orEmpty())
    }

  fun startDownload(item: StoreItem) {
    if (!::appContext.isInitialized) {
      Log.e(TAG, "DownloadManager.init() was never called")
      return
    }
    val appId = item.id
    if (activeJobs[appId]?.isActive == true) return

    val url = item.downloadUrl
    if (url.isBlank() || !url.startsWith("http")) {
      Log.w(TAG, "Item ${item.id} has no usable downloadUrl ('$url')")
      _downloads.update { it + (appId to DownloadProgress(
        appId = appId,
        progress = 0f,
        status = DownloadStatus.FAILED,
        totalSize = item.fileSize,
        errorMessage = "رابط التنزيل غير متوفر"
      )) }
      return
    }

    activeJobs[appId] = scope.launch {
      val target = File(appContext.filesDir, "apk/${item.id}.apk")
      target.parentFile?.mkdirs()

      publish(
        appId, item,
        DownloadProgress(appId = appId, progress = 0f,
          status = DownloadStatus.DOWNLOADING, totalSize = item.fileSize,
          localPath = target.name)
      )

      var lastEmit = 0L
      try {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
          if (!response.isSuccessful) {
            Log.w(TAG, "HTTP ${response.code} for ${item.id}")
            publish(appId, item, DownloadProgress(
              appId = appId, progress = 0f, status = DownloadStatus.FAILED,
              totalSize = item.fileSize,
              errorMessage = "فشل التنزيل (${response.code})"
            ))
            return@launch
          }

          val body = response.body
          if (body == null) {
            publish(appId, item, DownloadProgress(
              appId = appId, status = DownloadStatus.FAILED,
              totalSize = item.fileSize, errorMessage = "استجابة فارغة"
            ))
            return@launch
          }

          val total = body.contentLength().takeIf { it > 0 } ?: -1L
          var downloaded = 0L

          body.byteStream().use { input ->
            target.outputStream().use { output ->
              val buffer = ByteArray(64 * 1024)
              while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                output.write(buffer, 0, read)
                downloaded += read

                val now = System.currentTimeMillis()
                if (now - lastEmit >= MIN_PROGRESS_INTERVAL_MS) {
                  lastEmit = now
                  val frac = if (total > 0) downloaded.toFloat() / total else 0f
                  publish(appId, item, DownloadProgress(
                    appId = appId,
                    progress = frac,
                    status = DownloadStatus.DOWNLOADING,
                    downloadedSize = formatBytes(downloaded),
                    totalSize = if (total > 0) formatBytes(total) else item.fileSize,
                    localPath = target.name
                  ))
                }
              }
            }
          }

          publish(appId, item, DownloadProgress(
            appId = appId,
            progress = 1f,
            status = DownloadStatus.COMPLETED,
            downloadedSize = formatBytes(downloaded),
            totalSize = formatBytes(downloaded),
            localPath = target.name
          ))
          Log.i(TAG, "Downloaded ${item.id} -> ${downloaded} bytes")
        }
      } catch (e: Exception) {
        Log.e(TAG, "Download failed for ${item.id}", e)
        target.delete()
        publish(appId, item, DownloadProgress(
          appId = appId, status = DownloadStatus.FAILED,
          totalSize = item.fileSize,
          errorMessage = e.localizedMessage ?: "خطأ في الشبكة"
        ))
      }
    }
  }

  fun cancelDownload(appId: String) {
    activeJobs.remove(appId)?.cancel()
    _downloads.update { it - appId }
  }

  fun getProgressForApp(appId: String): DownloadProgress? = _downloads.value[appId]

  /** Clears a finished/failed entry so the button returns to "install". */
  fun reset(appId: String) {
    _downloads.update { it - appId }
  }

  private fun publish(appId: String, item: StoreItem, progress: DownloadProgress) {
    _downloads.update { it + (appId to progress) }
  }

  private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
    bytes >= 1_048_576 -> String.format("%.0f MB", bytes / 1_048_576.0)
    bytes >= 1_024 -> String.format("%.0f KB", bytes / 1_024.0)
    else -> "$bytes B"
  }
}

/**
 * Launches the system package installer for a downloaded APK.
 *
 * The file lives in app-private storage, so it must be exposed through a
 * FileProvider — passing a `file://` URI triggers FileUriExposedException on
 * API 24+, which is this app's minimum.
 */
object ApkInstaller {
  fun install(context: Context, appId: String): Boolean {
    val file = DownloadManager.completedFile(appId) ?: return false
    if (!file.exists()) return false

    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
      setDataAndType(uri, "application/vnd.android.package-archive")
      addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
      addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      Log.e("ApkInstaller", "No installer available", e)
      false
    }
  }
}