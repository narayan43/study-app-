package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD(
        "dashboard",
        "Dashboard",
        Icons.Filled.Dashboard,
        Icons.Outlined.Dashboard
    ),
    TEST(
        "test",
        "Test",
        Icons.Filled.Quiz,
        Icons.Outlined.Quiz
    ),
    NOTES(
        "notes",
        "Notes",
        Icons.Filled.Description,
        Icons.Outlined.Description
    ),
    MISTAKES(
        "mistakes",
        "Mistakes",
        Icons.Filled.ErrorOutline,
        Icons.Outlined.ErrorOutline
    ),
    REELS(
        "reels",
        "Reels",
        Icons.Filled.SmartDisplay,
        Icons.Outlined.SmartDisplay
    )
}

val ExamPrepTabs = listOf(
    AppTab.DASHBOARD,
    AppTab.TEST,
    AppTab.NOTES,
    AppTab.MISTAKES,
    AppTab.REELS
)

private val NavyBluePrimary = Color(0xFF0D47A1)
private val LightBlueIndicator = Color(0xFFBBDEFB)
private val DarkBlueText = Color(0xFF0D47A1)

@Composable
fun ExamPrepBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = NavyBluePrimary,
        contentColor = Color.White,
        windowInsets = WindowInsets.navigationBars
    ) {
        ExamPrepTabs.forEach { tab ->
            val selected = tab == currentTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkBlueText,
                    selectedTextColor = Color.White,
                    indicatorColor = LightBlueIndicator,
                    unselectedIconColor = Color.White.copy(alpha = 0.65f),
                    unselectedTextColor = Color.White.copy(alpha = 0.65f)
                ),
                modifier = Modifier.testTag("nav_tab_${tab.route}")
            )
        }
    }
}
