package com.example

import android.app.Application
import com.example.data.DownloadManager

/**
 * Application entry point.
 *
 * [DownloadManager] holds a reference to the app's files directory so it can
 * write APKs somewhere private and hand them to the installer via a
 * FileProvider URI. Initialising it from `Application.onCreate` guarantees that
 * before any composable can call `startDownload`.
 */
class AppStoreApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    DownloadManager.init(this)
  }
}