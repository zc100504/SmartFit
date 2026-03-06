// app/src/main/java/com/example/smartfit/ui/navigation/AddLogBottomSheet.kt
package com.example.smartfit.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartfit.ui.theme.DarkBackground
import com.example.smartfit.ui.theme.DarkSurfaceGlass
import com.example.smartfit.ui.theme.LightSurfaceGlass
import com.example.smartfit.ui.theme.LimePrimary
import com.example.smartfit.ui.theme.LimePrimarySoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLogBottomSheet(
    onDismiss: () -> Unit,
    onAddExercise: () -> Unit,
    onAddFood: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background == DarkBackground

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = if (isDark)
            Color(0xFF050816)           // 比背景稍亮一点的底
        else
            Color(0xFFF9FAFB),
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Add Activity",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Choose what you want to log:",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(18.dp))

            AddOptionCard(
                title = "Add Exercise",
                subtitle = "Run, walk, cycling, gym…",
                icon = Icons.Filled.FitnessCenter,
                iconBg = Brush.linearGradient(
                    listOf(LimePrimarySoft, LimePrimary)
                ),
                onClick = onAddExercise
            )

            Spacer(Modifier.height(12.dp))

            AddOptionCard(
                title = "Add Food",
                subtitle = "Log meals and calorie intake",
                icon = Icons.Filled.Fastfood,
                iconBg = Brush.linearGradient(
                    listOf(Color(0x33FACC15), Color(0xFFFACC15))
                ),
                onClick = onAddFood
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AddOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Brush,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background == DarkBackground
    val surfaceColor =
        if (isDark) DarkSurfaceGlass else LightSurfaceGlass

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.06f)
            else Color.White.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
