package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DrillDownFilter
import com.example.ui.theme.EasySolid

@Composable
fun HierarchyPicker(
    lockedFilter: DrillDownFilter,
    availableExams: List<String>,
    getSubjects: (String) -> List<String>,
    getChapters: (String, String) -> List<String>,
    getTopics: (String, String, String) -> List<String>,
    selectedExam: String,
    onExamChange: (String) -> Unit,
    selectedSubject: String,
    onSubjectChange: (String) -> Unit,
    selectedChapter: String,
    onChapterChange: (String) -> Unit,
    selectedTopic: String,
    onTopicChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showTopic: Boolean = true
) {
    // Current options based on cascading selections
    val effectiveExam = lockedFilter.exam ?: selectedExam
    val subjects = remember(effectiveExam, availableExams) {
        if (effectiveExam.isNotBlank()) getSubjects(effectiveExam) else emptyList()
    }

    val effectiveSubject = lockedFilter.subject ?: selectedSubject
    val chapters = remember(effectiveExam, effectiveSubject, subjects) {
        if (effectiveExam.isNotBlank() && effectiveSubject.isNotBlank()) {
            getChapters(effectiveExam, effectiveSubject)
        } else emptyList()
    }

    val effectiveChapter = lockedFilter.chapter ?: selectedChapter
    val topics = remember(effectiveExam, effectiveSubject, effectiveChapter, chapters) {
        if (effectiveExam.isNotBlank() && effectiveSubject.isNotBlank() && effectiveChapter.isNotBlank()) {
            getTopics(effectiveExam, effectiveSubject, effectiveChapter)
        } else emptyList()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Exam Level
        if (lockedFilter.exam != null) {
            LockedHierarchyLevelBadge(levelName = "Exam", value = lockedFilter.exam)
        } else {
            HierarchyLevelPicker(
                label = "Exam *",
                levelName = "Exam",
                currentValue = selectedExam,
                options = availableExams,
                onValueSelected = { newExam ->
                    onExamChange(newExam)
                    // Reset subordinate selections if not valid for new exam
                    val newSubjects = getSubjects(newExam)
                    if (!newSubjects.any { it.equals(selectedSubject, ignoreCase = true) }) {
                        onSubjectChange("")
                        onChapterChange("")
                        onTopicChange("")
                    }
                }
            )
        }

        // 2. Subject Level
        if (lockedFilter.subject != null) {
            LockedHierarchyLevelBadge(levelName = "Subject", value = lockedFilter.subject)
        } else {
            val isEnabled = effectiveExam.isNotBlank()
            HierarchyLevelPicker(
                label = "Subject Name *",
                levelName = "Subject",
                currentValue = selectedSubject,
                options = subjects,
                enabled = isEnabled,
                emptyOptionsHint = if (effectiveExam.isBlank()) "Choose Exam first" else "No subjects in CSV for $effectiveExam",
                onValueSelected = { newSubject ->
                    onSubjectChange(newSubject)
                    // Reset subordinate selections if not valid
                    val newChapters = getChapters(effectiveExam, newSubject)
                    if (!newChapters.any { it.equals(selectedChapter, ignoreCase = true) }) {
                        onChapterChange("")
                        onTopicChange("")
                    }
                }
            )
        }

        // 3. Chapter Level
        if (lockedFilter.chapter != null) {
            LockedHierarchyLevelBadge(levelName = "Chapter", value = lockedFilter.chapter)
        } else {
            val isEnabled = effectiveSubject.isNotBlank()
            HierarchyLevelPicker(
                label = "Chapter Name *",
                levelName = "Chapter",
                currentValue = selectedChapter,
                options = chapters,
                enabled = isEnabled,
                emptyOptionsHint = if (effectiveSubject.isBlank()) "Choose Subject first" else "No chapters in CSV for $effectiveSubject",
                onValueSelected = { newChapter ->
                    onChapterChange(newChapter)
                    val newTopics = getTopics(effectiveExam, effectiveSubject, newChapter)
                    if (!newTopics.any { it.equals(selectedTopic, ignoreCase = true) }) {
                        onTopicChange("")
                    }
                }
            )
        }

        // 4. Topic Level (Optional)
        if (showTopic) {
            if (lockedFilter.topic != null) {
                LockedHierarchyLevelBadge(levelName = "Topic", value = lockedFilter.topic)
            } else {
                val isEnabled = effectiveChapter.isNotBlank()
                HierarchyLevelPicker(
                    label = "Topic (Optional)",
                    levelName = "Topic",
                    currentValue = selectedTopic,
                    options = topics,
                    enabled = isEnabled,
                    emptyOptionsHint = if (effectiveChapter.isBlank()) "Choose Chapter first" else "No topics in CSV (defaults to chapter)",
                    allowEmptySelection = true,
                    onValueSelected = { newTopic ->
                        onTopicChange(newTopic)
                    }
                )
            }
        }
    }
}

@Composable
fun LockedHierarchyLevelBadge(
    levelName: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "$levelName (Locked)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun HierarchyLevelPicker(
    label: String,
    levelName: String,
    currentValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    enabled: Boolean = true,
    emptyOptionsHint: String = "No options available",
    allowEmptySelection: Boolean = false
) {
    var isAddNewMode by remember { mutableStateOf(false) }
    var typedValue by remember { mutableStateOf("") }
    var showPickerDialog by remember { mutableStateOf(false) }

    // If options is empty and user tries to pick, default to add new mode
    LaunchedEffect(currentValue) {
        if (isAddNewMode && currentValue.isNotBlank() && typedValue.isBlank()) {
            typedValue = currentValue
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            if (enabled) {
                TextButton(
                    onClick = {
                        isAddNewMode = !isAddNewMode
                        if (isAddNewMode) {
                            typedValue = currentValue
                        }
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = if (isAddNewMode) Icons.Default.Search else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAddNewMode) "Select from CSV" else "+ Add New $levelName",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isAddNewMode) {
            // Add New free-text field with auto-canonical match check
            val trimmedInput = typedValue.trim()
            val canonicalMatch = options.firstOrNull { it.trim().equals(trimmedInput, ignoreCase = true) }

            OutlinedTextField(
                value = typedValue,
                onValueChange = { newVal ->
                    typedValue = newVal
                    // If matches existing option case-insensitively, canonicalize!
                    val match = options.firstOrNull { it.trim().equals(newVal.trim(), ignoreCase = true) }
                    val finalVal = match ?: newVal.trim()
                    onValueSelected(finalVal)
                },
                enabled = enabled,
                placeholder = { Text("Type new $levelName name...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (canonicalMatch != null && !canonicalMatch.equals(trimmedInput, ignoreCase = false)) {
                        Text(
                            text = "Matches existing \"$canonicalMatch\" in CSV. Canonical spelling will be used.",
                            color = EasySolid,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (trimmedInput.isNotBlank()) {
                        Text(
                            text = "New $levelName will be added upon saving.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        } else {
            // Select from Master CSV Picker (Clickable card opening picker dialog)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled) {
                        showPickerDialog = true
                    },
                shape = RoundedCornerShape(8.dp),
                color = if (enabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                border = BorderStroke(
                    1.dp,
                    if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentValue.isNotBlank()) currentValue else "Choose $levelName from CSV...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (currentValue.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (currentValue.isNotBlank()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.8f else 0.4f)
                        }
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select $levelName",
                        tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }

    if (showPickerDialog) {
        HierarchyPickerDialog(
            title = "Select $levelName",
            levelName = levelName,
            currentValue = currentValue,
            options = options,
            emptyHint = emptyOptionsHint,
            allowEmpty = allowEmptySelection,
            onDismiss = { showPickerDialog = false },
            onSelect = { selected ->
                showPickerDialog = false
                onValueSelected(selected)
            },
            onSwitchToAddNew = {
                showPickerDialog = false
                isAddNewMode = true
                typedValue = ""
            }
        )
    }
}

@Composable
fun HierarchyPickerDialog(
    title: String,
    levelName: String,
    currentValue: String,
    options: List<String>,
    emptyHint: String,
    allowEmpty: Boolean = false,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onSwitchToAddNew: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(options, searchQuery) {
        if (searchQuery.isBlank()) options
        else options.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                if (options.size > 4) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search $levelName...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (options.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = emptyHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onSwitchToAddNew,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add New $levelName")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (allowEmpty) {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelect("") },
                                    color = if (currentValue.isBlank()) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "(None / Default)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        items(filtered) { opt ->
                            val isSelected = opt.equals(currentValue, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelect(opt) },
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Add new item at bottom of list
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSwitchToAddNew() },
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+ Add new $levelName...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
