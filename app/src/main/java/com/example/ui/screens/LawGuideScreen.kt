package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LawArticle
import com.example.data.model.LawCategory
import com.example.data.sample.UPSILawGuideData
import com.example.ui.theme.PoliceGoldDark
import com.example.ui.theme.PoliceGoldLight
import com.example.ui.theme.PoliceNavyDark
import com.example.ui.theme.PoliceNavyPrimary
import com.example.ui.theme.PoliceRedTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LawGuideScreen(
    onBack: () -> Unit,
    isHindi: Boolean = true
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<LawCategory?>(null) }

    val allArticles = remember { UPSILawGuideData.articles }

    val filtered = remember(searchQuery, selectedCategory) {
        allArticles.filter { article ->
            val matchesCategory = selectedCategory == null || article.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    article.titleHindi.contains(searchQuery, ignoreCase = true) ||
                    article.titleEnglish.contains(searchQuery, ignoreCase = true) ||
                    article.keyPointsHindi.contains(searchQuery, ignoreCase = true) ||
                    article.keyPointsEnglish.contains(searchQuery, ignoreCase = true) ||
                    article.landmarkCaseOrNote.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("law_guide_screen")
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = if (isHindi) "मूल विधि एवं संविधान हैंडबुक" else "Mool Vidhi & Law Guide",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "IPC, CrPC, संविधान व विशेष अधिनियम",
                        style = MaterialTheme.typography.bodySmall.copy(color = PoliceGoldLight, fontSize = 11.sp)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = PoliceNavyPrimary)
        )

        // Search bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isHindi) "धारा, अनुच्छेद या कीवर्ड खोजें (उदा: 302, FIR, 32)..." else "Search section, article, keyword...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Categories Row
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text(if (isHindi) "सभी" else "All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PoliceNavyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(LawCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(if (isHindi) cat.hindiName else cat.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PoliceNavyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // List of Articles
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filtered) { article ->
                LawArticleCard(article = article, isHindi = isHindi)
            }
        }
    }
}

@Composable
private fun LawArticleCard(
    article: LawArticle,
    isHindi: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (article.category) {
                        LawCategory.IPC -> Color(0xFFFFEBEE)
                        LawCategory.CRPC -> Color(0xFFE8EAF6)
                        LawCategory.CONSTITUTION -> Color(0xFFE0F2F1)
                        LawCategory.SPECIAL_ACTS -> Color(0xFFFFF3E0)
                    }
                ) {
                    Text(
                        text = if (isHindi) article.category.hindiName else article.category.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (article.category) {
                                LawCategory.IPC -> Color(0xFFB71C1C)
                                LawCategory.CRPC -> Color(0xFF1A237E)
                                LawCategory.CONSTITUTION -> Color(0xFF004D40)
                                LawCategory.SPECIAL_ACTS -> Color(0xFFE65100)
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isHindi) article.titleHindi else article.titleEnglish,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            if (isHindi && article.titleEnglish.isNotBlank() && article.titleEnglish != article.titleHindi) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = article.titleEnglish,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isHindi) article.keyPointsHindi else article.keyPointsEnglish,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Punishment or Key Provision
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "प्रावधान / दण्ड: " else "Penalty / Provision: ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PoliceRedTertiary)
                    )
                    Text(
                        text = article.punishmentOrProvision,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    )
                }
            }

            if (article.landmarkCaseOrNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isHindi) "महत्वपूर्ण केस: " else "Landmark Case: ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PoliceGoldDark)
                    )
                    Text(
                        text = article.landmarkCaseOrNote,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}
