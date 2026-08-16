package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
fun CalendarScreen(
    viewModel: SportsOpsViewModel
) {
    val context = LocalContext.current
    val calendarItems by viewModel.calendarItems.collectAsState()

    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Unique dates
    val dates = remember(calendarItems) {
        calendarItems.map { it.date }.distinct().sorted()
    }

    val categories = listOf("All", "Review", "Tournament", "Briefing", "Deadline", "Vendor Delivery")

    val filteredItems = calendarItems.filter { item ->
        val matchesDate = selectedDate == null || item.date == selectedDate
        val matchesCategory = selectedCategory == null || selectedCategory == "All" || item.category.contains(selectedCategory!!, ignoreCase = true)
        matchesDate && matchesCategory
    }.sortedWith(compareBy({ it.date }, { it.time }))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Department Master Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Operational schedule & hard deadlines", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SportsNavyDark,
                contentColor = SportsAmberLight,
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // DATE SELECTOR HORIZONTAL ROW
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Surface(
                        color = if (selectedDate == null) SportsNavyDark else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clickable { selectedDate = null }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("ALL", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (selectedDate == null) SportsAmberLight else MaterialTheme.colorScheme.onSurface)
                            Text("DAYS", fontSize = 10.sp, color = if (selectedDate == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                items(dates) { d ->
                    val dayNum = d.substringAfterLast("-")
                    val isSelected = selectedDate == d

                    Surface(
                        color = if (isSelected) SportsNavyDark else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable { selectedDate = if (isSelected) null else d }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(dayNum, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isSelected) SportsAmberLight else MaterialTheme.colorScheme.onSurface)
                            Text("AUG", fontSize = 10.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // CATEGORY CHIPS
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(categories) { cat ->
                    val isSel = (selectedCategory == null && cat == "All") || selectedCategory == cat
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCategory = if (cat == "All") null else cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }

            // CALENDAR ITEMS LIST
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = SportsNavyMedium,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${item.date.substringAfter("-")} • ${item.time}",
                                            color = SportsAmberLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PriorityChip(priority = item.priority)
                                }

                                if (item.deadlineType == DeadlineType.HARD_DEADLINE) {
                                    Surface(
                                        color = Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "HARD DEADLINE",
                                            color = Color(0xFFDC2626),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = item.category,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.activity,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(item.eventOrArea, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "Lead: ${item.personResponsible}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (item.meetingUrl != null || item.resourceUrl != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (item.meetingUrl != null) {
                                        OutlinedButton(
                                            onClick = {
                                                try {
                                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.meetingUrl))
                                                    context.startActivity(browserIntent)
                                                } catch (e: Exception) {}
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Join Meet", fontSize = 11.sp)
                                        }
                                    }
                                    if (item.resourceUrl != null) {
                                        OutlinedButton(
                                            onClick = {
                                                try {
                                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.resourceUrl))
                                                    context.startActivity(browserIntent)
                                                } catch (e: Exception) {}
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Open Resource", fontSize = 11.sp)
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

    // ADD CALENDAR ITEM DIALOG
    if (showAddDialog) {
        var dateInput by remember { mutableStateOf("2026-08-20") }
        var timeInput by remember { mutableStateOf("10:00 AM") }
        var activityInput by remember { mutableStateOf("") }
        var areaInput by remember { mutableStateOf("") }
        var leadInput by remember { mutableStateOf("") }
        var isHardDeadline by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Calendar Milestone") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = activityInput, onValueChange = { activityInput = it }, label = { Text("Activity / Milestone *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dateInput, onValueChange = { dateInput = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = timeInput, onValueChange = { timeInput = it }, label = { Text("Time") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = areaInput, onValueChange = { areaInput = it }, label = { Text("Event / Area") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = leadInput, onValueChange = { leadInput = it }, label = { Text("Person Responsible") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isHardDeadline, onCheckedChange = { isHardDeadline = it })
                        Text("Hard Operational Deadline", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (activityInput.isNotBlank()) {
                            viewModel.addCalendarItem(
                                CalendarItem(
                                    id = "CAL-${System.currentTimeMillis() % 1000}",
                                    date = dateInput.trim(),
                                    time = timeInput.trim(),
                                    activity = activityInput.trim(),
                                    eventOrArea = areaInput.trim(),
                                    personResponsible = leadInput.trim().ifEmpty { "Sports Team" },
                                    category = if (isHardDeadline) "Deadline" else "Operations",
                                    priority = if (isHardDeadline) Priority.CRITICAL else Priority.MEDIUM,
                                    deadlineType = if (isHardDeadline) DeadlineType.HARD_DEADLINE else DeadlineType.SOFT_DEADLINE
                                )
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
