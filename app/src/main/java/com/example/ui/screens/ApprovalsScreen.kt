package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SportsOpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalsScreen(
    viewModel: SportsOpsViewModel
) {
    val approvals by viewModel.approvals.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var statusFilter by remember { mutableStateOf<CoreApprovalStatus?>(CoreApprovalStatus.PENDING) }
    var activeApprovalForAction by remember { mutableStateOf<ApprovalItem?>(null) }
    var actionType by remember { mutableStateOf<CoreApprovalStatus?>(null) }
    var remarksInput by remember { mutableStateOf("") }

    val filteredApprovals = approvals.filter {
        statusFilter == null || it.status == statusFilter
    }

    val isCore = currentUser.role == UserRole.CORE || currentUser.role == UserRole.DEPUTY_CORE

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Core Approvals Inbox", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Gatekeeping & executive decisions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // STATUS FILTER CHIPS
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { statusFilter = null },
                        label = { Text("All (${approvals.size})", fontSize = 12.sp) }
                    )
                }
                CoreApprovalStatus.values().forEach { st ->
                    val count = approvals.count { it.status == st }
                    item {
                        FilterChip(
                            selected = statusFilter == st,
                            onClick = { statusFilter = if (statusFilter == st) null else st },
                            label = { Text("${st.name} ($count)", fontSize = 12.sp) }
                        )
                    }
                }
            }

            if (filteredApprovals.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(32.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusCompleted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Inbox is clear", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("No pending requests matching the selected filter.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredApprovals, key = { it.id }) { item ->
                        val (statusBg, statusText) = when (item.status) {
                            CoreApprovalStatus.PENDING -> Color(0xFFFEF3C7) to Color(0xFF92400E)
                            CoreApprovalStatus.APPROVED -> Color(0xFFDCFCE7) to Color(0xFF166534)
                            CoreApprovalStatus.REJECTED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
                            CoreApprovalStatus.REWORK_REQUESTED -> Color(0xFFFFEDD5) to Color(0xFFC2410C)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(color = SportsNavyDark, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            text = item.type.displayName,
                                            color = SportsAmberLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Surface(color = statusBg, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            text = item.status.displayName,
                                            color = statusText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Requested by: ${item.requestedBy}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Date: ${item.requestedDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                if (item.remarks.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Core Remarks: ${item.remarks}",
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                // Core Action Buttons
                                if (isCore && item.status == CoreApprovalStatus.PENDING) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                activeApprovalForAction = item
                                                actionType = CoreApprovalStatus.APPROVED
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Approve", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                activeApprovalForAction = item
                                                actionType = CoreApprovalStatus.REWORK_REQUESTED
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SportsAmberPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Rework", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                activeApprovalForAction = item
                                                actionType = CoreApprovalStatus.REJECTED
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SportsCrimsonDanger),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reject", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // REMARKS MODAL FOR APPROVAL / REJECTION / REWORK
    activeApprovalForAction?.let { item ->
        actionType?.let { act ->
            AlertDialog(
                onDismissRequest = {
                    activeApprovalForAction = null
                    actionType = null
                },
                title = { Text("${act.name} Decision") },
                text = {
                    Column {
                        Text("Add decision remarks / notes for the team:", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = remarksInput,
                            onValueChange = { remarksInput = it },
                            label = { Text("Decision Notes") },
                            placeholder = { Text("e.g. Approved with condition that referee licenses are verified.") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.handleApprovalAction(item.id, act, remarksInput.trim().ifEmpty { "Signed off by Core" })
                            activeApprovalForAction = null
                            actionType = null
                            remarksInput = ""
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            activeApprovalForAction = null
                            actionType = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
