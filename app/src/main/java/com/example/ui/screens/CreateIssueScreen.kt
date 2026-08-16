package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.SportsOpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIssueScreen(
    viewModel: SportsOpsViewModel,
    onNavigateBack: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val teamMembers by viewModel.teamMembers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var problem by remember { mutableStateOf("") }
    var actionRequired by remember { mutableStateOf("") }
    var actionPlan by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(IssueSeverity.HIGH) }
    var selectedTargetUserId by remember { mutableStateOf(teamMembers.firstOrNull { it.role == UserRole.CORE }?.id ?: teamMembers.first().id) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }

    var expandedSeverity by remember { mutableStateOf(false) }
    var expandedAssignee by remember { mutableStateOf(false) }
    var expandedEvent by remember { mutableStateOf(false) }

    val selectedTargetUser = teamMembers.find { it.id == selectedTargetUserId }
    val selectedEvent = events.find { it.id == selectedEventId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Operational Issue", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Issue Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = problem,
                        onValueChange = { problem = it },
                        label = { Text("Problem Description *") },
                        placeholder = { Text("Clearly articulate the operational blocker or failure...") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = actionRequired,
                        onValueChange = { actionRequired = it },
                        label = { Text("Action Required *") },
                        placeholder = { Text("What specific action or approval is needed?") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = actionPlan,
                        onValueChange = { actionPlan = it },
                        label = { Text("Proposed Mitigation / Action Plan") },
                        placeholder = { Text("Steps to resolve or work around the issue...") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Severity Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedSeverity,
                        onExpandedChange = { expandedSeverity = !expandedSeverity }
                    ) {
                        OutlinedTextField(
                            value = severity.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Severity *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSeverity) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSeverity,
                            onDismissRequest = { expandedSeverity = false }
                        ) {
                            IssueSeverity.values().forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.displayName) },
                                    onClick = { severity = s; expandedSeverity = false }
                                )
                            }
                        }
                    }

                    // Assignee / Escalation Target Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedAssignee,
                        onExpandedChange = { expandedAssignee = !expandedAssignee }
                    ) {
                        OutlinedTextField(
                            value = selectedTargetUser?.let { "${it.name} (${it.role.displayName})" } ?: "Select Assignee",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assign / Escalate To *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAssignee) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAssignee,
                            onDismissRequest = { expandedAssignee = false }
                        ) {
                            teamMembers.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.name} • ${m.role.displayName} (${m.vertical})") },
                                    onClick = {
                                        selectedTargetUserId = m.id
                                        expandedAssignee = false
                                    }
                                )
                            }
                        }
                    }

                    // Event Dropdown (Optional)
                    ExposedDropdownMenuBox(
                        expanded = expandedEvent,
                        onExpandedChange = { expandedEvent = !expandedEvent }
                    ) {
                        OutlinedTextField(
                            value = selectedEvent?.let { "${it.id} - ${it.name}" } ?: "None (General Department)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Related Event (Optional)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEvent) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEvent,
                            onDismissRequest = { expandedEvent = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (General Department)") },
                                onClick = { selectedEventId = null; expandedEvent = false }
                            )
                            events.forEach { ev ->
                                DropdownMenuItem(
                                    text = { Text("${ev.id}: ${ev.name}") },
                                    onClick = { selectedEventId = ev.id; expandedEvent = false }
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (problem.isNotBlank() && actionRequired.isNotBlank() && selectedTargetUser != null) {
                        val newIssue = IssueItem(
                            id = "ISSUE-${System.currentTimeMillis() % 1000}",
                            dateRaised = "2026-08-16",
                            vertical = selectedTargetUser.vertical,
                            eventId = selectedEvent?.id,
                            eventName = selectedEvent?.name,
                            problem = problem.trim(),
                            actionRequired = actionRequired.trim(),
                            actionPlan = actionPlan.trim(),
                            raisedById = currentUser.id,
                            raisedByName = currentUser.name,
                            assignedToId = selectedTargetUser.id,
                            assignedToName = selectedTargetUser.name,
                            severity = severity,
                            status = IssueStatus.OPEN,
                            escalationLevel = when (selectedTargetUser.role) {
                                UserRole.CORE -> EscalationLevel.L4_CORE
                                UserRole.DEPUTY_CORE -> EscalationLevel.L3_DEPUTY_CORE
                                UserRole.SUPER_COORDINATOR -> EscalationLevel.L2_SUPER_COORDINATOR
                                else -> EscalationLevel.L1_VOLUNTEER_COORDINATOR
                            }
                        )
                        viewModel.createIssue(newIssue)
                        onNavigateBack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SportsCrimsonDanger),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Report Issue to Register", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
