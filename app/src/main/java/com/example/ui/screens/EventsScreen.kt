package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun EventsScreen(
    viewModel: SportsOpsViewModel,
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToCreateEvent: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val searchQuery by viewModel.eventSearchQuery.collectAsState()
    val stageFilter by viewModel.eventStageFilter.collectAsState()
    val readinessMap by viewModel.eventReadinessMap.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val filteredEvents = events.filter { ev ->
        val matchesQuery = searchQuery.isBlank() ||
                ev.id.contains(searchQuery, ignoreCase = true) ||
                ev.name.contains(searchQuery, ignoreCase = true) ||
                ev.society.contains(searchQuery, ignoreCase = true) ||
                ev.eventHead.contains(searchQuery, ignoreCase = true)
        val matchesStage = stageFilter == null || ev.currentStage == stageFilter
        matchesQuery && matchesStage
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sports Events Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${events.size} active operational tournament(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (currentUser.role == UserRole.CORE || currentUser.role == UserRole.DEPUTY_CORE) {
                FloatingActionButton(
                    onClick = onNavigateToCreateEvent,
                    containerColor = SportsNavyDark,
                    contentColor = SportsAmberLight,
                    modifier = Modifier.padding(bottom = 60.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
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
            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setEventSearchQuery(it) },
                placeholder = { Text("Search event ID, name, society, event head...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setEventSearchQuery("") }) {
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

            // STAGE FILTER CHIPS
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = stageFilter == null,
                        onClick = { viewModel.setEventStageFilter(null) },
                        label = { Text("All Stages (${events.size})", fontSize = 12.sp) }
                    )
                }
                EventStage.values().forEach { stage ->
                    val count = events.count { it.currentStage == stage }
                    if (count > 0) {
                        item {
                            FilterChip(
                                selected = stageFilter == stage,
                                onClick = { viewModel.setEventStageFilter(if (stageFilter == stage) null else stage) },
                                label = { Text("${stage.displayName} ($count)", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // EVENTS LIST
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEvents, key = { it.id }) { ev ->
                    val readiness = readinessMap[ev.id]
                    val pct = readiness?.overallPercent ?: ev.readinessPercent
                    val isRisk = readiness?.isExecutionRisk == true

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToEventDetail(ev.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = SportsNavyMedium,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = ev.id,
                                            color = SportsAmberLight,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFFEFF6FF),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = ev.currentStage.displayName.uppercase(),
                                            color = Color(0xFF1D4ED8),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (isRisk) {
                                    Surface(
                                        color = Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "EXECUTION RISK",
                                                color = Color(0xFF991B1B),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = ev.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = ev.society,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Event Head", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(ev.eventHead, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("Sports POC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(ev.sportsPoc, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Dates", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${ev.startDate.substringAfter("-")} to ${ev.endDate.substringAfter("-")}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            if (readiness?.blockingNotes?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFFFFBEB),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = readiness.blockingNotes.first(),
                                            color = Color(0xFF92400E),
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

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
    }
}
