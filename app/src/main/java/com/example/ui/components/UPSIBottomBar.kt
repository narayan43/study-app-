package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.VideoLibrary
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
import com.example.ui.theme.PoliceGoldLight
import com.example.ui.theme.PoliceNavyDark
import com.example.ui.theme.PoliceNavyPrimary

enum class AppTab(
    val route: String,
    val title: String,
    val hindiTitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD(
        "dashboard",
        "Home",
        "होम",
        Icons.Filled.Home,
        Icons.Outlined.Home
    ),
    PRACTICE(
        "practice",
        "Practice",
        "प्रश्न",
        Icons.Filled.MenuBook,
        Icons.Outlined.MenuBook
    ),
    NOTES(
        "notes",
        "Notes",
        "नोट्स",
        Icons.Filled.Description,
        Icons.Outlined.Description
    ),
    VIDEOS(
        "videos",
        "Videos",
        "क्लासेस",
        Icons.Filled.VideoLibrary,
        Icons.Outlined.VideoLibrary
    ),
    MOCK_TESTS(
        "mock_tests",
        "Tests",
        "मॉक टेस्ट",
        Icons.Filled.Assignment,
        Icons.Outlined.Assignment
    ),
    ANALYTICS(
        "analytics",
        "Analytics",
        "रिपोर्ट",
        Icons.Filled.AutoGraph,
        Icons.Outlined.AutoGraph
    ),
    WEEKLY_REVIEWS(
        "reviews",
        "Reviews",
        "डायरी",
        Icons.Filled.RateReview,
        Icons.Outlined.RateReview
    )
}

val MainBottomBarTabs = listOf(
    AppTab.DASHBOARD,
    AppTab.PRACTICE,
    AppTab.NOTES,
    AppTab.VIDEOS,
    AppTab.MOCK_TESTS
)

@Composable
fun UPSIBottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    isHindi: Boolean = true
) {
    NavigationBar(
        containerColor = PoliceNavyPrimary,
        contentColor = Color.White,
        windowInsets = WindowInsets.navigationBars
    ) {
        MainBottomBarTabs.forEach { tab ->
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
                        text = if (isHindi) tab.hindiTitle else tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PoliceNavyDark,
                    selectedTextColor = PoliceGoldLight,
                    indicatorColor = PoliceGoldLight,
                    unselectedIconColor = Color.White.copy(alpha = 0.65f),
                    unselectedTextColor = Color.White.copy(alpha = 0.65f)
                ),
                modifier = Modifier.testTag("nav_tab_${tab.route}")
            )
        }
    }
}
