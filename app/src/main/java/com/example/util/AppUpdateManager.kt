package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppReleaseInfo(
    val tagName: String,
    val releaseName: String,
    val changelog: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val remoteVersionCode: Int,
    val remoteVersionName: String,
    val isNewer: Boolean
)

sealed class UpdateCheckResult {
    data class UpdateAvailable(val release: AppReleaseInfo) : UpdateCheckResult()
    data class UpToDate(val currentVersionCode: Int, val currentVersionName: String) : UpdateCheckResult()
    data class NoReleasesFound(val message: String) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

object AppUpdateManager {

    private const val GITHUB_OWNER = "narayan43"
    private const val GITHUB_REPO = "study-app-"
    private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    /**
     * Checks the GitHub Releases API for a newer version.
     */
    suspend fun checkForUpdate(
        customApiUrl: String = GITHUB_API_URL
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(customApiUrl)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            conn.setRequestProperty("User-Agent", "ExamPrepCSV-Android")

            val responseCode = conn.responseCode
            if (responseCode == 404) {
                return@withContext UpdateCheckResult.NoReleasesFound("No published releases found on GitHub repository $GITHUB_OWNER/$GITHUB_REPO yet.")
            }
            if (responseCode !in 200..299) {
                return@withContext UpdateCheckResult.Error("GitHub API returned HTTP $responseCode")
            }

            val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseBody)

            val tagName = json.optString("tag_name", "").trim()
            val releaseName = json.optString("name", tagName).trim()
            val body = json.optString("body", "Bug fixes and improvements.").trim()

            // Look for ExamPrep-*.apk file in assets (avoiding any unsigned assets)
            val assetsArray = json.optJSONArray("assets")
            var apkUrl: String? = null
            var apkName: String? = null
            var apkSize: Long = 0L

            if (assetsArray != null) {
                // First pass: look specifically for ExamPrep-*.apk
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.startsWith("ExamPrep-", ignoreCase = true) &&
                        name.endsWith(".apk", ignoreCase = true) &&
                        !name.contains("unsigned", ignoreCase = true)
                    ) {
                        apkUrl = asset.optString("browser_download_url", "")
                        apkName = name
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
                // Fallback pass: any non-unsigned .apk
                if (apkUrl == null) {
                    for (i in 0 until assetsArray.length()) {
                        val asset = assetsArray.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true) &&
                            !name.contains("unsigned", ignoreCase = true)
                        ) {
                            apkUrl = asset.optString("browser_download_url", "")
                            apkName = name
                            apkSize = asset.optLong("size", 0L)
                            break
                        }
                    }
                }
            }

            if (apkUrl.isNullOrBlank()) {
                return@withContext UpdateCheckResult.NoReleasesFound("Release $tagName exists, but no APK asset (.apk) is attached.")
            }

            val remoteVersionName = tagName.removePrefix("v").trim()
            val remoteVersionCode = extractVersionCode(releaseName, body, apkName ?: "", remoteVersionName)

            val currentCode = BuildConfig.VERSION_CODE
            val currentName = BuildConfig.VERSION_NAME

            val isNewer = isRemoteVersionNewer(
                remoteCode = remoteVersionCode,
                remoteName = remoteVersionName,
                localCode = currentCode,
                localName = currentName
            )

            val releaseInfo = AppReleaseInfo(
                tagName = tagName,
                releaseName = releaseName,
                changelog = body,
                apkDownloadUrl = apkUrl,
                apkFileName = apkName ?: "ExamPrep-update.apk",
                apkSizeBytes = apkSize,
                remoteVersionCode = remoteVersionCode,
                remoteVersionName = remoteVersionName,
                isNewer = isNewer
            )

            if (isNewer) {
                UpdateCheckResult.UpdateAvailable(releaseInfo)
            } else {
                UpdateCheckResult.UpToDate(currentCode, currentName)
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.message ?: "Failed to connect to GitHub")
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Downloads the APK file to the app's cache directory with progress reporting.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        targetFileName: String,
        onProgress: (bytesDownloaded: Long, totalBytes: Long, percent: Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val outputFile = File(updatesDir, targetFileName)
            if (outputFile.exists()) {
                outputFile.delete()
            }

            var currentUrl = downloadUrl
            var redirectCount = 0
            while (redirectCount < 5) {
                val url = URL(currentUrl)
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.setRequestProperty("User-Agent", "ExamPrepCSV-Android")

                val status = conn.responseCode
                if (status in 300..399) {
                    val location = conn.getHeaderField("Location") ?: break
                    conn.disconnect()
                    currentUrl = location
                    redirectCount++
                } else if (status in 200..299) {
                    break
                } else {
                    return@withContext Result.failure(Exception("HTTP download error: $status"))
                }
            }

            val totalBytes = conn?.contentLengthLong ?: -1L
            var downloadedBytes = 0L

            conn?.inputStream?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        val percent = if (totalBytes > 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0
                        onProgress(downloadedBytes, totalBytes, percent)
                    }
                    output.flush()
                }
            }

            if (!outputFile.exists() || outputFile.length() == 0L) {
                return@withContext Result.failure(Exception("Downloaded file is empty"))
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Returns true if unknown app installation permission is granted.
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Intent to open the System Settings screen to allow installing unknown apps.
     */
    fun createInstallPermissionIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    /**
     * Starts the system package installer session using FileProvider.
     */
    fun startInstall(context: Context, apkFile: File): Result<Unit> {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractVersionCode(
        releaseName: String,
        body: String,
        assetName: String,
        versionName: String
    ): Int {
        // Try regex on release body: "versionCode: 105" or "versionCode = 105" or "code: 105"
        val codeRegex = Regex("""(?:versionCode|build|code)[\s:=]+(\d+)""", RegexOption.IGNORE_CASE)
        val matchBody = codeRegex.find(body)
        if (matchBody != null) {
            val code = matchBody.groupValues[1].toIntOrNull()
            if (code != null) return code
        }

        // Try regex on release name
        val matchName = codeRegex.find(releaseName)
        if (matchName != null) {
            val code = matchName.groupValues[1].toIntOrNull()
            if (code != null) return code
        }

        // Try regex on asset name (e.g. ExamPrep-v1.0.2-b102.apk or similar)
        val matchAsset = Regex("""-b(\d+)""").find(assetName)
        if (matchAsset != null) {
            val code = matchAsset.groupValues[1].toIntOrNull()
            if (code != null) return code
        }

        // Synthesize an integer from semantic version e.g. 1.0.2 -> 10002
        return parseSemverToCode(versionName)
    }

    private fun isRemoteVersionNewer(
        remoteCode: Int,
        remoteName: String,
        localCode: Int,
        localName: String
    ): Boolean {
        if (remoteCode > localCode) return true
        if (remoteCode < localCode) return false
        // If version codes match, fallback to semantic version comparison
        return compareSemver(remoteName, localName) > 0
    }

    private fun parseSemverToCode(version: String): Int {
        val parts = version.split(".").mapNotNull { it.takeWhile { c -> c.isDigit() }.toIntOrNull() }
        val major = parts.getOrElse(0) { 0 }
        val minor = parts.getOrElse(1) { 0 }
        val patch = parts.getOrElse(2) { 0 }
        return major * 10000 + minor * 100 + patch
    }

    private fun compareSemver(v1: String, v2: String): Int {
        val p1 = v1.split(".").mapNotNull { it.takeWhile { c -> c.isDigit() }.toIntOrNull() }
        val p2 = v2.split(".").mapNotNull { it.takeWhile { c -> c.isDigit() }.toIntOrNull() }
        val maxLen = maxOf(p1.size, p2.size)
        for (i in 0 until maxLen) {
            val n1 = p1.getOrElse(i) { 0 }
            val n2 = p2.getOrElse(i) { 0 }
            if (n1 != n2) return n1.compareTo(n2)
        }
        return 0
    }
}
