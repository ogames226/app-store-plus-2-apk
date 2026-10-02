package com.example.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.zip.ZipFile

data class ExtractedApkData(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val fileSizeFormatted: String,
    val fileSizeBytes: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val abi: String,
    val iconLocalPath: String?,
    val savedApkFile: File
)

object ApkParser {

    private const val TAG = "ApkParser"

    fun copyAndExtractApk(context: Context, uri: Uri): Result<ExtractedApkData> {
        return try {
            val uploadDir = File(context.filesDir, "uploaded_apks").apply { mkdirs() }
            val tempFile = File(uploadDir, "apk_${System.currentTimeMillis()}.apk")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return Result.failure(Exception("تعذر قراءة ملف APK"))

            val pm = context.packageManager
            val packageInfo: PackageInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageArchiveInfo(tempFile.absolutePath, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageArchiveInfo(tempFile.absolutePath, 0)
            }

            if (packageInfo == null) {
                return Result.failure(Exception("ملف APK غير صالح أو تالف"))
            }

            val appInfo: ApplicationInfo = packageInfo.applicationInfo ?: ApplicationInfo()
            appInfo.sourceDir = tempFile.absolutePath
            appInfo.publicSourceDir = tempFile.absolutePath

            val appName = try {
                appInfo.loadLabel(pm).toString().ifBlank { tempFile.nameWithoutExtension }
            } catch (e: Exception) {
                tempFile.nameWithoutExtension
            }

            val packageName = packageInfo.packageName ?: "com.app.${System.currentTimeMillis()}"
            val versionName = packageInfo.versionName ?: "v1.0.0"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }

            val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                appInfo.minSdkVersion
            } else 21

            val targetSdk = appInfo.targetSdkVersion

            val abi = detectAbiFromApk(tempFile)

            // Extract and save icon as PNG
            val iconPath = try {
                val iconDrawable = appInfo.loadIcon(pm)
                val iconsDir = File(context.filesDir, "app_icons").apply { mkdirs() }
                val iconFile = File(iconsDir, "${packageName}.png")
                saveDrawableAsPng(iconDrawable, iconFile)
                iconFile.absolutePath
            } catch (e: Exception) {
                Log.w(TAG, "Failed to extract icon: ${e.message}")
                null
            }

            val fileSize = tempFile.length()
            val formattedSize = formatFileSize(fileSize)

            Result.success(
                ExtractedApkData(
                    appName = appName,
                    packageName = packageName,
                    versionName = if (versionName.startsWith("v", ignoreCase = true)) versionName else "v$versionName",
                    versionCode = versionCode,
                    fileSizeFormatted = formattedSize,
                    fileSizeBytes = fileSize,
                    minSdk = minSdk,
                    targetSdk = targetSdk,
                    abi = abi,
                    iconLocalPath = iconPath,
                    savedApkFile = tempFile
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in copyAndExtractApk: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun detectAbiFromApk(file: File): String {
        return try {
            ZipFile(file).use { zip ->
                val entries = zip.entries()
                val abis = mutableSetOf<String>()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name
                    if (name.startsWith("lib/")) {
                        val parts = name.split("/")
                        if (parts.size >= 2) {
                            abis.add(parts[1])
                        }
                    }
                }
                when {
                    abis.isEmpty() -> "Universal (No native libs)"
                    abis.size > 2 -> "Multi-Arch (${abis.joinToString(", ")})"
                    else -> abis.joinToString(", ")
                }
            }
        } catch (e: Exception) {
            "Universal"
        }
    }

    private fun saveDrawableAsPng(drawable: Drawable, outputFile: File) {
        val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
            drawable.bitmap
        } else {
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128
            val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bm
        }

        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun formatFileSize(size: Long): String {
        val df = DecimalFormat("#.##")
        return when {
            size >= 1024L * 1024L * 1024L -> "${df.format(size.toDouble() / (1024L * 1024L * 1024L))} GB"
            size >= 1024L * 1024L -> "${df.format(size.toDouble() / (1024L * 1024L))} MB"
            size >= 1024L -> "${df.format(size.toDouble() / 1024L)} KB"
            else -> "$size B"
        }
    }
}
