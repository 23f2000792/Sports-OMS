package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

@Composable
fun HomeScreen(
    viewModel: SportsOpsViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToIssues: () -> Unit,
    onNavigateToIssueDetail: (String) -> Unit,
    onNavigateToApprovals: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAiOps: () -> Unit,
    onOpenRoleSwitcher: () -> Unit,
    onOpenGlobalSearch: () -> Unit,
    onOpenCloudSync: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val syncSummary by viewModel.cloudSyncSummary.collectAsState()
    val isSyncing by viewModel.isSyncingCloud.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val events by viewModel.events.collectAsState()
    val issues by viewModel.issues.collectAsState()
    val calendarItems by viewModel.calendarItems.collectAsState()
    val approvals by viewModel.approvals.collectAsState()
    val attentionAlerts by viewModel.attentionRequiredAlerts.collectAsState()
    val readinessMap by viewModel.eventReadinessMap.collectAsState()

    val isCoreOrDeputy = currentUser.role == UserRole.CORE || currentUser.role == UserRole.DEPUTY_CORE

    // Task counts
    val myTasks = tasks.filter { it.teamMemberId == currentUser.id }
    val openTasks = if (isCoreOrDeputy) tasks.filter { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED } else myTasks.filter { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
    val overdueTasks = if (isCoreOrDeputy) tasks.filter { SportsOpsLogic.calculateTaskHealth(it.status, it.deadline) == TaskHealth.OVERDUE } else myTasks.filter { SportsOpsLogic.calculateTaskHealth(it.status, it.deadline) == TaskHealth.OVERDUE }
    val dueSoonTasks = if (isCoreOrDeputy) tasks.filter { SportsOpsLogic.calculateTaskHealth(it.status, it.deadline) == TaskHealth.AT_RISK } else myTasks.filter { SportsOpsLogic.calculateTaskHealth(it.status, it.deadline) == TaskHealth.AT_RISK }
    val blockedTasks = if (isCoreOrDeputy) tasks.filter { it.status == TaskStatus.BLOCKED || it.blocker != null } else myTasks.filter { it.status == TaskStatus.BLOCKED || it.blocker != null }
    val criticalIssues = issues.filter { it.severity == IssueSeverity.CRITICAL && it.status != IssueStatus.RESOLVED && it.status != IssueStatus.CLOSED }
    val pendingApprovals = approvals.filter { it.status == CoreApprovalStatus.PENDING }

    // Prioritized Today's Focus Action List
    val focusTasks = remember(tasks, currentUser) {
        val targetList = if (isCoreOrDeputy) tasks else myTasks
        targetList.sortedWith(
            compareBy(
                {
                    val health = SportsOpsLogic.calculateTaskHealth(it.status, it.deadline)
                    when {
                        it.priority == Priority.CRITICAL && health == TaskHealth.OVERDUE -> 1
                        it.priority == Priority.CRITICAL && health == TaskHealth.AT_RISK -> 2
                        it.status == TaskStatus.BLOCKED -> 3
                        health == TaskHealth.OVERDUE -> 4
                        health == TaskHealth.AT_RISK -> 5
                        it.priority == Priority.HIGH -> 6
                        else -> 7
                    }
                },
                { it.deadline }
            )
        ).take(6)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // TOP HEADER / GREETING SECTION
        item {
            Card(
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = SportsNavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SPORTS OPS",
                                    color = SportsAmberLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SportsEmeraldSuccess)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE",
                                    color = SportsEmeraldSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentUser.name,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenCloudSync,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SportsNavyMedium)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = "Cloud Sync",
                                        tint = if (isSyncing) SportsAmberLight else if (syncSummary.status == com.example.data.firebase.CloudConnectionStatus.ONLINE) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onOpenGlobalSearch,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SportsNavyMedium)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onNavigateToAiOps,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SportsNavyMedium)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Ops", tint = SportsAmberLight, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.clickable { onOpenRoleSwitcher() }) {
                                UserAvatar(name = currentUser.name, colorHex = currentUser.avatarColor, size = 38)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RoleBadge(role = currentUser.role)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentUser.vertical,
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }

                        // Cloud status indicator pill
                        Surface(
                            color = SportsNavyMedium,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { onOpenCloudSync() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (syncSummary.status == com.example.data.firebase.CloudConnectionStatus.ONLINE) Color(0xFF4ADE80)
                                            else if (isSyncing) SportsAmberLight
                                            else Color(0xFF94A3B8)
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSyncing) "Syncing..." else "Firestore Cloud",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // ATTENTION REQUIRED BANNER (Generated Smart Alerts)
        if (attentionAlerts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFECACA))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ATTENTION REQUIRED (${attentionAlerts.size})",
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            attentionAlerts.take(3).forEach { alert ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("•", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                                    Text(
                                        text = alert,
                                        color = Color(0xFF7F1D1D),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // OPERATIONAL KPI CARDS
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = if (isCoreOrDeputy) "DEPARTMENT INTELLIGENCE" else "MY WORKLOAD OVERVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    KPIStatCard(
                        title = if (isCoreOrDeputy) "Active Tasks" else "My Open Tasks",
                        value = "${openTasks.size}",
                        icon = Icons.Default.Assignment,
                        accentColor = Color(0xFF2563EB),
                        subtext = "${overdueTasks.size} overdue",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTasks
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    KPIStatCard(
                        title = "Overdue",
                        value = "${overdueTasks.size}",
                        icon = Icons.Default.Error,
                        accentColor = Color(0xFFEF4444),
                        subtext = if (overdueTasks.isNotEmpty()) "Action required" else "Clean record",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTasks
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    KPIStatCard(
                        title = "Due in <= 48h",
                        value = "${dueSoonTasks.size}",
                        icon = Icons.Default.Schedule,
                        accentColor = Color(0xFFF59E0B),
                        subtext = "Critical milestones",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTasks
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    KPIStatCard(
                        title = if (isCoreOrDeputy) "Critical Issues" else "Blocked Tasks",
                        value = if (isCoreOrDeputy) "${criticalIssues.size}" else "${blockedTasks.size}",
                        icon = Icons.Default.Block,
                        accentColor = Color(0xFFDC2626),
                        subtext = if (isCoreOrDeputy) "Escalation register" else "Dependencies",
                        modifier = Modifier.weight(1f),
                        onClick = if (isCoreOrDeputy) onNavigateToIssues else onNavigateToTasks
                    )
                }

                if (isCoreOrDeputy && pendingApprovals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = SportsNavyDark,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToApprovals() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SportsAmberPrimary)
                                ) {
                                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Approvals Inbox",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${pendingApprovals.size} items pending Core sign-off",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }

        // TODAY'S PRIORITIZED ACTION FOCUS LIST
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S ACTION FOCUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "View All Tasks",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onNavigateToTasks() }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (focusTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SportsEmeraldSuccess, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All clear for today!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("No pending priority tasks requiring immediate action.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(focusTasks) { task ->
                val health = SportsOpsLogic.calculateTaskHealth(task.status, task.deadline)
                val daysRemaining = SportsOpsLogic.calculateDaysRemaining(task.deadline)

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clickable { onNavigateToTaskDetail(task.id) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = task.id,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                PriorityChip(priority = task.priority)
                            }
                            HealthBadge(health = health)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )

                        if (task.blocker != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Blocker: ${task.blocker}",
                                        color = Color(0xFF991B1B),
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = task.teamMemberName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = when {
                                    daysRemaining == null -> "No deadline"
                                    daysRemaining < 0 -> "${Math.abs(daysRemaining)}d overdue"
                                    daysRemaining == 0L -> "Due Today"
                                    daysRemaining == 1L -> "Due Tomorrow"
                                    else -> "Due in ${daysRemaining}d"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (daysRemaining != null && daysRemaining <= 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressWithLabel(
                            progressPercent = task.progressPercent,
                            color = if (task.progressPercent == 100) StatusCompleted else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // EVENT READINESS PREVIEW CARDS
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE EVENTS & READINESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "View All Events",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onNavigateToEvents() }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events.take(5)) { ev ->
                    val readiness = readinessMap[ev.id]
                    val pct = readiness?.overallPercent ?: ev.readinessPercent
                    val isRisk = readiness?.isExecutionRisk == true || pct < 50

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(220.dp)
                            .clickable { onNavigateToEventDetail(ev.id) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ev.id,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (isRisk) {
                                    Surface(
                                        color = Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "AT RISK",
                                            color = Color(0xFFDC2626),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = ev.currentStage.displayName.uppercase(),
                                            color = Color(0xFF166534),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ev.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ev.society,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressWithLabel(
                                progressPercent = pct,
                                color = if (pct >= 80) StatusCompleted else if (pct >= 50) SportsAmberPrimary else Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }

        // UPCOMING MASTER CALENDAR MILESTONES
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UPCOMING SCHEDULE & DEADLINES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Full Calendar",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { onNavigateToCalendar() }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(calendarItems.take(3)) { cal ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateToCalendar() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SportsNavyMedium)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = cal.date.substringAfterLast("-"),
                                color = SportsAmberLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "AUG",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cal.time,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("•", color = Color.Gray, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cal.category,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = cal.activity,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = cal.eventOrArea,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    if (cal.meetingUrl != null) {
                        Icon(Icons.Default.VideoCall, contentDescription = "Meeting", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
