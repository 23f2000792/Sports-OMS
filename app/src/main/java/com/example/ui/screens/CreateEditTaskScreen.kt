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
import com.example.viewmodel.SportsOpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTaskScreen(
    viewModel: SportsOpsViewModel,
    onNavigateBack: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val teamMembers by viewModel.teamMembers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var vertical by remember { mutableStateOf("Event Operations") }
    var selectedMemberId by remember { mutableStateOf(teamMembers.firstOrNull()?.id ?: "") }
    var taskType by remember { mutableStateOf("Documentation") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var deadline by remember { mutableStateOf("2026-08-20") }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var remarks by remember { mutableStateOf("") }

    var expandedVertical by remember { mutableStateOf(false) }
    var expandedPriority by remember { mutableStateOf(false) }
    var expandedAssignee by remember { mutableStateOf(false) }
    var expandedEvent by remember { mutableStateOf(false) }

    val selectedMember = teamMembers.find { it.id == selectedMemberId }
    val selectedEvent = events.find { it.id == selectedEventId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Operational Task", fontWeight = FontWeight.Bold) },
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
                    Text("Task Specifications", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title *") },
                        placeholder = { Text("e.g. Finalize Football Referee Contracts") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Scope / Instructions") },
                        placeholder = { Text("Explain deliverables, requirements and guidelines...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Vertical Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedVertical,
                        onExpandedChange = { expandedVertical = !expandedVertical }
                    ) {
                        OutlinedTextField(
                            value = vertical,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vertical") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVertical) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedVertical,
                            onDismissRequest = { expandedVertical = false }
                        ) {
                            listOf("Event Operations", "Technology & Systems", "Stakeholder Coordination").forEach { v ->
                                DropdownMenuItem(
                                    text = { Text(v) },
                                    onClick = { vertical = v; expandedVertical = false }
                                )
                            }
                        }
                    }

                    // Assignee Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedAssignee,
                        onExpandedChange = { expandedAssignee = !expandedAssignee }
                    ) {
                        OutlinedTextField(
                            value = selectedMember?.let { "${it.name} (${it.role.displayName})" } ?: "Select Assignee",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assignee *") },
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
                                        selectedMemberId = m.id
                                        vertical = m.vertical
                                        expandedAssignee = false
                                    }
                                )
                            }
                        }
                    }

                    // Priority Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedPriority,
                        onExpandedChange = { expandedPriority = !expandedPriority }
                    ) {
                        OutlinedTextField(
                            value = priority.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPriority,
                            onDismissRequest = { expandedPriority = false }
                        ) {
                            Priority.values().forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.displayName) },
                                    onClick = { priority = p; expandedPriority = false }
                                )
                            }
                        }
                    }

                    // Linked Event Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedEvent,
                        onExpandedChange = { expandedEvent = !expandedEvent }
                    ) {
                        OutlinedTextField(
                            value = selectedEvent?.let { "${it.id} - ${it.name}" } ?: "None (General Operations)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Associated Event (Optional)") },
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
                                text = { Text("None (General Operations)") },
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

                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline (YYYY-MM-DD) *") },
                        placeholder = { Text("2026-08-25") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Initial Remarks / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = {
                    if (title.isNotBlank() && selectedMember != null) {
                        val newTask = TaskItem(
                            id = "NEW-${System.currentTimeMillis() % 1000}",
                            title = title.trim(),
                            description = description.trim(),
                            vertical = vertical,
                            teamMemberId = selectedMember.id,
                            teamMemberName = selectedMember.name,
                            taskType = taskType,
                            priority = priority,
                            assignedById = currentUser.id,
                            assignedByName = currentUser.name,
                            dateAssigned = "2026-08-16",
                            deadline = deadline.trim(),
                            status = TaskStatus.NOT_STARTED,
                            progressPercent = 0,
                            remarks = remarks.trim(),
                            eventId = selectedEvent?.id,
                            eventName = selectedEvent?.name
                        )
                        viewModel.createOrUpdateTask(newTask)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
