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
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.SportsOpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuesScreen(
    viewModel: SportsOpsViewModel,
    onNavigateToIssueDetail: (String) -> Unit,
    onNavigateToCreateIssue: () -> Unit
) {
    val issues by viewModel.issues.collectAsState()
    val searchQuery by viewModel.issueSearchQuery.collectAsState()
    val severityFilter by viewModel.issueSeverityFilter.collectAsState()
    val statusFilter by viewModel.issueStatusFilter.collectAsState()

    val filteredIssues = issues.filter { issue ->
        val matchesQuery = searchQuery.isBlank() ||
                issue.id.contains(searchQuery, ignoreCase = true) ||
                issue.problem.contains(searchQuery, ignoreCase = true) ||
                issue.assignedToName.contains(searchQuery, ignoreCase = true) ||
                (issue.eventName?.contains(searchQuery, ignoreCase = true) == true)
        val matchesSeverity = severityFilter == null || issue.severity == severityFilter
        val matchesStatus = statusFilter == null || issue.status == statusFilter
        matchesQuery && matchesSeverity && matchesStatus
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Issue Escalation Register", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${issues.size} logged operational issue(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateIssue,
                containerColor = SportsCrimsonDanger,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Report Issue")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setIssueSearchQuery(it) },
                placeholder = { Text("Search issue ID, problem, assignee, event...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setIssueSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // SEVERITY & STATUS FILTER CHIPS
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = severityFilter == IssueSeverity.CRITICAL,
                        onClick = { viewModel.setIssueSeverityFilter(if (severityFilter == IssueSeverity.CRITICAL) null else IssueSeverity.CRITICAL) },
                        label = { Text("Critical (${issues.count { it.severity == IssueSeverity.CRITICAL }})", fontSize = 12.sp) },
                        leadingIcon = {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFDC2626)))
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = severityFilter == IssueSeverity.HIGH,
                        onClick = { viewModel.setIssueSeverityFilter(if (severityFilter == IssueSeverity.HIGH) null else IssueSeverity.HIGH) },
                        label = { Text("High", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == IssueStatus.UNDER_REVIEW,
                        onClick = { viewModel.setIssueStatusFilter(if (statusFilter == IssueStatus.UNDER_REVIEW) null else IssueStatus.UNDER_REVIEW) },
                        label = { Text("Under Review", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == IssueStatus.OPEN,
                        onClick = { viewModel.setIssueStatusFilter(if (statusFilter == IssueStatus.OPEN) null else IssueStatus.OPEN) },
                        label = { Text("Open", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == IssueStatus.RESOLVED,
                        onClick = { viewModel.setIssueStatusFilter(if (statusFilter == IssueStatus.RESOLVED) null else IssueStatus.RESOLVED) },
                        label = { Text("Resolved", fontSize = 12.sp) }
                    )
                }
            }

            // ISSUES LIST
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredIssues, key = { it.id }) { issue ->
                    val (sevBg, sevText) = when (issue.severity) {
                        IssueSeverity.CRITICAL -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
                        IssueSeverity.HIGH -> Color(0xFFFFEDD5) to Color(0xFFC2410C)
                        IssueSeverity.MEDIUM -> Color(0xFFFEF3C7) to Color(0xFF92400E)
                        IssueSeverity.LOW -> Color(0xFFF1F5F9) to Color(0xFF475569)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToIssueDetail(issue.id) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(issue.id, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = sevBg, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            text = issue.severity.displayName.uppercase(),
                                            color = sevText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Surface(
                                    color = if (issue.status == IssueStatus.RESOLVED) Color(0xFFDCFCE7) else Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = issue.status.displayName,
                                        color = if (issue.status == IssueStatus.RESOLVED) Color(0xFF166534) else Color(0xFF1D4ED8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = issue.problem,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Action: ${issue.actionRequired}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(name = issue.assignedToName, size = 18)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = issue.assignedToName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    color = SportsNavyDark,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = issue.escalationLevel.name,
                                        color = SportsAmberLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
