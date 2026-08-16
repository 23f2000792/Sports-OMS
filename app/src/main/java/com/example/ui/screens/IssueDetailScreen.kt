package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SportsOpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    issueId: String,
    viewModel: SportsOpsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val issues by viewModel.issues.collectAsState()
    val teamMembers by viewModel.teamMembers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val issue = issues.find { it.id == issueId }

    var showEscalateDialog by remember { mutableStateOf(false) }
    var escalationReasonInput by remember { mutableStateOf("") }
    var selectedTargetUserId by remember { mutableStateOf("") }

    var showResolveDialog by remember { mutableStateOf(false) }
    var resolutionInput by remember { mutableStateOf("") }

    var showEvidenceDialog by remember { mutableStateOf(false) }
    var evidenceTitleInput by remember { mutableStateOf("") }
    var evidenceUrlInput by remember { mutableStateOf("") }

    if (issue == null) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Issue not found")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNavigateBack) { Text("Back") }
            }
        }
        return
    }

    val (sevBg, sevText) = when (issue.severity) {
        IssueSeverity.CRITICAL -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        IssueSeverity.HIGH -> Color(0xFFFFEDD5) to Color(0xFFC2410C)
        IssueSeverity.MEDIUM -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        IssueSeverity.LOW -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(issue.id, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Escalation Register", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // HEADER ISSUE CARD
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
                            Surface(color = sevBg, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "${issue.severity.displayName.uppercase()} SEVERITY",
                                    color = sevText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                color = SportsNavyDark,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "LEVEL: ${issue.escalationLevel.name}",
                                    color = SportsAmberLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = issue.problem,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Action Required", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(issue.actionRequired, fontSize = 13.sp)
                            }
                        }

                        if (issue.actionPlan.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Action Plan: ${issue.actionPlan}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Raised By", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(issue.raisedByName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text("Assigned To", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(issue.assignedToName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(issue.status.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (issue.status == IssueStatus.RESOLVED) StatusCompleted else Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }

            // ACTIONS ROW (Escalate / Resolve)
            if (issue.status != IssueStatus.RESOLVED && issue.status != IssueStatus.CLOSED) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showEscalateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SportsCrimsonDanger),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Escalate Issue", fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showResolveDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Resolved", fontSize = 13.sp)
                        }
                    }
                }
            }

            // RESOLUTION DETAILS (If resolved)
            if (issue.resolution != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusCompleted, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Resolution Summary", fontWeight = FontWeight.Bold, color = Color(0xFF166534), fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(issue.resolution, fontSize = 12.sp, color = Color(0xFF14532D))
                        }
                    }
                }
            }

            // EVIDENCE & ATTACHMENTS
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
                            Text("Evidence & Documents (${issue.evidenceList.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showEvidenceDialog = true }) {
                                Icon(Icons.Default.AddLink, contentDescription = "Add Evidence", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        if (issue.evidenceList.isEmpty()) {
                            Text("No evidence links attached yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                issue.evidenceList.forEach { ev ->
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
                                                    // Ignored in container
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(ev.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            }
                                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ESCALATION TIMELINE
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Escalation History", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        if (issue.escalationHistory.isEmpty()) {
                            Text("No escalation events logged.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            issue.escalationHistory.reversed().forEach { act ->
                                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SportsCrimsonDanger)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(act.reason, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("From ${act.fromUser} → ${act.toUser} • ${act.timestamp}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ESCALATE DIALOG
    if (showEscalateDialog) {
        AlertDialog(
            onDismissRequest = { showEscalateDialog = false },
            title = { Text("Escalate Operational Issue") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select target authority (Core / Deputy Core):", fontSize = 12.sp)
                    teamMembers.filter { it.role == UserRole.CORE || it.role == UserRole.DEPUTY_CORE || it.role == UserRole.SUPER_COORDINATOR }.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTargetUserId = m.id }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedTargetUserId == m.id, onClick = { selectedTargetUserId = m.id })
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(m.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("${m.role.displayName} • ${m.vertical}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = escalationReasonInput,
                        onValueChange = { escalationReasonInput = it },
                        label = { Text("Reason for Escalation") },
                        placeholder = { Text("e.g. Requires approval for budget exception...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedTargetUserId.isNotBlank()) {
                            viewModel.escalateIssue(issue.id, selectedTargetUserId, escalationReasonInput)
                            showEscalateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SportsCrimsonDanger)
                ) {
                    Text("Escalate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEscalateDialog = false }) { Text("Cancel") }
            }
        )
    }

    // RESOLVE DIALOG
    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = { Text("Resolve Operational Issue") },
            text = {
                Column {
                    Text("Describe how this issue was resolved:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resolutionInput,
                        onValueChange = { resolutionInput = it },
                        label = { Text("Resolution Details") },
                        placeholder = { Text("e.g. Secured backup stadium floodlights from Admin") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resolutionInput.isNotBlank()) {
                            viewModel.updateIssueStatus(issue.id, IssueStatus.RESOLVED, resolutionInput.trim())
                            showResolveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted)
                ) {
                    Text("Mark Resolved")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ATTACH EVIDENCE DIALOG
    if (showEvidenceDialog) {
        AlertDialog(
            onDismissRequest = { showEvidenceDialog = false },
            title = { Text("Attach Evidence Link") },
            text = {
                Column {
                    OutlinedTextField(
                        value = evidenceTitleInput,
                        onValueChange = { evidenceTitleInput = it },
                        label = { Text("Title") },
                        placeholder = { Text("e.g. Permission Letter PDF") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = evidenceUrlInput,
                        onValueChange = { evidenceUrlInput = it },
                        label = { Text("Resource URL") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (evidenceTitleInput.isNotBlank() && evidenceUrlInput.isNotBlank()) {
                            viewModel.addEvidenceToIssue(
                                issue.id,
                                EvidenceAttachment(
                                    id = "EV-ISSUE-${System.currentTimeMillis() % 10000}",
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
}
