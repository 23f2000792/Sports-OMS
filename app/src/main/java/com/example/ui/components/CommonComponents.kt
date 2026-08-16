package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun HealthBadge(health: TaskHealth, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (health) {
        TaskHealth.COMPLETED -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), Icons.Default.CheckCircle)
        TaskHealth.ON_TRACK -> Triple(Color(0xFFDCFCE7), Color(0xFF166534), Icons.Default.TrendingUp)
        TaskHealth.AT_RISK -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), Icons.Default.Warning)
        TaskHealth.OVERDUE -> Triple(Color(0xFFFFE4E6), Color(0xFF9F1239), Icons.Default.Error)
        TaskHealth.BLOCKED -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), Icons.Default.Block)
        TaskHealth.NO_DEADLINE -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Icons.Default.Schedule)
        TaskHealth.CANCELLED -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), Icons.Default.Cancel)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = health.displayName.uppercase(),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun StatusChip(status: TaskStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        TaskStatus.NOT_STARTED -> Color(0xFFE2E8F0) to Color(0xFF334155)
        TaskStatus.IN_PROGRESS -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        TaskStatus.BLOCKED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        TaskStatus.COMPLETED -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        TaskStatus.CANCELLED -> Color(0xFFF1F5F9) to Color(0xFF64748B)
        TaskStatus.ON_HOLD -> Color(0xFFFEF3C7) to Color(0xFF92400E)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PriorityChip(priority: Priority, modifier: Modifier = Modifier) {
    val (color, text) = when (priority) {
        Priority.CRITICAL -> Color(0xFFDC2626) to "CRITICAL"
        Priority.HIGH -> Color(0xFFEA580C) to "HIGH"
        Priority.MEDIUM -> Color(0xFFD97706) to "MEDIUM"
        Priority.LOW -> Color(0xFF4B5563) to "LOW"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RoleBadge(role: UserRole, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (role) {
        UserRole.CORE -> Color(0xFF0B132B) to Color(0xFFFCD34D)
        UserRole.DEPUTY_CORE -> Color(0xFF1C2541) to Color(0xFF67E8F9)
        UserRole.SUPER_COORDINATOR -> Color(0xFF3B1F5E) to Color(0xFFE9D5FF)
        UserRole.COORDINATOR -> Color(0xFF1E3A8A) to Color(0xFFBFDBFE)
        UserRole.VOLUNTEER -> Color(0xFF064E3B) to Color(0xFFA7F3D0)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = role.displayName.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun UserAvatar(name: String, colorHex: Long = 0xFF1E88E5, size: Int = 36) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(colorHex))
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size * 0.4).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun KPIStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    subtext: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun LinearProgressWithLabel(
    progressPercent: Int,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$progressPercent%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progressPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
