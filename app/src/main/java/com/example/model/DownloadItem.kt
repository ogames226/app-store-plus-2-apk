package com.example.model

enum class DownloadStatus {
    IDLE,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

data class DownloadItem(
    val appId: String = "",
    val appName: String = "",
    val appIconRes: Int? = null,
    val appIconUrl: String = "",
    val status: DownloadStatus = DownloadStatus.IDLE,
    val progress: Float = 0f, // 0.0 to 1.0
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 1024L * 1024L * 50L,
    val localFilePath: String? = null,
    val errorMessage: String? = null
)
