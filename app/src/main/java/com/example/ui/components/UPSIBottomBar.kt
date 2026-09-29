package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.AppDivider
import com.example.ui.theme.AppDividerDark
import com.example.ui.theme.AppOnPrimary
import com.example.ui.theme.AppOnPrimaryDark
import com.example.ui.theme.AppPrimary
import com.example.ui.theme.AppPrimaryDark
import com.example.ui.theme.AppSurface
import com.example.ui.theme.AppSurfaceDark
import com.example.ui.theme.AppTextPrimary
import com.example.ui.theme.AppTextSecondary
import com.example.ui.theme.AppTextSecondaryDark

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

@Composable
fun ExamPrepBottomBar(
    currentTab: AppTab,
    isDarkTheme: Boolean,
    onTabSelected: (AppTab) -> Unit
) {
    val barContainer = if (isDarkTheme) AppSurfaceDark else AppSurface
    val dividerColor = if (isDarkTheme) AppDividerDark else AppDivider
    val indicatorColor = if (isDarkTheme) AppPrimaryDark else AppPrimary
    val selectedIconColor = if (isDarkTheme) AppOnPrimaryDark else AppOnPrimary
    val selectedTextColor = if (isDarkTheme) AppPrimaryDark else AppPrimary
    val unselectedColor = if (isDarkTheme) AppTextSecondaryDark else AppTextSecondary

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = dividerColor)
        NavigationBar(
            containerColor = barContainer,
            contentColor = selectedTextColor,
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
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconColor,
                        selectedTextColor = selectedTextColor,
                        indicatorColor = indicatorColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor
                    ),
                    modifier = Modifier.testTag("nav_tab_${tab.route}")
                )
            }
        }
    }
}
