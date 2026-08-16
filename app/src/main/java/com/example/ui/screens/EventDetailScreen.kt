package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
fun EventDetailScreen(
    eventId: String,
    viewModel: SportsOpsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToProposalReview: (String) -> Unit
) {
    val events by viewModel.events.collectAsState()
    val readinessRequirements by viewModel.readinessRequirements.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val issues by viewModel.issues.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val event = events.find { it.id == eventId }
    val summary = remember(eventId, readinessRequirements) {
        SportsOpsLogic.calculateEventReadiness(eventId, readinessRequirements)
    }

    var selectedTab by remember { mutableStateOf(0) } // 0 = 7-Phase Readiness, 1 = Event Tasks, 2 = Issues & Blockers
    var expandedPhase by remember { mutableStateOf<Int?>(1) }

    // Dialog state for updating requirement responsibility status
    var activeReqForUpdate by remember { mutableStateOf<EventReadinessRequirement?>(null) }
    var selectedPocState by remember { mutableStateOf(RequirementResponsibilityState.PENDING) }
    var selectedCoordState by remember { mutableStateOf(RequirementResponsibilityState.PENDING) }
    var selectedCoreState by remember { mutableStateOf(RequirementResponsibilityState.PENDING) }

    var showStageDialog by remember { mutableStateOf(false) }

    if (event == null) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Event not found")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNavigateBack) { Text("Back") }
            }
        }
        return
    }

    val linkedTasks = tasks.filter { it.eventId == event.id }
    val linkedIssues = issues.filter { it.eventId == event.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(event.id, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(event.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToProposalReview(event.id) }) {
                        Icon(Icons.Default.RateReview, contentDescription = "Proposal Rubric", tint = MaterialTheme.colorScheme.primary)
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // EVENT HEADER CARD
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SportsNavyMedium,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = event.society,
                                    color = SportsAmberLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    if (currentUser.role == UserRole.CORE || currentUser.role == UserRole.DEPUTY_CORE) {
                                        showStageDialog = true
                                    }
                                }
                            ) {
                                Text(
                                    text = "Stage: ${event.currentStage.displayName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (currentUser.role == UserRole.CORE || currentUser.role == UserRole.DEPUTY_CORE) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Stage", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (event.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // KEY ATTRIBUTES
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Event Head", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(event.eventHead, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(event.eventHeadContact, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column {
                                Text("Sports POC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(event.sportsPoc, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Coord: ${event.coordinator}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Venue & Dates", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(event.venue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("${event.startDate} to ${event.endDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressWithLabel(
                            progressPercent = summary.overallPercent,
                            color = if (summary.overallPercent >= 80) StatusCompleted else if (summary.overallPercent >= 50) SportsAmberPrimary else Color(0xFFDC2626)
                        )
                    }
                }
            }

            // TAB ROW
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("7-Phase Readiness", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Tasks (${linkedTasks.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Issues (${linkedIssues.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // TAB 0: 7-PHASE EVENT READINESS CHECKLIST
            if (selectedTab == 0) {
                if (summary.isExecutionRisk) {
                    item {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("EXECUTION RISK DETECTED", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B), fontSize = 12.sp)
                                    Text("One or more mandatory preparatory requirements are delayed or under Core rework.", fontSize = 11.sp, color = Color(0xFF7F1D1D))
                                }
                            }
                        }
                    }
                }

                items(summary.phaseBreakdown) { phase ->
                    val isExpanded = expandedPhase == phase.phaseNumber

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedPhase = if (isExpanded) null else phase.phaseNumber
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Phase ${phase.phaseNumber} — ${phase.phaseTitle}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${phase.completedRequirements}/${phase.totalRequirements} Completed (${phase.percent}%)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${phase.percent}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (phase.percent == 100) StatusCompleted else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { phase.percent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (phase.percent == 100) StatusCompleted else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // EXPANDED REQUIREMENTS MATRIX
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier.padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    phase.requirements.forEach { req ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    activeReqForUpdate = req
                                                    selectedPocState = req.pocStatus
                                                    selectedCoordState = req.coordinatorStatus
                                                    selectedCoreState = req.coreStatus
                                                }
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = req.title,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    if (SportsOpsLogic.isRequirementFullyCompleted(req)) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = StatusCompleted, modifier = Modifier.size(16.dp))
                                                    }
                                                }

                                                if (req.notes.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(req.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // 3-RESPONSIBILITY STATES: POC | Coordinator | Core
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    ResponsibilityStatusChip(label = "POC", state = req.pocStatus)
                                                    ResponsibilityStatusChip(label = "Coord", state = req.coordinatorStatus)
                                                    ResponsibilityStatusChip(label = "Core", state = req.coreStatus)
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

            // TAB 1: LINKED EVENT TASKS
            if (selectedTab == 1) {
                if (linkedTasks.isEmpty()) {
                    item {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                            Text("No tasks explicitly linked to this event yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(linkedTasks) { task ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToTaskDetail(task.id) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(task.id, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    StatusChip(status = task.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(task.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Assignee: ${task.teamMemberName} • Due: ${task.deadline}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // TAB 2: LINKED ISSUES
            if (selectedTab == 2) {
                if (linkedIssues.isEmpty()) {
                    item {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                            Text("No issues logged for this event.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(linkedIssues) { issue ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(issue.id, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                                    Surface(
                                        color = if (issue.severity == IssueSeverity.CRITICAL) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = issue.severity.displayName.uppercase(),
                                            color = if (issue.severity == IssueSeverity.CRITICAL) Color(0xFF991B1B) else Color(0xFF92400E),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(issue.problem, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Action: ${issue.actionRequired}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    // REQUIREMENT RESPONSIBILITY UPDATE DIALOG
    activeReqForUpdate?.let { req ->
        AlertDialog(
            onDismissRequest = { activeReqForUpdate = null },
            title = { Text(req.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Update responsibility sign-off status:", fontSize = 12.sp)

                    Text("POC Status", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RequirementResponsibilityState.values().forEach { st ->
                            FilterChip(
                                selected = selectedPocState == st,
                                onClick = { selectedPocState = st },
                                label = { Text(st.name, fontSize = 10.sp) }
                            )
                        }
                    }

                    Text("Coordinator Status", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RequirementResponsibilityState.values().forEach { st ->
                            FilterChip(
                                selected = selectedCoordState == st,
                                onClick = { selectedCoordState = st },
                                label = { Text(st.name, fontSize = 10.sp) }
                            )
                        }
                    }

                    if (currentUser.role == UserRole.CORE || currentUser.role == UserRole.DEPUTY_CORE) {
                        Text("Core Approval Status", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RequirementResponsibilityState.values().forEach { st ->
                                FilterChip(
                                    selected = selectedCoreState == st,
                                    onClick = { selectedCoreState = st },
                                    label = { Text(st.name, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateRequirementStatus(
                            reqId = req.id,
                            pocState = selectedPocState,
                            coordState = selectedCoordState,
                            coreState = selectedCoreState
                        )
                        activeReqForUpdate = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeReqForUpdate = null }) { Text("Cancel") }
            }
        )
    }

    // STAGE TRANSITION DIALOG
    if (showStageDialog) {
        AlertDialog(
            onDismissRequest = { showStageDialog = false },
            title = { Text("Transition Event Stage") },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(EventStage.values()) { st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateEventStage(event.id, st)
                                    showStageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = event.currentStage == st, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(st.displayName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStageDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ResponsibilityStatusChip(label: String, state: RequirementResponsibilityState) {
    val (bgColor, textColor) = when (state) {
        RequirementResponsibilityState.COMPLETED -> Color(0xFFDCFCE7) to Color(0xFF166534)
        RequirementResponsibilityState.PENDING -> Color(0xFFF1F5F9) to Color(0xFF64748B)
        RequirementResponsibilityState.REWORK -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        RequirementResponsibilityState.REJECTED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = textColor
            )
            Text(
                text = state.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = textColor
            )
        }
    }
}
