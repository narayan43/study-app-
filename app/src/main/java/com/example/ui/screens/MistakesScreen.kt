package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrillDownFilter
import com.example.data.model.TestSliceSource
import com.example.ui.theme.EasySolid
import com.example.ui.theme.HardSolid
import com.example.ui.theme.HardTint
import com.example.ui.viewmodel.ExamPrepViewModel

@Composable
fun MistakesScreen(
    viewModel: ExamPrepViewModel,
    onStartTestSlice: (TestSliceSource) -> Unit,
    onOpenNotesForChapter: (DrillDownFilter) -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val mistakesMap = remember(stats) { viewModel.getMistakesGrouped() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Mistakes Notebook",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HardSolid
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Grouped by Exam → Subject → Chapter from attempts.csv (is_correct = 0)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (mistakesMap.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EasySolid,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No recorded mistakes!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Any incorrect answers in test sessions will appear here for targeted revision.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                mistakesMap.forEach { (exam, subMap) ->
                    item {
                        Text(
                            text = "Exam: $exam",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    subMap.forEach { (subject, chapMap) ->
                        item {
                            Text(
                                text = "Subject: $subject",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        items(chapMap.toList()) { (chapter, qIds) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp),
                                shape = RoundedCornerShape(12.dp),
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = chapter,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${qIds.size} wrong question(s)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = HardSolid,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(HardTint),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = HardSolid,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onStartTestSlice(
                                                    TestSliceSource.MistakesRetest(
                                                        exam = exam,
                                                        subject = subject,
                                                        chapter = chapter,
                                                        questionIds = qIds
                                                    )
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Retest", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                onOpenNotesForChapter(
                                                    DrillDownFilter(
                                                        exam = exam,
                                                        subject = subject,
                                                        chapter = chapter
                                                    )
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Open Notes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
