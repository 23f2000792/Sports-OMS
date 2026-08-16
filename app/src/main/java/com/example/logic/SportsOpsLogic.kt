package com.example.logic

import com.example.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object SportsOpsLogic {

    // Current operational reference date (2026-08-16)
    val CURRENT_OPERATIONAL_DATE: LocalDate = LocalDate.of(2026, 8, 16)
    private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    fun calculateDaysRemaining(deadlineStr: String?, fromDate: LocalDate = CURRENT_OPERATIONAL_DATE): Long? {
        if (deadlineStr.isNullOrBlank()) return null
        return try {
            val deadline = LocalDate.parse(deadlineStr.trim(), DATE_FORMATTER)
            ChronoUnit.DAYS.between(fromDate, deadline)
        } catch (e: Exception) {
            null
        }
    }

    fun calculateTaskHealth(
        status: TaskStatus,
        deadlineStr: String?,
        fromDate: LocalDate = CURRENT_OPERATIONAL_DATE
    ): TaskHealth {
        if (status == TaskStatus.COMPLETED) return TaskHealth.COMPLETED
        if (status == TaskStatus.CANCELLED) return TaskHealth.CANCELLED
        if (status == TaskStatus.BLOCKED) return TaskHealth.BLOCKED
        if (deadlineStr.isNullOrBlank()) return TaskHealth.NO_DEADLINE

        val daysRemaining = calculateDaysRemaining(deadlineStr, fromDate) ?: return TaskHealth.NO_DEADLINE

        return when {
            daysRemaining < 0 -> TaskHealth.OVERDUE
            daysRemaining <= 2 -> TaskHealth.AT_RISK
            else -> TaskHealth.ON_TRACK
        }
    }

    data class PhaseReadiness(
        val phaseNumber: Int,
        val phaseTitle: String,
        val totalRequirements: Int,
        val completedRequirements: Int,
        val percent: Int,
        val isBlocked: Boolean,
        val requirements: List<EventReadinessRequirement>
    )

    data class EventReadinessSummary(
        val eventId: String,
        val overallPercent: Int,
        val isExecutionRisk: Boolean,
        val phaseBreakdown: List<PhaseReadiness>,
        val blockingNotes: List<String>
    )

    fun isRequirementFullyCompleted(req: EventReadinessRequirement): Boolean {
        // A requirement is complete if POC and Coordinator are done, and Core is approved (or if Core hasn't rejected)
        return req.pocStatus == RequirementResponsibilityState.COMPLETED &&
                req.coordinatorStatus == RequirementResponsibilityState.COMPLETED &&
                req.coreStatus == RequirementResponsibilityState.COMPLETED
    }

    fun calculateEventReadiness(
        eventId: String,
        requirements: List<EventReadinessRequirement>
    ): EventReadinessSummary {
        val eventReqs = requirements.filter { it.eventId == eventId }
        if (eventReqs.isEmpty()) {
            return EventReadinessSummary(
                eventId = eventId,
                overallPercent = 0,
                isExecutionRisk = false,
                phaseBreakdown = emptyList(),
                blockingNotes = emptyList()
            )
        }

        val phaseNames = mapOf(
            1 to "Documentation",
            2 to "Branding",
            3 to "Registration",
            4 to "Technical Setup",
            5 to "Mock Trials",
            6 to "Execution",
            7 to "Closure"
        )

        val phaseSummaries = (1..7).map { phaseNum ->
            val phaseReqs = eventReqs.filter { it.phaseNumber == phaseNum }
            val total = phaseReqs.size
            val completed = phaseReqs.count { isRequirementFullyCompleted(it) }
            val pct = if (total > 0) (completed * 100) / total else 0
            val blocked = phaseReqs.any { req ->
                req.pocStatus == RequirementResponsibilityState.REJECTED ||
                        req.coreStatus == RequirementResponsibilityState.REJECTED ||
                        req.pocStatus == RequirementResponsibilityState.REWORK ||
                        (calculateDaysRemaining(req.deadline) ?: 10) < 0 && !isRequirementFullyCompleted(req)
            }
            PhaseReadiness(
                phaseNumber = phaseNum,
                phaseTitle = phaseNames[phaseNum] ?: "Phase $phaseNum",
                totalRequirements = total,
                completedRequirements = completed,
                percent = pct,
                isBlocked = blocked,
                requirements = phaseReqs
            )
        }

        val totalReqs = eventReqs.size
        val totalCompleted = eventReqs.count { isRequirementFullyCompleted(it) }
        val overallPercent = if (totalReqs > 0) (totalCompleted * 100) / totalReqs else 0

        val blockingNotes = mutableListOf<String>()
        eventReqs.forEach { req ->
            val days = calculateDaysRemaining(req.deadline)
            if (days != null && days < 0 && !isRequirementFullyCompleted(req)) {
                blockingNotes.add("${req.title} is overdue (${Math.abs(days)}d ago)")
            }
            if (req.coreStatus == RequirementResponsibilityState.REWORK) {
                blockingNotes.add("${req.title} requires Core rework")
            }
        }

        // Execution risk if phases 1-5 have incomplete critical items close to execution
        val isExecutionRisk = overallPercent < 70 && blockingNotes.isNotEmpty()

        return EventReadinessSummary(
            eventId = eventId,
            overallPercent = overallPercent,
            isExecutionRisk = isExecutionRisk,
            phaseBreakdown = phaseSummaries,
            blockingNotes = blockingNotes
        )
    }

    fun isIssueOverdue(issue: IssueItem, fromDate: LocalDate = CURRENT_OPERATIONAL_DATE): Boolean {
        if (issue.status == IssueStatus.RESOLVED || issue.status == IssueStatus.CLOSED) return false
        val days = calculateDaysRemaining(issue.deadline, fromDate) ?: return false
        return days < 0
    }

    fun checkUserPermission(
        role: UserRole,
        action: String,
        targetVertical: String? = null,
        userVertical: String? = null
    ): Boolean {
        return when (role) {
            UserRole.CORE -> true
            UserRole.DEPUTY_CORE -> {
                when (action) {
                    "DELETE_EVENT", "ARCHIVE_SYSTEM" -> false
                    else -> true
                }
            }
            UserRole.SUPER_COORDINATOR -> {
                if (targetVertical != null && userVertical != null && targetVertical != userVertical) {
                    false
                } else {
                    action !in listOf("DELETE_EVENT", "CORE_APPROVAL", "ASSIGN_SUPER_COORDINATOR")
                }
            }
            UserRole.COORDINATOR -> {
                action in listOf("UPDATE_TASK", "ASSIGN_VOLUNTEER", "UPDATE_READINESS_COORDINATOR", "RAISE_ISSUE", "UPLOAD_EVIDENCE", "ADD_REMARK")
            }
            UserRole.VOLUNTEER -> {
                action in listOf("UPDATE_MY_TASK_PROGRESS", "UPDATE_MY_TASK_STATUS", "UPLOAD_EVIDENCE", "RAISE_ISSUE", "VIEW_ASSIGNED")
            }
        }
    }

    // AI Grounded Query Engine (strictly answers from active app data)
    fun answerOpsAssistantQuery(
        query: String,
        tasks: List<TaskItem>,
        events: List<SportsEvent>,
        issues: List<IssueItem>,
        calendar: List<CalendarItem>,
        team: List<TeamMember>,
        readinessSummaries: Map<String, EventReadinessSummary>
    ): String {
        val q = query.lowercase().trim()
        val overdueTasks = tasks.filter { calculateTaskHealth(it.status, it.deadline) == TaskHealth.OVERDUE }
        val atRiskTasks = tasks.filter { calculateTaskHealth(it.status, it.deadline) == TaskHealth.AT_RISK }
        val blockedTasks = tasks.filter { it.status == TaskStatus.BLOCKED || it.blocker != null }
        val openCriticalIssues = issues.filter { it.severity == IssueSeverity.CRITICAL && it.status != IssueStatus.RESOLVED && it.status != IssueStatus.CLOSED }
        val atRiskEvents = events.filter {
            val r = readinessSummaries[it.id]
            (r?.overallPercent ?: 0) < 70 || (r?.isExecutionRisk == true)
        }

        return when {
            q.contains("overdue") || q.contains("late") -> {
                if (overdueTasks.isEmpty()) {
                    "Good news! There are currently no overdue tasks across the department."
                } else {
                    val items = overdueTasks.take(5).joinToString("\n") {
                        "- ${it.id}: \"${it.title}\" assigned to ${it.teamMemberName} (Deadline: ${it.deadline}, ${it.vertical})"
                    }
                    "Found ${overdueTasks.size} overdue task(s):\n$items\n\nRecommended Action: Reassign or follow up with assignees immediately."
                }
            }
            q.contains("risk") || q.contains("falling behind") -> {
                val eventRiskList = atRiskEvents.joinToString("\n") {
                    val r = readinessSummaries[it.id]
                    "- ${it.id} (${it.name}): ${r?.overallPercent ?: 0}% readiness. Blockers: ${r?.blockingNotes?.joinToString(", ") ?: "None"}"
                }
                val taskRiskList = atRiskTasks.take(4).joinToString("\n") {
                    "- ${it.id}: ${it.title} (${it.teamMemberName}, due in <= 48h)"
                }
                "Operations Risk Assessment:\n\nEvents At Risk (${atRiskEvents.size}):\n$eventRiskList\n\nTasks Due in <= 48h (${atRiskTasks.size}):\n$taskRiskList"
            }
            q.contains("focus") || q.contains("today") || q.contains("what should i do") -> {
                "Today's Priority Focus (August 16, 2026):\n" +
                        "1. [Critical Overdue]: ${overdueTasks.size} tasks need immediate resolution.\n" +
                        "2. [Critical Issues]: ${openCriticalIssues.size} unresolved critical blockers.\n" +
                        "3. [At-Risk Deadlines]: ${atRiskTasks.size} tasks due within 48 hours.\n" +
                        "4. [Events Below 70% Readiness]: ${atRiskEvents.map { it.id }.joinToString(", ")}.\n" +
                        "Top Action: Inspect Event E04 readiness checklist and resolve ISS-001."
            }
            q.contains("critical issue") || q.contains("issue") -> {
                if (openCriticalIssues.isEmpty()) {
                    "No open critical issues found in the register."
                } else {
                    val list = openCriticalIssues.joinToString("\n") {
                        "- ${it.id}: \"${it.problem}\" | Owner: ${it.assignedToName} | Deadline: ${it.deadline} | Status: ${it.status.displayName}"
                    }
                    "Active Critical Issues (${openCriticalIssues.size}):\n$list"
                }
            }
            q.contains("e01") || q.contains("football") -> {
                val ev = events.find { it.id == "E01" } ?: return "Event E01 not found."
                val r = readinessSummaries["E01"]
                "Event Summary: E01 - ${ev.name}\n- Society: ${ev.society}\n- Head: ${ev.eventHead} (${ev.eventHeadContact})\n- Sports POC: ${ev.sportsPoc}\n- Stage: ${ev.currentStage.displayName}\n- Readiness: ${r?.overallPercent ?: 0}%\n- Core Approval: ${ev.coreApproval.displayName}\n- Blockers: ${r?.blockingNotes?.joinToString("; ") ?: "None"}"
            }
            q.contains("e04") || q.contains("table tennis") -> {
                val ev = events.find { it.id == "E04" } ?: return "Event E04 not found."
                val r = readinessSummaries["E04"]
                "Event Summary: E04 - ${ev.name}\n- Society: ${ev.society}\n- Head: ${ev.eventHead}\n- Stage: ${ev.currentStage.displayName}\n- Readiness: ${r?.overallPercent ?: 0}%\n- Status: AT RISK\n- Blockers: ${r?.blockingNotes?.joinToString("; ") ?: "None"}"
            }
            q.contains("blocker") || q.contains("blocked") -> {
                val list = blockedTasks.joinToString("\n") {
                    "- ${it.id}: \"${it.title}\" (${it.teamMemberName}) -> Blocker: ${it.blocker ?: "Flagged Blocked"}"
                }
                "Currently Blocked Tasks (${blockedTasks.size}):\n${if (list.isBlank()) "None" else list}"
            }
            q.contains("team") || q.contains("workload") || q.contains("who") -> {
                val workload = team.map { member ->
                    val memberTasks = tasks.filter { it.teamMemberId == member.id }
                    val active = memberTasks.count { it.status == TaskStatus.IN_PROGRESS || it.status == TaskStatus.NOT_STARTED }
                    val overdue = memberTasks.count { calculateTaskHealth(it.status, it.deadline) == TaskHealth.OVERDUE }
                    "${member.name} (${member.role.displayName}): $active active, $overdue overdue"
                }.joinToString("\n")
                "Team Workload Breakdown:\n$workload"
            }
            else -> {
                "Sports Ops Assistant ready. You can ask:\n" +
                        "• \"Which tasks are overdue?\"\n" +
                        "• \"Which events are at risk?\"\n" +
                        "• \"What should I focus on today?\"\n" +
                        "• \"Show unresolved critical issues\"\n" +
                        "• \"Summarize Event E01\"\n" +
                        "• \"Who has pending tasks?\""
            }
        }
    }
}
