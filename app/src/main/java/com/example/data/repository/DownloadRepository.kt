package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.model.AppItem
import com.example.model.DownloadItem
import com.example.model.DownloadStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class DownloadRepository(private val context: Context) {

    private val TAG = "DownloadRepository"
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _downloads = MutableStateFlow<Map<String, DownloadItem>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadItem>> = _downloads.asStateFlow()

    private val downloadJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startDownload(app: AppItem) {
        val appId = app.id
        downloadJobs[appId]?.cancel()

        val parsedTotalBytes = parseBytes(app.fileSize)
        val item = DownloadItem(
            appId = appId,
            appName = app.name,
            appIconRes = app.localIconRes,
            appIconUrl = app.iconUrl,
            status = DownloadStatus.DOWNLOADING,
            progress = 0f,
            downloadedBytes = 0L,
            totalBytes = parsedTotalBytes
        )
        _downloads.update { it + (appId to item) }

        val job = scope.launch {
            val downloadDir = File(context.cacheDir, "downloads").apply { mkdirs() }
            val cleanPkg = if (app.packageName.isNotBlank()) app.packageName else app.id
            val targetFile = File(downloadDir, "${cleanPkg}_${app.version}.apk")

            try {
                // Determine source: local uploaded file or remote URL
                val rawUrl = app.apkDownloadUrl.ifBlank { app.uploadedApkLocalPath }

                if (rawUrl.isBlank()) {
                    _downloads.update { map ->
                        val current = map[appId] ?: item
                        map + (appId to current.copy(
                            status = DownloadStatus.FAILED,
                            errorMessage = "لم يتم رفع ملف APK لهذا التطبيق بعد"
                        ))
                    }
                    return@launch
                }

                if (rawUrl.startsWith("file://") || File(rawUrl).exists()) {
                    // Local file: copy with real byte progress
                    val sourcePath = rawUrl.removePrefix("file://")
                    val sourceFile = File(sourcePath)

                    if (!sourceFile.exists()) {
                        _downloads.update { map ->
                            val current = map[appId] ?: item
                            map + (appId to current.copy(
                                status = DownloadStatus.FAILED,
                                errorMessage = "ملف APK المحلي غير موجود"
                            ))
                        }
                        return@launch
                    }

                    val totalLength = sourceFile.length()
                    FileInputStream(sourceFile).use { input ->
                        FileOutputStream(targetFile).use { output ->
                            val buffer = ByteArray(64 * 1024)
                            var read: Int
                            var totalRead = 0L
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                totalRead += read
                                delay(20) // Provide smooth progress feedback
                                val progress = (totalRead.toFloat() / totalLength.toFloat()).coerceIn(0f, 1f)
                                _downloads.update { map ->
                                    val current = map[appId] ?: item
                                    map + (appId to current.copy(
                                        status = DownloadStatus.DOWNLOADING,
                                        progress = progress,
                                        downloadedBytes = totalRead,
                                        totalBytes = totalLength
                                    ))
                                }
                            }
                        }
                    }
                } else {
                    // Remote HTTP/HTTPS download
                    val request = Request.Builder().url(rawUrl).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful || response.body == null) {
                            throw Exception("فشل الاتصال بخادم التحميل: رمز الخطأ ${response.code}")
                        }
                        val body = response.body!!
                        val length = if (body.contentLength() > 0) body.contentLength() else parsedTotalBytes
                        val inputStream = body.byteStream()
                        val outputStream = FileOutputStream(targetFile)

                        val buffer = ByteArray(32 * 1024)
                        var read: Int
                        var totalRead = 0L

                        while (inputStream.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                            totalRead += read
                            val progress = (totalRead.toFloat() / length.toFloat()).coerceIn(0f, 1f)

                            _downloads.update { map ->
                                val current = map[appId] ?: item
                                map + (appId to current.copy(
                                    status = DownloadStatus.DOWNLOADING,
                                    progress = progress,
                                    downloadedBytes = totalRead,
                                    totalBytes = length
                                ))
                            }
                        }
                        outputStream.flush()
                        outputStream.close()
                    }
                }

                _downloads.update { map ->
                    val current = map[appId] ?: item
                    map + (appId to current.copy(
                        status = DownloadStatus.COMPLETED,
                        progress = 1f,
                        downloadedBytes = targetFile.length(),
                        localFilePath = targetFile.absolutePath
                    ))
                }

                // Launch package installer
                installApk(targetFile)

            } catch (e: CancellationException) {
                _downloads.update { map ->
                    val current = map[appId] ?: item
                    map + (appId to current.copy(status = DownloadStatus.IDLE, progress = 0f))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download error: ${e.message}")
                _downloads.update { map ->
                    val current = map[appId] ?: item
                    map + (appId to current.copy(
                        status = DownloadStatus.FAILED,
                        errorMessage = e.localizedMessage ?: "حدث خطأ أثناء تحميل الملف"
                    ))
                }
            }
        }
        downloadJobs[appId] = job
    }

    fun cancelDownload(appId: String) {
        downloadJobs[appId]?.cancel()
        downloadJobs.remove(appId)
        _downloads.update { map ->
            val cur = map[appId] ?: return@update map
            map + (appId to cur.copy(status = DownloadStatus.IDLE, progress = 0f))
        }
    }

    fun installApk(file: File) {
        try {
            if (!file.exists()) return
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "installApk intent failed: ${e.message}")
        }
    }

    private fun parseBytes(sizeStr: String): Long {
        return try {
            val clean = sizeStr.uppercase().trim()
            when {
                clean.endsWith("GB") -> {
                    val num = clean.replace("GB", "").trim().toDouble()
                    (num * 1024 * 1024 * 1024).toLong()
                }
                clean.endsWith("MB") -> {
                    val num = clean.replace("MB", "").trim().toDouble()
                    (num * 1024 * 1024).toLong()
                }
                clean.endsWith("KB") -> {
                    val num = clean.replace("KB", "").trim().toDouble()
                    (num * 1024).toLong()
                }
                else -> 45L * 1024 * 1024
            }
        } catch (e: Exception) {
            45L * 1024 * 1024
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DownloadRepository? = null

        fun getInstance(context: Context): DownloadRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DownloadRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
