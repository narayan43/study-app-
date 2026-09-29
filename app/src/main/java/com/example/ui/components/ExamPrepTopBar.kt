package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppOnPrimary
import com.example.ui.theme.AppPrimary
import com.example.ui.theme.AppPrimaryDark
import com.example.ui.theme.AppSurfaceDark
import com.example.ui.theme.AppTextPrimaryDark
import com.example.ui.theme.EasySolid
import com.example.ui.theme.EasySolidDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamPrepTopBar(
    isFolderLinked: Boolean,
    isDarkTheme: Boolean,
    onPickFolder: () -> Unit,
    onReloadData: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val barContainer = if (isDarkTheme) AppSurfaceDark else AppPrimary
    val barContent = if (isDarkTheme) AppTextPrimaryDark else AppOnPrimary

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ExamPrep CSV",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = barContent
                )
                Spacer(modifier = Modifier.width(10.dp))

                val chipBg = if (isFolderLinked) {
                    if (isDarkTheme) EasySolidDark.copy(alpha = 0.25f) else EasySolid
                } else {
                    if (isDarkTheme) AppPrimaryDark.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.2f)
                }

                val chipTextColor = if (isDarkTheme) {
                    if (isFolderLinked) EasySolidDark else AppPrimaryDark
                } else {
                    Color.White
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(chipBg)
                        .clickable { onPickFolder() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFolderLinked) Icons.Default.FolderOpen else Icons.Default.Folder,
                            contentDescription = null,
                            tint = chipTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFolderLinked) "SAF Linked" else "Demo Data",
                            color = chipTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        actions = {
            // Theme toggle (Sun/Moon icon)
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                    tint = barContent
                )
            }
            IconButton(onClick = onReloadData) {
                Icon(Icons.Default.Refresh, contentDescription = "Reload Data", tint = barContent)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = barContainer,
            titleContentColor = barContent,
            actionIconContentColor = barContent
        )
    )
}
