package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrillDownFilter

@Composable
fun DrillDownSelector(
    title: String,
    exams: List<String>,
    currentFilter: DrillDownFilter,
    getSubjects: (String) -> List<String>,
    getChapters: (String, String) -> List<String>,
    getTopics: (String, String, String) -> List<String>,
    onFilterChanged: (DrillDownFilter) -> Unit,
    onSliceReady: (DrillDownFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Breadcrumbs & Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Active Breadcrumb trail
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "All Exams",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (currentFilter.exam == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentFilter.exam == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable {
                            onFilterChanged(DrillDownFilter())
                        }
                    )

                    if (currentFilter.exam != null) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = currentFilter.exam,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (currentFilter.subject == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentFilter.subject == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable {
                                onFilterChanged(DrillDownFilter(exam = currentFilter.exam))
                            }
                        )
                    }

                    if (currentFilter.subject != null) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = currentFilter.subject,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (currentFilter.chapter == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentFilter.chapter == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable {
                                onFilterChanged(DrillDownFilter(exam = currentFilter.exam, subject = currentFilter.subject))
                            }
                        )
                    }

                    if (currentFilter.chapter != null) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = currentFilter.chapter,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Selection List
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                // Step 1: Select Exam
                currentFilter.exam == null -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(exams) { exam ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onFilterChanged(DrillDownFilter(exam = exam))
                                            }
                                    ) {
                                        Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = exam, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(text = "Tap to choose subject", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val filter = DrillDownFilter(exam = exam)
                                            onFilterChanged(filter)
                                            onSliceReady(filter)
                                        }
                                    ) {
                                        Text("All subjects")
                                    }
                                }
                            }
                        }
                    }
                }

                // Step 2: Select Subject
                currentFilter.subject == null -> {
                    val subjects = getSubjects(currentFilter.exam)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(subjects) { subject ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onFilterChanged(currentFilter.copy(subject = subject))
                                            }
                                    ) {
                                        Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(text = "Tap to choose chapter", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val filter = currentFilter.copy(subject = subject)
                                            onFilterChanged(filter)
                                            onSliceReady(filter)
                                        }
                                    ) {
                                        Text("All chapters")
                                    }
                                }
                            }
                        }
                    }
                }

                // Step 3: Select Chapter
                currentFilter.chapter == null -> {
                    val chapters = getChapters(currentFilter.exam, currentFilter.subject!!)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(chapters) { chapter ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val topics = getTopics(currentFilter.exam, currentFilter.subject, chapter)
                                        if (topics.size > 1) {
                                            onFilterChanged(currentFilter.copy(chapter = chapter))
                                        } else {
                                            val filter = currentFilter.copy(chapter = chapter)
                                            onFilterChanged(filter)
                                            onSliceReady(filter)
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = chapter, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Step 4: Optional Topic Selection (if multiple topics exist)
                else -> {
                    val topics = getTopics(currentFilter.exam, currentFilter.subject!!, currentFilter.chapter!!)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Button(
                                onClick = {
                                    val filter = currentFilter.copy(topic = null)
                                    onFilterChanged(filter)
                                    onSliceReady(filter)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("All topics in ${currentFilter.chapter}")
                            }
                        }
                        items(topics) { topic ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val filter = currentFilter.copy(topic = topic)
                                        onFilterChanged(filter)
                                        onSliceReady(filter)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = topic, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
