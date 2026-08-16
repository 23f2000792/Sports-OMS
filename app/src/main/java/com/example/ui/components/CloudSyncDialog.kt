package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.firebase.CloudConnectionStatus
import com.example.ui.theme.*
import com.example.viewmodel.SportsOpsViewModel

@Composable
fun CloudSyncDialog(
    viewModel: SportsOpsViewModel,
    onDismiss: () -> Unit
) {
    val syncSummary by viewModel.cloudSyncSummary.collectAsState()
    val isSyncing by viewModel.isSyncingCloud.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val events by viewModel.events.collectAsState()
    val issues by viewModel.issues.collectAsState()
    val calendar by viewModel.calendarItems.collectAsState()
    val approvals by viewModel.approvals.collectAsState()
    val readiness by viewModel.readinessRequirements.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = SportsAmberLight.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Cloud Sync",
                                    tint = SportsAmberLight
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cloud Backend & Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Firebase Firestore Realtime Engine",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Cloud Connection Status Banner
                    val statusBg = when (syncSummary.status) {
                        CloudConnectionStatus.ONLINE -> Color(0xFFDCFCE7)
                        CloudConnectionStatus.SYNCING -> Color(0xFFFEF3C7)
                        CloudConnectionStatus.ERROR -> Color(0xFFFEE2E2)
                        else -> Color(0xFFF1F5F9)
                    }
                    val statusTextColor = when (syncSummary.status) {
                        CloudConnectionStatus.ONLINE -> Color(0xFF166534)
                        CloudConnectionStatus.SYNCING -> Color(0xFF92400E)
                        CloudConnectionStatus.ERROR -> Color(0xFF991B1B)
                        else -> Color(0xFF334155)
                    }
                    val statusDotColor = when (syncSummary.status) {
                        CloudConnectionStatus.ONLINE -> Color(0xFF22C55E)
                        CloudConnectionStatus.SYNCING -> SportsAmberLight
                        CloudConnectionStatus.ERROR -> SportsCrimsonDanger
                        else -> Color(0xFF64748B)
                    }

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(statusDotColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = syncSummary.status.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = statusTextColor
                                )
                                Text(
                                    text = syncSummary.statusMessage,
                                    fontSize = 11.sp,
                                    color = statusTextColor.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // Result Message Toast if present
                    AnimatedVisibility(visible = statusMessage != null) {
                        Surface(
                            color = if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isSuccess) Color(0xFF166534) else SportsCrimsonDanger,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = statusMessage ?: "",
                                    fontSize = 12.sp,
                                    color = if (isSuccess) Color(0xFF166534) else Color(0xFF991B1B)
                                )
                            }
                        }
                    }

                    // System Architecture Specs Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Operational Data Sync State",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            SyncStatRow(label = "Active Tasks in Scope", count = tasks.size.toString(), collection = "sports_tasks")
                            SyncStatRow(label = "Inter-College Events", count = events.size.toString(), collection = "sports_events")
                            SyncStatRow(label = "Readiness Requirements", count = readiness.size.toString(), collection = "sports_readiness")
                            SyncStatRow(label = "Raised Issues & Blockers", count = issues.size.toString(), collection = "sports_issues")
                            SyncStatRow(label = "Master Schedule Milestones", count = calendar.size.toString(), collection = "sports_calendar")
                            SyncStatRow(label = "Pending Core Approvals", count = approvals.size.toString(), collection = "sports_approvals")
                        }
                    }

                    // Architecture Highlights
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SportsNavyDark.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Dual-Layer Resilience",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SportsNavyDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Local Room SQLite cache guarantees instant response time, offline operation, and reliable persistence.\n" +
                                       "• Firebase Cloud Firestore automatically synchronizes updates in real-time across devices, committee leads, and coordinators.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            viewModel.syncAllToCloud { success, message ->
                                isSuccess = success
                                statusMessage = message
                            }
                        },
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SportsNavyDark)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pushing...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Push to Cloud", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatRow(
    label: String,
    count: String,
    collection: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = "Collection: $collection", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
            color = SportsNavyDark.copy(alpha = 0.1f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = count,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = SportsNavyDark,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
