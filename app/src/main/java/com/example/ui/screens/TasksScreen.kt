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
import com.example.viewmodel.TaskSortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: SportsOpsViewModel,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToCreateTask: () -> Unit
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val searchQuery by viewModel.taskSearchQuery.collectAsState()
    val verticalFilter by viewModel.taskVerticalFilter.collectAsState()
    val priorityFilter by viewModel.taskPriorityFilter.collectAsState()
    val healthFilter by viewModel.taskHealthFilter.collectAsState()
    val statusFilter by viewModel.taskStatusFilter.collectAsState()
    val sortOption by viewModel.taskSortOption.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val hasActiveFilters = verticalFilter != null || priorityFilter != null || healthFilter != null || statusFilter != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Master Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tasks.size} operational task(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Deadline") },
                            onClick = { viewModel.setTaskSortOption(TaskSortOption.DEADLINE); showSortMenu = false },
                            leadingIcon = { if (sortOption == TaskSortOption.DEADLINE) Icon(Icons.Default.Check, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Priority") },
                            onClick = { viewModel.setTaskSortOption(TaskSortOption.PRIORITY); showSortMenu = false },
                            leadingIcon = { if (sortOption == TaskSortOption.PRIORITY) Icon(Icons.Default.Check, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Health") },
                            onClick = { viewModel.setTaskSortOption(TaskSortOption.HEALTH); showSortMenu = false },
                            leadingIcon = { if (sortOption == TaskSortOption.HEALTH) Icon(Icons.Default.Check, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Recently Updated") },
                            onClick = { viewModel.setTaskSortOption(TaskSortOption.RECENTLY_UPDATED); showSortMenu = false },
                            leadingIcon = { if (sortOption == TaskSortOption.RECENTLY_UPDATED) Icon(Icons.Default.Check, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Progress") },
                            onClick = { viewModel.setTaskSortOption(TaskSortOption.PROGRESS); showSortMenu = false },
                            leadingIcon = { if (sortOption == TaskSortOption.PROGRESS) Icon(Icons.Default.Check, null) }
                        )
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (hasActiveFilters) {
                                    Badge { Text("!") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (currentUser.role != UserRole.VOLUNTEER) {
                FloatingActionButton(
                    onClick = onNavigateToCreateTask,
                    containerColor = SportsNavyDark,
                    contentColor = SportsAmberLight,
                    modifier = Modifier.padding(bottom = 60.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Task")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // SEARCH FIELD
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setTaskSearchQuery(it) },
                placeholder = { Text("Search task ID, title, assignee, vertical...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setTaskSearchQuery("") }) {
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

            // QUICK FILTER CHIPS ROW
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = healthFilter == TaskHealth.OVERDUE,
                        onClick = {
                            viewModel.setTaskHealthFilter(if (healthFilter == TaskHealth.OVERDUE) null else TaskHealth.OVERDUE)
                        },
                        label = { Text("Overdue", fontSize = 12.sp) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626))
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = healthFilter == TaskHealth.AT_RISK,
                        onClick = {
                            viewModel.setTaskHealthFilter(if (healthFilter == TaskHealth.AT_RISK) null else TaskHealth.AT_RISK)
                        },
                        label = { Text("At Risk (<=48h)", fontSize = 12.sp) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SportsAmberPrimary)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == TaskStatus.BLOCKED,
                        onClick = {
                            viewModel.setTaskStatusFilter(if (statusFilter == TaskStatus.BLOCKED) null else TaskStatus.BLOCKED)
                        },
                        label = { Text("Blocked", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFDC2626))
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = priorityFilter == Priority.CRITICAL,
                        onClick = {
                            viewModel.setTaskPriorityFilter(if (priorityFilter == Priority.CRITICAL) null else Priority.CRITICAL)
                        },
                        label = { Text("Critical", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = verticalFilter == "Event Operations",
                        onClick = {
                            viewModel.setTaskVerticalFilter(if (verticalFilter == "Event Operations") null else "Event Operations")
                        },
                        label = { Text("Event Ops", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = verticalFilter == "Technology & Systems",
                        onClick = {
                            viewModel.setTaskVerticalFilter(if (verticalFilter == "Technology & Systems") null else "Technology & Systems")
                        },
                        label = { Text("Tech & Systems", fontSize = 12.sp) }
                    )
                }
                if (hasActiveFilters) {
                    item {
                        TextButton(onClick = { viewModel.clearTaskFilters() }) {
                            Text("Reset", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // TASK ITEMS LIST
            if (tasks.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching tasks found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search query or clear the active filter tags.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        val health = SportsOpsLogic.calculateTaskHealth(task.status, task.deadline)
                        val daysRemaining = SportsOpsLogic.calculateDaysRemaining(task.deadline)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
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
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
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
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (task.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = task.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }

                                if (task.blocker != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = Color(0xFFFEF2F2),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Blocker: ${task.blocker}",
                                                color = Color(0xFF991B1B),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
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
                                        UserAvatar(name = task.teamMemberName, size = 22)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = task.teamMemberName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = task.vertical,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Due: ${task.deadline}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (daysRemaining != null && daysRemaining <= 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when {
                                                daysRemaining == null -> "No deadline"
                                                daysRemaining < 0 -> "${Math.abs(daysRemaining)}d overdue"
                                                daysRemaining == 0L -> "Due Today"
                                                daysRemaining == 1L -> "Due Tomorrow"
                                                else -> "in ${daysRemaining}d"
                                            },
                                            fontSize = 10.sp,
                                            color = if (daysRemaining != null && daysRemaining <= 0) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusChip(status = task.status)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    LinearProgressWithLabel(
                                        progressPercent = task.progressPercent,
                                        color = if (task.progressPercent == 100) StatusCompleted else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // FULL FILTER MODAL SHEET
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter Master Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.clearTaskFilters(); showFilterSheet = false }) {
                        Text("Reset All")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Vertical", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Event Operations", "Technology & Systems", "Stakeholder Coordination").forEach { vert ->
                        FilterChip(
                            selected = verticalFilter == vert,
                            onClick = { viewModel.setTaskVerticalFilter(if (verticalFilter == vert) null else vert) },
                            label = { Text(vert, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Priority", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.values().forEach { pri ->
                        FilterChip(
                            selected = priorityFilter == pri,
                            onClick = { viewModel.setTaskPriorityFilter(if (priorityFilter == pri) null else pri) },
                            label = { Text(pri.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Status", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(TaskStatus.NOT_STARTED, TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED, TaskStatus.COMPLETED).forEach { st ->
                        FilterChip(
                            selected = statusFilter == st,
                            onClick = { viewModel.setTaskStatusFilter(if (statusFilter == st) null else st) },
                            label = { Text(st.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters")
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
