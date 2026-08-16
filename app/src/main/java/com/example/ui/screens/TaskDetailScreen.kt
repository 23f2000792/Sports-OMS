package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
fun TaskDetailScreen(
    taskId: String,
    viewModel: SportsOpsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val teamMembers by viewModel.teamMembers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val task = tasks.find { it.id == taskId }

    var showBlockerDialog by remember { mutableStateOf(false) }
    var blockerInput by remember { mutableStateOf("") }

    var showEvidenceDialog by remember { mutableStateOf(false) }
    var evidenceTitleInput by remember { mutableStateOf("") }
    var evidenceUrlInput by remember { mutableStateOf("") }

    var showReassignDialog by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf("") }

    var showRemarkDialog by remember { mutableStateOf(false) }
    var remarkInput by remember { mutableStateOf("") }

    if (task == null) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Task not found")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNavigateBack) { Text("Back") }
            }
        }
        return
    }

    val health = SportsOpsLogic.calculateTaskHealth(task.status, task.deadline)
    val daysRemaining = SportsOpsLogic.calculateDaysRemaining(task.deadline)
    val isAuthorizedToReassign = currentUser.role == UserRole.CORE || currentUser.role == UserRole.DEPUTY_CORE || currentUser.role == UserRole.SUPER_COORDINATOR

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(task.id, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(task.vertical, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAuthorizedToReassign) {
                        IconButton(onClick = { showReassignDialog = true }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Reassign")
                        }
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
            // HEADER CARD
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
                            PriorityChip(priority = task.priority)
                            HealthBadge(health = health)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // METRICS GRID
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Assignee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(name = task.teamMemberName, size = 18)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(task.teamMemberName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                            Column {
                                Text("Assigned By", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(task.assignedByName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Deadline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = task.deadline,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (daysRemaining != null && daysRemaining <= 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (task.eventName != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Event: ${task.eventName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // INTERACTIVE PROGRESS & STATUS CONTROL
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
                            Text("Task Progress & Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            StatusChip(status = task.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Completion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${task.progressPercent}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Slider(
                            value = task.progressPercent.toFloat(),
                            onValueChange = { viewModel.updateTaskProgress(task.id, it.toInt()) },
                            valueRange = 0f..100f,
                            steps = 19,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick % Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(0, 25, 50, 75, 100).forEach { pct ->
                                OutlinedButton(
                                    onClick = { viewModel.updateTaskProgress(task.id, pct) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    colors = if (task.progressPercent == pct) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("$pct%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Change Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (task.status != TaskStatus.IN_PROGRESS && task.status != TaskStatus.COMPLETED) {
                                Button(
                                    onClick = { viewModel.updateTaskStatus(task.id, TaskStatus.IN_PROGRESS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("In Progress", fontSize = 12.sp)
                                }
                            }
                            if (task.status != TaskStatus.COMPLETED) {
                                Button(
                                    onClick = { viewModel.updateTaskStatus(task.id, TaskStatus.COMPLETED, progress = 100) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Complete", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // BLOCKER SECTION
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = if (task.blocker != null) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Operational Blocker", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }

                            if (task.blocker == null) {
                                TextButton(onClick = { showBlockerDialog = true }) {
                                    Text("Flag Blocker", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                TextButton(onClick = { viewModel.clearTaskBlocker(task.id) }) {
                                    Text("Resolve Blocker", color = StatusCompleted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (task.blocker != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = task.blocker,
                                    color = Color(0xFF991B1B),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "No active blockers reported on this task.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // EVIDENCE & DOCUMENTATION SECTION
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
                            Text("Evidence & Deliverables (${task.evidenceList.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showEvidenceDialog = true }) {
                                Icon(Icons.Default.AddLink, contentDescription = "Add Link", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        if (task.evidenceList.isEmpty()) {
                            Text(
                                text = "No evidence links attached yet. Tap '+' to link Google Drive, sheet, or report.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                task.evidenceList.forEach { ev ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                try {
                                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ev.url))
                                                    context.startActivity(browserIntent)
                                                } catch (e: Exception) {
                                                    // ignored in sandbox
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(ev.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                                                    Text("Uploaded by ${ev.uploadedBy} • ${ev.uploadedAt}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            Icon(Icons.Default.OpenInNew, contentDescription = "Open", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DEPENDENCIES SECTION
            if (task.dependencies.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Mandatory Dependencies", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            task.dependencies.forEach { depId ->
                                val depTask = tasks.find { it.id == depId }
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToTaskDetail(depId) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(depId, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            Text(depTask?.title ?: "Dependent task", fontSize = 12.sp)
                                        }
                                        if (depTask != null) {
                                            StatusChip(status = depTask.status)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ACTIVITY & AUDIT TIMELINE
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Task Activity Timeline", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        task.activityHistory.reversed().forEach { act ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(act.action, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("By ${act.user} • ${act.timestamp}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // FLAG BLOCKER DIALOG
    if (showBlockerDialog) {
        AlertDialog(
            onDismissRequest = { showBlockerDialog = false },
            title = { Text("Flag Operational Blocker") },
            text = {
                Column {
                    Text("Describe what is preventing progress on this task:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = blockerInput,
                        onValueChange = { blockerInput = it },
                        placeholder = { Text("e.g. Awaiting venue keys from security") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (blockerInput.isNotBlank()) {
                            viewModel.addTaskBlocker(task.id, blockerInput)
                            showBlockerDialog = false
                            blockerInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Flag Blocker")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockerDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ADD EVIDENCE DIALOG
    if (showEvidenceDialog) {
        AlertDialog(
            onDismissRequest = { showEvidenceDialog = false },
            title = { Text("Attach Evidence Deliverable") },
            text = {
                Column {
                    OutlinedTextField(
                        value = evidenceTitleInput,
                        onValueChange = { evidenceTitleInput = it },
                        label = { Text("Title") },
                        placeholder = { Text("e.g. Final Schedule Sheet") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = evidenceUrlInput,
                        onValueChange = { evidenceUrlInput = it },
                        label = { Text("URL / Resource Link") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (evidenceTitleInput.isNotBlank() && evidenceUrlInput.isNotBlank()) {
                            viewModel.addEvidenceToTask(
                                task.id,
                                EvidenceAttachment(
                                    id = "EV-${System.currentTimeMillis() % 10000}",
                                    title = evidenceTitleInput.trim(),
                                    url = evidenceUrlInput.trim(),
                                    uploadedBy = currentUser.name,
                                    uploadedAt = "2026-08-16 12:00"
                                )
                            )
                            showEvidenceDialog = false
                            evidenceTitleInput = ""
                            evidenceUrlInput = ""
                        }
                    }
                ) {
                    Text("Attach")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEvidenceDialog = false }) { Text("Cancel") }
            }
        )
    }

    // REASSIGN DIALOG
    if (showReassignDialog) {
        AlertDialog(
            onDismissRequest = { showReassignDialog = false },
            title = { Text("Reassign Task") },
            text = {
                Column {
                    Text("Select team member to take ownership:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    teamMembers.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMemberId = member.id }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMemberId == member.id,
                                onClick = { selectedMemberId = member.id }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(member.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${member.role.displayName} • ${member.vertical}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedMemberId.isNotBlank()) {
                            viewModel.reassignTask(task.id, selectedMemberId)
                            showReassignDialog = false
                        }
                    }
                ) {
                    Text("Reassign")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReassignDialog = false }) { Text("Cancel") }
            }
        )
    }
}
