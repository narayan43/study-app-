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
import java.security.MessageDigest
import java.util.Locale

data class AppReleaseInfo(
    val tagName: String,
    val releaseName: String,
    val changelog: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val remoteVersionCode: Int,
    val remoteVersionName: String,
    val isNewer: Boolean,
    val expectedSha256: String = ""
)

sealed class UpdateCheckResult {
    data class UpdateAvailable(val release: AppReleaseInfo) : UpdateCheckResult()
    data class UpToDate(val currentVersionCode: Int, val currentVersionName: String) : UpdateCheckResult()
    data class NoReleasesFound(val message: String) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

data class VersionMetadata(
    val versionCode: Int,
    val versionName: String,
    val apkFileName: String,
    val sha256: String
)

object AppUpdateManager {

    private const val GITHUB_OWNER = "narayan43"
    private const val GITHUB_REPO = "study-app-"
    private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    /**
     * Checks the GitHub Releases API for a newer version.
     * Prefers reading version.json release asset if published.
     */
    suspend fun checkForUpdate(
        customApiUrl: String = GITHUB_API_URL
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(customApiUrl)
            if (!isValidUrl(customApiUrl)) {
                return@withContext UpdateCheckResult.Error("Insecure or untrusted update URL: $customApiUrl")
            }

            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            conn.setRequestProperty("User-Agent", "ExamPrepCSV-Android")

            val responseCode = conn.responseCode
            if (responseCode == 404) {
                return@withContext UpdateCheckResult.NoReleasesFound(
                    "No published releases found on GitHub repository $GITHUB_OWNER/$GITHUB_REPO yet."
                )
            }
            if (responseCode !in 200..299) {
                return@withContext UpdateCheckResult.Error("GitHub API returned HTTP $responseCode")
            }

            val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseBody)

            val tagName = json.optString("tag_name", "").trim()
            val releaseName = json.optString("name", tagName).trim()
            val body = json.optString("body", "Bug fixes and improvements.").trim()
            val assetsArray = json.optJSONArray("assets")

            var versionJsonUrl: String? = null
            var apkUrl: String? = null
            var apkName: String? = null
            var apkSize: Long = 0L
            var checksumFileUrl: String? = null

            if (assetsArray != null) {
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val name = asset.optString("name", "")
                    val downloadUrl = asset.optString("browser_download_url", "")

                    if (name.equals("version.json", ignoreCase = true)) {
                        versionJsonUrl = downloadUrl
                    } else if (name.endsWith(".sha256", ignoreCase = true)) {
                        checksumFileUrl = downloadUrl
                    } else if (name.endsWith(".apk", ignoreCase = true) && !name.contains("unsigned", ignoreCase = true)) {
                        // Prefer ExamPrep-*.apk
                        if (apkUrl == null || name.startsWith("ExamPrep-", ignoreCase = true)) {
                            apkUrl = downloadUrl
                            apkName = name
                            apkSize = asset.optLong("size", 0L)
                        }
                    }
                }
            }

            // 1. Check version.json asset first
            var metadata: VersionMetadata? = null
            if (!versionJsonUrl.isNullOrBlank()) {
                val downloadedJson = fetchTextFile(versionJsonUrl)
                if (downloadedJson != null) {
                    metadata = parseVersionJson(downloadedJson)
                }
            }

            // 2. Determine remote version code & name
            val remoteVersionCode: Int
            val remoteVersionName: String
            var expectedSha256 = ""

            if (metadata != null) {
                remoteVersionCode = metadata.versionCode
                remoteVersionName = metadata.versionName
                expectedSha256 = metadata.sha256
                if (metadata.apkFileName.isNotBlank()) {
                    apkName = metadata.apkFileName
                }
            } else {
                // Backward-compatible fallback: parse from tag and release body
                remoteVersionName = tagName.removePrefix("v").trim()
                remoteVersionCode = extractVersionCode(releaseName, body, apkName ?: "", remoteVersionName)
            }

            // If expectedSha256 was not in version.json, check standalone .sha256 asset
            if (expectedSha256.isBlank() && !checksumFileUrl.isNullOrBlank()) {
                val shaText = fetchTextFile(checksumFileUrl)
                if (!shaText.isNullOrBlank()) {
                    expectedSha256 = extractSha256FromText(shaText)
                }
            }

            if (apkUrl.isNullOrBlank()) {
                return@withContext UpdateCheckResult.NoReleasesFound("Release $tagName exists, but no APK asset (.apk) is attached.")
            }

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
                isNewer = isNewer,
                expectedSha256 = expectedSha256
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
     * Downloads the APK file to the app's cache directory with redirect safety and SHA-256 verification.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        targetFileName: String,
        expectedSha256: String = "",
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
                if (!isValidUrl(currentUrl)) {
                    return@withContext Result.failure(
                        SecurityException("Untrusted download URL or insecure redirect rejected: $currentUrl")
                    )
                }

                val url = URL(currentUrl)
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.setRequestProperty("User-Agent", "ExamPrepCSV-Android")

                val status = conn.responseCode
                if (status in 300..399) {
                    val location = conn.getHeaderField("Location")
                        ?: return@withContext Result.failure(Exception("Redirect without Location header"))
                    conn.disconnect()

                    // Resolve relative URLs if any
                    val resolvedLocation = URL(URL(currentUrl), location).toString()
                    if (!isValidUrl(resolvedLocation)) {
                        return@withContext Result.failure(
                            SecurityException("Insecure or untrusted redirect destination: $resolvedLocation")
                        )
                    }

                    currentUrl = resolvedLocation
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

            // Verify SHA-256 Checksum
            if (expectedSha256.isNotBlank()) {
                val actualHash = calculateSha256(outputFile)
                if (!actualHash.equals(expectedSha256.trim(), ignoreCase = true)) {
                    outputFile.delete()
                    return@withContext Result.failure(
                        SecurityException(
                            "SHA-256 checksum mismatch!\nExpected: ${expectedSha256.trim()}\nActual: $actualHash\nDownloaded file was discarded for security."
                        )
                    )
                }
            } else {
                // When expectedSha256 is blank, warn/fail as unverified package
                outputFile.delete()
                return@withContext Result.failure(
                    SecurityException(
                        "Release is missing SHA-256 verification checksum (version.json or .sha256). Update aborted for security."
                    )
                )
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * Validates that an update/asset URL is HTTPS and points strictly to github.com or githubusercontent.com.
     */
    fun isValidUrl(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            if (!url.protocol.equals("https", ignoreCase = true)) {
                return false
            }
            val host = url.host.lowercase(Locale.ROOT)
            host == "github.com" ||
                host == "api.github.com" ||
                host.endsWith(".github.com") ||
                host == "githubusercontent.com" ||
                host.endsWith(".githubusercontent.com")
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Calculates the SHA-256 hex digest of a local file.
     */
    fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Parses the version.json metadata payload.
     * Works on both Android runtime and pure JVM unit tests without requiring mocked android.jar.
     */
    fun parseVersionJson(jsonString: String): VersionMetadata {
        val codeRegex = Regex(""""versionCode"\s*:\s*(\d+)""")
        val nameRegex = Regex(""""versionName"\s*:\s*"([^"]+)"""")
        val apkRegex = Regex(""""apkFileName"\s*:\s*"([^"]+)"""")
        val shaRegex = Regex(""""sha256"\s*:\s*"([^"]+)"""")

        val code = codeRegex.find(jsonString)?.groupValues?.get(1)?.toIntOrNull()
            ?: try { JSONObject(jsonString).getInt("versionCode") } catch (_: Throwable) { 0 }
        val name = nameRegex.find(jsonString)?.groupValues?.get(1)
            ?: try { JSONObject(jsonString).getString("versionName") } catch (_: Throwable) { "" }
        val apk = apkRegex.find(jsonString)?.groupValues?.get(1)
            ?: try { JSONObject(jsonString).optString("apkFileName", "") } catch (_: Throwable) { "" }
        val sha = shaRegex.find(jsonString)?.groupValues?.get(1)
            ?: try { JSONObject(jsonString).optString("sha256", "") } catch (_: Throwable) { "" }

        return VersionMetadata(
            versionCode = code,
            versionName = name,
            apkFileName = apk,
            sha256 = sha.trim()
        )
    }

    /**
     * Extracts SHA-256 hex string from standard sha256sum format:
     * "<hash>  <filename>" or just raw hash.
     */
    fun extractSha256FromText(text: String): String {
        val firstToken = text.trim().split(Regex("""\s+""")).firstOrNull() ?: ""
        return if (firstToken.length == 64 && firstToken.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) {
            firstToken.lowercase(Locale.ROOT)
        } else {
            ""
        }
    }

    /**
     * Safe helper to fetch small text asset (like version.json or *.sha256) over HTTPS.
     */
    private fun fetchTextFile(urlString: String): String? {
        if (!isValidUrl(urlString)) return null
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.instanceFollowRedirects = true
            conn.setRequestProperty("User-Agent", "ExamPrepCSV-Android")
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    fun isRemoteVersionNewer(
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

    fun compareSemver(v1: String, v2: String): Int {
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

    private fun extractVersionCode(
        releaseName: String,
        body: String,
        assetName: String,
        versionName: String
    ): Int {
        val codeRegex = Regex("""(?:versionCode|build|code)[\s:=]+(\d+)""", RegexOption.IGNORE_CASE)
        val matchBody = codeRegex.find(body)
        if (matchBody != null) {
            val code = matchBody.groupValues[1].toIntOrNull()
            if (code != null) return code
        }

        val matchName = codeRegex.find(releaseName)
        if (matchName != null) {
            val code = matchName.groupValues[1].toIntOrNull()
            if (code != null) return code
        }

        val matchAsset = Regex("""-b(\d+)""").find(assetName)
        if (matchAsset != null) {
            val code = matchAsset.groupValues[1].toIntOrNull()
            if (code != null) return code
        }

        return parseSemverToCode(versionName)
    }

    private fun parseSemverToCode(version: String): Int {
        val parts = version.split(".").mapNotNull { it.takeWhile { c -> c.isDigit() }.toIntOrNull() }
        val major = parts.getOrElse(0) { 0 }
        val minor = parts.getOrElse(1) { 0 }
        val patch = parts.getOrElse(2) { 0 }
        return major * 10000 + minor * 100 + patch
    }

    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

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
}
