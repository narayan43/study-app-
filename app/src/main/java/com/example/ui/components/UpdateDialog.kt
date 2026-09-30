package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.EasySolid
import com.example.util.AppReleaseInfo
import com.example.util.AppUpdateManager
import com.example.util.UpdateCheckResult
import kotlinx.coroutines.launch
import java.io.File

sealed class UpdateDialogState {
    object Checking : UpdateDialogState()
    data class UpdateFound(val release: AppReleaseInfo) : UpdateDialogState()
    data class Downloading(val release: AppReleaseInfo, val percent: Int, val bytesDownloaded: Long, val totalBytes: Long) : UpdateDialogState()
    data class ReadyToInstall(val release: AppReleaseInfo, val apkFile: File) : UpdateDialogState()
    data class NeedsPermission(val release: AppReleaseInfo, val apkFile: File) : UpdateDialogState()
    data class UpToDate(val currentVersion: String, val currentCode: Int) : UpdateDialogState()
    data class Error(val message: String) : UpdateDialogState()
}

@Composable
fun UpdateDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UpdateDialogState>(UpdateDialogState.Checking) }

    LaunchedEffect(Unit) {
        val result = AppUpdateManager.checkForUpdate()
        state = when (result) {
            is UpdateCheckResult.UpdateAvailable -> UpdateDialogState.UpdateFound(result.release)
            is UpdateCheckResult.UpToDate -> UpdateDialogState.UpToDate(result.currentVersionName, result.currentVersionCode)
            is UpdateCheckResult.NoReleasesFound -> UpdateDialogState.Error(result.message)
            is UpdateCheckResult.Error -> UpdateDialogState.Error(result.message)
        }
    }

    fun startDownload(release: AppReleaseInfo) {
        scope.launch {
            state = UpdateDialogState.Downloading(release, 0, 0L, release.apkSizeBytes)
            val result = AppUpdateManager.downloadApk(
                context = context,
                downloadUrl = release.apkDownloadUrl,
                targetFileName = release.apkFileName,
                onProgress = { downloaded, total, percent ->
                    state = UpdateDialogState.Downloading(release, percent, downloaded, total)
                }
            )

            result.fold(
                onSuccess = { apkFile ->
                    if (!AppUpdateManager.canRequestPackageInstalls(context)) {
                        state = UpdateDialogState.NeedsPermission(release, apkFile)
                    } else {
                        state = UpdateDialogState.ReadyToInstall(release, apkFile)
                        // Trigger immediate install attempt
                        val installResult = AppUpdateManager.startInstall(context, apkFile)
                        if (installResult.isFailure) {
                            val err = installResult.exceptionOrNull()?.message ?: "Install session failed"
                            state = UpdateDialogState.Error(
                                "Install failed: $err.\n\nTroubleshooting: Ensure the new APK is signed with the same key and has a higher versionCode. Never uninstall to protect your data."
                            )
                        }
                    }
                },
                onFailure = { err ->
                    state = UpdateDialogState.Error("Download failed: ${err.message}")
                }
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            when (val s = state) {
                is UpdateDialogState.UpdateFound -> {
                    Button(
                        onClick = { startDownload(s.release) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download & Update", fontWeight = FontWeight.Bold)
                    }
                }
                is UpdateDialogState.ReadyToInstall -> {
                    Button(
                        onClick = {
                            val installResult = AppUpdateManager.startInstall(context, s.apkFile)
                            if (installResult.isFailure) {
                                val err = installResult.exceptionOrNull()?.message ?: "Install error"
                                state = UpdateDialogState.Error("Install failed: $err")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EasySolid,
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Install Update Now", fontWeight = FontWeight.Bold)
                    }
                }
                is UpdateDialogState.NeedsPermission -> {
                    Button(
                        onClick = {
                            context.startActivity(AppUpdateManager.createInstallPermissionIntent(context))
                            Toast.makeText(context, "Grant 'Allow from this source', then return to install", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Allow App Installs", fontWeight = FontWeight.Bold)
                    }
                }
                is UpdateDialogState.UpToDate, is UpdateDialogState.Error -> {
                    Button(onClick = onDismiss) {
                        Text("Done")
                    }
                }
                is UpdateDialogState.Checking, is UpdateDialogState.Downloading -> {
                    // No confirm button while active
                }
            }
        },
        dismissButton = {
            if (state !is UpdateDialogState.Downloading) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Updates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (val s = state) {
                    is UpdateDialogState.Checking -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Checking GitHub Releases...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    is UpdateDialogState.UpToDate -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EasySolid,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "You're up to date!",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Installed: v${s.currentVersion} (build ${s.currentCode})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = "In-Place Update Guarantee: Any upcoming updates downloaded through GitHub or sideload will update in-place without losing your notes, HTML files, attempts, or CSV data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is UpdateDialogState.UpdateFound -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = s.release.releaseName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    val mb = if (s.release.apkSizeBytes > 0) " (${s.release.apkSizeBytes / (1024 * 1024)} MB)" else ""
                                    Text(
                                        text = "v${s.release.remoteVersionName}$mb",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Current: v${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE}) → New: code ${s.release.remoteVersionCode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (s.release.changelog.isNotBlank()) {
                            Text(
                                text = "What's New:",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 140.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(
                                    text = s.release.changelog,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .verticalScroll(rememberScrollState())
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "In-Place Update: Local CSV files, notes, HTML, and attempt logs are preserved. No uninstall required.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is UpdateDialogState.Downloading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Downloading ${s.release.apkFileName}...",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            LinearProgressIndicator(
                                progress = { if (s.percent > 0) s.percent / 100f else 0.1f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            val mbDownloaded = s.bytesDownloaded / (1024 * 1024f)
                            val mbTotal = s.totalBytes / (1024 * 1024f)
                            Text(
                                text = if (s.totalBytes > 0) {
                                    "${s.percent}% (%.1f / %.1f MB)".format(mbDownloaded, mbTotal)
                                } else {
                                    "%.1f MB downloaded".format(mbDownloaded)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is UpdateDialogState.NeedsPermission -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Permission Required",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Android requires your permission to install APK updates directly from this app. Tap 'Allow App Installs' below, enable the toggle, then return to complete the update.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    is UpdateDialogState.ReadyToInstall -> {
                        Text(
                            text = "Download complete! Tap 'Install Update Now' to update the app in-place. All your data and notes will remain intact.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    is UpdateDialogState.Error -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Update Information",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = s.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Text(
                            text = "Sideload Rule: If Android ever asks to uninstall first, the APK signature does not match or the package changed. The solution is to ensure both builds use the same release keystore — never uninstall.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}
