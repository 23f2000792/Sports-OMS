package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.ui.theme.*
import com.example.viewmodel.SportsOpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposalReviewScreen(
    eventId: String,
    viewModel: SportsOpsViewModel,
    onNavigateBack: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val criteria by viewModel.rubricCriteria.collectAsState()
    val reviews by viewModel.proposalReviews.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val event = events.find { it.id == eventId }
    val existingUserReview = reviews.find { it.eventId == eventId && it.reviewerId == currentUser.id }
    val allSubmittedReviews = reviews.filter { it.eventId == eventId && it.isSubmitted }

    val isCore = currentUser.role == UserRole.CORE || currentUser.role == UserRole.DEPUTY_CORE

    // Dynamic scores state mapped by criterion ID
    var scoresMap by remember(existingUserReview) {
        val initial = mutableStateMapOf<String, Int>()
        val comments = mutableStateMapOf<String, String>()
        criteria.forEach { crit ->
            val match = existingUserReview?.criteriaScores?.find { it.criterionId == crit.id }
            initial[crit.id] = match?.score ?: 7
            comments[crit.id] = match?.comment ?: ""
        }
        mutableStateOf(initial)
    }

    var recommendation by remember(existingUserReview) {
        mutableStateOf(existingUserReview?.recommendation ?: ProposalRecommendation.RECOMMEND)
    }
    var strengths by remember(existingUserReview) {
        mutableStateOf(existingUserReview?.strengths ?: "")
    }
    var concerns by remember(existingUserReview) {
        mutableStateOf(existingUserReview?.concerns ?: "")
    }
    var suggestions by remember(existingUserReview) {
        mutableStateOf(existingUserReview?.suggestions ?: "")
    }

    var isSubmittedState by remember(existingUserReview) {
        mutableStateOf(existingUserReview?.isSubmitted ?: false)
    }

    val totalScore = criteria.sumOf { scoresMap[it.id] ?: 7 }
    val maxScore = criteria.size * 10
    val avgScore = if (allSubmittedReviews.isNotEmpty()) allSubmittedReviews.map { it.totalScore }.average() else totalScore.toDouble()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Event Proposal Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(event?.name ?: "Event $eventId", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            // HEADER BANNER
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SportsNavyDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("10-CRITERIA EVALUATION RUBRIC", color = SportsAmberLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(event?.name ?: "Event Proposal", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Independent reviewer evaluation. To preserve evaluation integrity, individual submissions remain independent until the review cycle closes.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("YOUR TOTAL SCORE", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("$totalScore / $maxScore", color = SportsAmberLight, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            if (isSubmittedState) {
                                Surface(color = Color(0xFF065F46), shape = RoundedCornerShape(6.dp)) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SportsEmeraldSuccess, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SUBMITTED", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // CORE AGGREGATED VIEW (If Core and submitted reviews exist)
            if (isCore && allSubmittedReviews.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Core Aggregated Intelligence (${allSubmittedReviews.size} Reviewers)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Average Rubric Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(String.format("%.1f / %d", avgScore, maxScore), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Consensus", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(if (avgScore >= 80) "Strong Candidate" else if (avgScore >= 60) "Viable with Changes" else "High Risk", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (avgScore >= 80) StatusCompleted else SportsAmberPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // 10 CRITERIA ITEMS
            item {
                Text("SCORING CRITERIA (10)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            }

            items(criteria) { crit ->
                val currentScore = scoresMap[crit.id] ?: 7

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(crit.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text("$currentScore / 10", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Text(crit.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = currentScore.toFloat(),
                            onValueChange = {
                                if (!isSubmittedState) {
                                    scoresMap[crit.id] = it.toInt()
                                }
                            },
                            valueRange = 0f..10f,
                            steps = 9,
                            enabled = !isSubmittedState,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // QUALITATIVE FEEDBACK SECTION
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Qualitative Assessment", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = strengths,
                            onValueChange = { strengths = it },
                            label = { Text("Operational Strengths") },
                            placeholder = { Text("What makes this event viable and well-planned?") },
                            enabled = !isSubmittedState,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = concerns,
                            onValueChange = { concerns = it },
                            label = { Text("Risks & Concerns") },
                            placeholder = { Text("What potential points of failure exist?") },
                            enabled = !isSubmittedState,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = suggestions,
                            onValueChange = { suggestions = it },
                            label = { Text("Required Adjustments / Suggestions") },
                            placeholder = { Text("Suggested improvements before execution...") },
                            enabled = !isSubmittedState,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // SUBMIT BUTTON
            item {
                if (!isSubmittedState) {
                    Button(
                        onClick = {
                            val reviewObj = ProposalReview(
                                id = existingUserReview?.id ?: "REV-${System.currentTimeMillis() % 10000}",
                                eventId = eventId,
                                eventTitle = event?.name ?: "Event $eventId",
                                reviewerId = currentUser.id,
                                reviewerName = currentUser.name,
                                criteriaScores = criteria.map {
                                    CriterionScore(it.id, it.name, scoresMap[it.id] ?: 7, 10, "")
                                },
                                totalScore = totalScore,
                                maxPossibleScore = maxScore,
                                recommendation = recommendation,
                                strengths = strengths.trim(),
                                concerns = concerns.trim(),
                                suggestions = suggestions.trim(),
                                isSubmitted = true,
                                submittedAt = "2026-08-16 12:00"
                            )
                            viewModel.submitProposalReview(reviewObj)
                            isSubmittedState = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Formal Review", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = { isSubmittedState = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Submitted Review")
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
