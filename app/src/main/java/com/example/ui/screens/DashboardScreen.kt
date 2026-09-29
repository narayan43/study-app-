package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EasySolid
import com.example.ui.theme.EasyTint
import com.example.ui.theme.HardSolid
import com.example.ui.theme.HardTint
import com.example.ui.theme.MediumSolid
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExamPrepViewModel,
    onPickFolder: () -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val folderPath by viewModel.folderPath.collectAsState()
    val isFolderLinked by viewModel.isFolderLinked.collectAsState()
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Folder Card: Surface, title TextPrimary, path TextSecondary, Link/Reset buttons ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFolderLinked) Icons.Default.FolderOpen else Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (isFolderLinked) EasySolid else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFolderLinked) "External Data/ Folder Linked" else "Internal Demo Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { scope.launch { viewModel.reloadData() } }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "URI / Path: $folderPath",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onPickFolder,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(if (isFolderLinked) "Change Folder" else "Link Data Folder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (isFolderLinked) {
                            OutlinedButton(
                                onClick = { viewModel.unlinkDataFolder() },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Unlink", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.resetToDemoData() },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Demo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // --- Stat Cards: Surface, label TextSecondary, number TextPrimary, small caption TextSecondary ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Questions",
                    value = "${stats.totalQuestions}",
                    subtitle = "questions.csv",
                    icon = Icons.Default.Quiz,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Attempted Today",
                    value = if (stats.attemptedToday > 0) "${stats.attemptedToday}" else "0",
                    subtitle = if (stats.attemptedToday > 0 && stats.todayAccuracyPercent != null)
                        "${stats.todayAccuracyPercent!!.toInt()}% accuracy" else "No attempts today",
                    icon = Icons.Default.CheckCircle,
                    iconColor = EasySolid,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Streak",
                    value = if (stats.streakDays > 0) "${stats.streakDays} Days" else "0 Days",
                    subtitle = if (stats.streakDays > 0) "Consistent learning" else "Start practicing today",
                    icon = Icons.Default.LocalFireDepartment,
                    iconColor = MediumSolid,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Overall Avg Time",
                    value = if (stats.overallAvgTimeSec != null) "${stats.overallAvgTimeSec}s" else "—",
                    subtitle = if (stats.overallAvgTimeSec != null) "Per question speed" else "No logs yet",
                    icon = Icons.Default.Timer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- Overall Avg Speed callout ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Overall Average Speed",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (stats.overallAvgTimeSec != null)
                                "${stats.overallAvgTimeSec} seconds per question across ${stats.totalAttempts} total attempt(s)"
                            else "No attempt logs found in attempts.csv",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (stats.overallAvgTimeSec != null) "${stats.overallAvgTimeSec}s" else "—",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // --- Chart card: Surface. Bars Primary. Axis labels TextSecondary. Accuracy % TextPrimary ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Activity & Accuracy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (stats.dailyAttempts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No attempt logs yet. Answer questions in the Test tab!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val maxCount = stats.dailyAttempts.maxOf { it.count }.coerceAtLeast(1)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            stats.dailyAttempts.forEach { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = "${item.accuracyPercent.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.accuracyPercent >= 60f) EasySolid else HardSolid
                                    )
                                    Text(
                                        text = "${item.count}q",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .height(((item.count.toFloat() / maxCount) * 80).coerceAtLeast(8f).dp)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.date.takeLast(5),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Weak vs Strong Chapters: Weak list: HardSolid labels. Strong list: EasySolid labels. Values TextPrimary ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Weak Chapters
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = HardSolid, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Weak Chapters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = HardSolid)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (stats.weakChapters.isEmpty()) {
                            Text("None (<60%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            stats.weakChapters.forEach { (label, acc) ->
                                Text("• $label: ${acc.toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Strong Chapters
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EasySolid, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Strong Chapters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EasySolid)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (stats.strongChapters.isEmpty()) {
                            Text("None (≥60%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            stats.strongChapters.forEach { (label, acc) ->
                                Text("• $label: ${acc.toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- Per-Chapter Accuracy & Avg Time ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chapter Performance Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (stats.chapterStats.isEmpty()) {
                        Text(
                            text = "No chapter statistics available yet. Answer questions in the Test tab!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        stats.chapterStats.forEach { item ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.fullPathLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${item.accuracyPercent.toInt()}% • ${item.avgTimeSec}s avg (${item.totalAttempts} att)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (item.accuracyPercent >= 60f) EasySolid else HardSolid,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { item.accuracyPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (item.accuracyPercent >= 60f) EasySolid else HardSolid,
                                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
