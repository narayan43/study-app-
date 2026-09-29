package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppPrimary
import com.example.ui.theme.EasySolid
import com.example.ui.theme.HardSolid
import com.example.ui.theme.MediumSolid

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color = AppPrimary,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SectionBadge(
    subject: String,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeText) = when {
        subject.contains("Hindi", ignoreCase = true) || subject.contains("हिन्दी") ->
            Pair(AppPrimary.copy(alpha = 0.12f), AppPrimary)
        subject.contains("Law", ignoreCase = true) || subject.contains("विधि") ->
            Pair(HardSolid.copy(alpha = 0.12f), HardSolid)
        subject.contains("Numerical", ignoreCase = true) || subject.contains("गणित") || subject.contains("संख्यात्मक") ->
            Pair(EasySolid.copy(alpha = 0.12f), EasySolid)
        else ->
            Pair(MediumSolid.copy(alpha = 0.12f), MediumSolid)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = badgeBg
    ) {
        Text(
            text = subject,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = badgeText
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
