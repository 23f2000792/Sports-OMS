package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.logic.SportsOpsLogic
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SportsOpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    viewModel: SportsOpsViewModel
) {
    val teamMembers by viewModel.teamMembers.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val issues by viewModel.issues.collectAsState()

    var selectedMemberForDetail by remember { mutableStateOf<TeamMember?>(null) }
    var selectedRoleFilter by remember { mutableStateOf<UserRole?>(null) }

    val filteredMembers = teamMembers.filter {
        selectedRoleFilter == null || it.role == selectedRoleFilter
    }.sortedBy { it.role.ordinal }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sports Team & Hierarchy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${teamMembers.size} operational officers & coordinators", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // HIERARCHY OVERVIEW BANNER
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SportsNavyDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("DEPARTMENT CHAIN OF COMMAND", color = SportsAmberLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Core Leadership (L4) → Deputy Core (L3) → Super Coordinators (L2) → Coordinators & Volunteers (L1)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3 Active Verticals", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("Real-Time Workload Tracking", color = SportsEmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ROLE FILTER CHIPS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedRoleFilter == null,
                        onClick = { selectedRoleFilter = null },
                        label = { Text("All", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRoleFilter == UserRole.CORE,
                        onClick = { selectedRoleFilter = if (selectedRoleFilter == UserRole.CORE) null else UserRole.CORE },
                        label = { Text("Core", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRoleFilter == UserRole.SUPER_COORDINATOR,
                        onClick = { selectedRoleFilter = if (selectedRoleFilter == UserRole.SUPER_COORDINATOR) null else UserRole.SUPER_COORDINATOR },
                        label = { Text("Super Coord", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRoleFilter == UserRole.COORDINATOR,
                        onClick = { selectedRoleFilter = if (selectedRoleFilter == UserRole.COORDINATOR) null else UserRole.COORDINATOR },
                        label = { Text("Coord", fontSize = 11.sp) }
                    )
                }
            }

            // MEMBER CARDS
            items(filteredMembers, key = { it.id }) { member ->
                val memberTasks = tasks.filter { it.teamMemberId == member.id }
                val completedTasks = memberTasks.count { it.status == TaskStatus.COMPLETED }
                val overdueTasks = memberTasks.count { SportsOpsLogic.calculateTaskHealth(it.status, it.deadline) == TaskHealth.OVERDUE }
                val completionPct = if (memberTasks.isNotEmpty()) (completedTasks * 100) / memberTasks.size else 100

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMemberForDetail = member }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(name = member.name, colorHex = member.avatarColor, size = 36)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(member.vertical, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            RoleBadge(role = member.role)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(10.dp))

                        // WORKLOAD METRICS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Active Tasks", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${memberTasks.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Completed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$completedTasks", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StatusCompleted)
                            }
                            Column {
                                Text("Overdue", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$overdueTasks", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (overdueTasks > 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Efficiency", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$completionPct%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    // MEMBER PROFILE DIALOG
    selectedMemberForDetail?.let { member ->
        val mTasks = tasks.filter { it.teamMemberId == member.id }
        val mIssues = issues.filter { it.assignedToId == member.id }

        AlertDialog(
            onDismissRequest = { selectedMemberForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(name = member.name, colorHex = member.avatarColor, size = 32)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(member.vertical, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Role:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        RoleBadge(role = member.role)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Email:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(member.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Phone:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(member.phone, fontSize = 12.sp)
                    }
                    Divider()
                    Text("Operational Portfolio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("• ${mTasks.size} tasks in assigned queue", fontSize = 12.sp)
                    Text("• ${mIssues.size} open escalation issues assigned", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { selectedMemberForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}
