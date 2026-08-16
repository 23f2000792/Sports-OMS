package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SportsOpsRepository
import com.example.data.SportsOpsRepositoryImpl
import com.example.logic.SportsOpsLogic
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GlobalSearchResult(
    val category: String, // Task, Event, Issue, Team, Calendar
    val id: String,
    val title: String,
    val subtitle: String,
    val statusBadge: String? = null
)

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String = "12:00 PM"
)

enum class TaskSortOption {
    DEADLINE,
    PRIORITY,
    HEALTH,
    RECENTLY_UPDATED,
    PROGRESS,
    ASSIGNEE
}

class SportsOpsViewModel(
    private val repository: SportsOpsRepository = SportsOpsRepositoryImpl()
) : ViewModel() {

    val currentUser = repository.currentUser
    val allUsers = repository.allUsers
    val teamMembers = repository.teamMembers
    val tasks = repository.tasks
    val events = repository.events
    val readinessRequirements = repository.readinessRequirements
    val issues = repository.issues
    val calendarItems = repository.calendarItems
    val proposalReviews = repository.proposalReviews
    val rubricCriteria = repository.rubricCriteria
    val approvals = repository.approvals
    val notifications = repository.notifications
    val auditLogs = repository.auditLogs
    val cloudSyncSummary = repository.cloudSyncSummary
    val isAuthenticated = repository.isAuthenticated

    private val _isSyncingCloud = MutableStateFlow(false)
    val isSyncingCloud = _isSyncingCloud.asStateFlow()

    // Filters for Master Tasks
    private val _taskSearchQuery = MutableStateFlow("")
    val taskSearchQuery = _taskSearchQuery.asStateFlow()

    private val _taskVerticalFilter = MutableStateFlow<String?>(null)
    val taskVerticalFilter = _taskVerticalFilter.asStateFlow()

    private val _taskPriorityFilter = MutableStateFlow<Priority?>(null)
    val taskPriorityFilter = _taskPriorityFilter.asStateFlow()

    private val _taskHealthFilter = MutableStateFlow<TaskHealth?>(null)
    val taskHealthFilter = _taskHealthFilter.asStateFlow()

    private val _taskStatusFilter = MutableStateFlow<TaskStatus?>(null)
    val taskStatusFilter = _taskStatusFilter.asStateFlow()

    private val _taskAssigneeFilter = MutableStateFlow<String?>(null)
    val taskAssigneeFilter = _taskAssigneeFilter.asStateFlow()

    private val _taskSortOption = MutableStateFlow(TaskSortOption.DEADLINE)
    val taskSortOption = _taskSortOption.asStateFlow()

    // Filter for Events
    private val _eventSearchQuery = MutableStateFlow("")
    val eventSearchQuery = _eventSearchQuery.asStateFlow()

    private val _eventStageFilter = MutableStateFlow<EventStage?>(null)
    val eventStageFilter = _eventStageFilter.asStateFlow()

    // Filter for Issues
    private val _issueSearchQuery = MutableStateFlow("")
    val issueSearchQuery = _issueSearchQuery.asStateFlow()

    private val _issueSeverityFilter = MutableStateFlow<IssueSeverity?>(null)
    val issueSeverityFilter = _issueSeverityFilter.asStateFlow()

    private val _issueStatusFilter = MutableStateFlow<IssueStatus?>(null)
    val issueStatusFilter = _issueStatusFilter.asStateFlow()

    // Global Search Query
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery = _globalSearchQuery.asStateFlow()

    // AI Chat History
    private val _aiChatMessages = MutableStateFlow(
        listOf(
            AiChatMessage("1", false, "Hello! I am your Sports Operations Assistant. I can summarize event readiness, surface overdue tasks, analyze blockers, or identify operational risks grounded strictly in current department data. How can I help?")
        )
    )
    val aiChatMessages = _aiChatMessages.asStateFlow()

    // Filtered Tasks (Chained combines for type safety)
    private val taskFilterCriteria = combine(
        taskSearchQuery,
        taskVerticalFilter,
        taskPriorityFilter,
        taskHealthFilter
    ) { q, v, p, h ->
        Tuple4(q, v, p, h)
    }

    private val taskStatusAndSort = combine(
        taskStatusFilter,
        taskAssigneeFilter,
        taskSortOption
    ) { s, a, sort ->
        Triple(s, a, sort)
    }

    val filteredTasks: StateFlow<List<TaskItem>> = combine(
        tasks,
        taskFilterCriteria,
        taskStatusAndSort
    ) { allTasks, (query, vertical, priority, health), (status, assignee, sort) ->
        var list = allTasks

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.id.lowercase().contains(q) ||
                        it.title.lowercase().contains(q) ||
                        it.teamMemberName.lowercase().contains(q) ||
                        (it.eventName?.lowercase()?.contains(q) == true) ||
                        it.taskType.lowercase().contains(q)
            }
        }
        if (vertical != null) list = list.filter { it.vertical == vertical }
        if (priority != null) list = list.filter { it.priority == priority }
        if (health != null) list = list.filter { SportsOpsLogic.calculateTaskHealth(it.status, it.deadline) == health }
        if (status != null) list = list.filter { it.status == status }
        if (assignee != null) list = list.filter { it.teamMemberId == assignee }

        when (sort) {
            TaskSortOption.DEADLINE -> list.sortedBy { it.deadline }
            TaskSortOption.PRIORITY -> list.sortedBy { it.priority.ordinal }
            TaskSortOption.HEALTH -> list.sortedBy { SportsOpsLogic.calculateTaskHealth(it.status, it.deadline).ordinal }
            TaskSortOption.RECENTLY_UPDATED -> list.sortedByDescending { it.lastUpdated }
            TaskSortOption.PROGRESS -> list.sortedByDescending { it.progressPercent }
            TaskSortOption.ASSIGNEE -> list.sortedBy { it.teamMemberName }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // My Work Tasks (Filtered by logged in user)
    val myTasks = combine(tasks, currentUser) { allTasks, user ->
        allTasks.filter { it.teamMemberId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Event Readiness Map
    val eventReadinessMap = combine(events, readinessRequirements) { evs, reqs ->
        evs.associate { it.id to SportsOpsLogic.calculateEventReadiness(it.id, reqs) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Attention Required Alerts
    val attentionRequiredAlerts = combine(tasks, issues, events, eventReadinessMap) { allTasks, allIssues, evs, rMap ->
        val alerts = mutableListOf<String>()
        val overdueTasks = allTasks.filter { SportsOpsLogic.calculateTaskHealth(it.status, it.deadline) == TaskHealth.OVERDUE }
        if (overdueTasks.isNotEmpty()) {
            alerts.add("${overdueTasks.size} critical task(s) are overdue across the department.")
        }
        val openCritIssues = allIssues.filter { it.severity == IssueSeverity.CRITICAL && it.status != IssueStatus.RESOLVED && it.status != IssueStatus.CLOSED }
        if (openCritIssues.isNotEmpty()) {
            alerts.add("${openCritIssues.size} critical issue(s) require escalation / Core approval.")
        }
        val atRiskEvs = evs.filter { (rMap[it.id]?.overallPercent ?: 0) < 70 && it.currentStage != EventStage.PROPOSAL }
        if (atRiskEvs.isNotEmpty()) {
            alerts.add("${atRiskEvs.size} event(s) (${atRiskEvs.joinToString { it.id }}) have low readiness (<70%).")
        }
        val dueSoon = allTasks.filter { SportsOpsLogic.calculateTaskHealth(it.status, it.deadline) == TaskHealth.AT_RISK }
        if (dueSoon.isNotEmpty()) {
            alerts.add("${dueSoon.size} task(s) are due within 48 hours.")
        }
        alerts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global Search Results
    val globalSearchResults: StateFlow<List<GlobalSearchResult>> = combine(
        globalSearchQuery,
        combine(tasks, events) { t, e -> Pair(t, e) },
        combine(issues, teamMembers, calendarItems) { i, tm, c -> Triple(i, tm, c) }
    ) { query, (allTasks, allEvents), (allIssues, allTeam, allCal) ->
        if (query.isBlank()) return@combine emptyList<GlobalSearchResult>()
        val q = query.trim().lowercase()
        val results = mutableListOf<GlobalSearchResult>()

        allTasks.filter {
            it.id.lowercase().contains(q) || it.title.lowercase().contains(q) || it.teamMemberName.lowercase().contains(q)
        }.forEach {
            results.add(GlobalSearchResult("Task", it.id, it.title, "Assignee: ${it.teamMemberName} | Due: ${it.deadline}", it.status.displayName))
        }

        allEvents.filter {
            it.id.lowercase().contains(q) || it.name.lowercase().contains(q) || it.society.lowercase().contains(q) || it.eventHead.lowercase().contains(q)
        }.forEach {
            results.add(GlobalSearchResult("Event", it.id, it.name, "Stage: ${it.currentStage.displayName} | Head: ${it.eventHead}", "${it.readinessPercent}% Ready"))
        }

        allIssues.filter {
            it.id.lowercase().contains(q) || it.problem.lowercase().contains(q) || it.assignedToName.lowercase().contains(q)
        }.forEach {
            results.add(GlobalSearchResult("Issue", it.id, it.problem, "Severity: ${it.severity.displayName} | Assigned: ${it.assignedToName}", it.status.displayName))
        }

        allTeam.filter {
            it.name.lowercase().contains(q) || it.vertical.lowercase().contains(q) || it.role.displayName.lowercase().contains(q)
        }.forEach {
            results.add(GlobalSearchResult("Team", it.id, it.name, "${it.role.displayName} • ${it.vertical}", if (it.active) "Active" else "Inactive"))
        }

        allCal.filter {
            it.id.lowercase().contains(q) || it.activity.lowercase().contains(q) || it.eventOrArea.lowercase().contains(q)
        }.forEach {
            results.add(GlobalSearchResult("Calendar", it.id, it.activity, "${it.date} ${it.time} | ${it.eventOrArea}", it.category))
        }

        results.take(30)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setTaskSearchQuery(q: String) { _taskSearchQuery.value = q }
    fun setTaskVerticalFilter(v: String?) { _taskVerticalFilter.value = v }
    fun setTaskPriorityFilter(p: Priority?) { _taskPriorityFilter.value = p }
    fun setTaskHealthFilter(h: TaskHealth?) { _taskHealthFilter.value = h }
    fun setTaskStatusFilter(s: TaskStatus?) { _taskStatusFilter.value = s }
    fun setTaskAssigneeFilter(a: String?) { _taskAssigneeFilter.value = a }
    fun setTaskSortOption(s: TaskSortOption) { _taskSortOption.value = s }
    fun clearTaskFilters() {
        _taskSearchQuery.value = ""
        _taskVerticalFilter.value = null
        _taskPriorityFilter.value = null
        _taskHealthFilter.value = null
        _taskStatusFilter.value = null
        _taskAssigneeFilter.value = null
    }

    fun setEventSearchQuery(q: String) { _eventSearchQuery.value = q }
    fun setEventStageFilter(s: EventStage?) { _eventStageFilter.value = s }

    fun setIssueSearchQuery(q: String) { _issueSearchQuery.value = q }
    fun setIssueSeverityFilter(s: IssueSeverity?) { _issueSeverityFilter.value = s }
    fun setIssueStatusFilter(s: IssueStatus?) { _issueStatusFilter.value = s }

    fun setGlobalSearchQuery(q: String) { _globalSearchQuery.value = q }

    fun switchUser(userId: String) {
        repository.switchUser(userId)
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus, progress: Int? = null, remark: String? = null) {
        repository.updateTaskStatus(taskId, status, progress, remark)
    }

    fun updateTaskProgress(taskId: String, progress: Int) {
        repository.updateTaskProgress(taskId, progress)
    }

    fun addTaskBlocker(taskId: String, blocker: String) {
        repository.addTaskBlocker(taskId, blocker)
    }

    fun clearTaskBlocker(taskId: String) {
        repository.clearTaskBlocker(taskId)
    }

    fun addEvidenceToTask(taskId: String, attachment: EvidenceAttachment) {
        repository.addEvidenceToTask(taskId, attachment)
    }

    fun createOrUpdateTask(task: TaskItem) {
        repository.createOrUpdateTask(task)
    }

    fun reassignTask(taskId: String, newAssigneeId: String) {
        repository.reassignTask(taskId, newAssigneeId)
    }

    fun updateEventStage(eventId: String, stage: EventStage) {
        repository.updateEventStage(eventId, stage)
    }

    fun createOrUpdateEvent(event: SportsEvent) {
        repository.createOrUpdateEvent(event)
    }

    fun updateRequirementStatus(
        reqId: String,
        pocState: RequirementResponsibilityState? = null,
        coordState: RequirementResponsibilityState? = null,
        coreState: RequirementResponsibilityState? = null,
        notes: String? = null
    ) {
        repository.updateRequirementStatus(reqId, pocState, coordState, coreState, notes)
    }

    fun createIssue(issue: IssueItem) {
        repository.createIssue(issue)
    }

    fun updateIssueStatus(issueId: String, status: IssueStatus, resolution: String? = null) {
        repository.updateIssueStatus(issueId, status, resolution)
    }

    fun escalateIssue(issueId: String, targetUserId: String, reason: String) {
        repository.escalateIssue(issueId, targetUserId, reason)
    }

    fun addEvidenceToIssue(issueId: String, attachment: EvidenceAttachment) {
        repository.addEvidenceToIssue(issueId, attachment)
    }

    fun submitProposalReview(review: ProposalReview) {
        repository.submitProposalReview(review)
    }

    fun handleApprovalAction(approvalId: String, action: CoreApprovalStatus, remark: String) {
        repository.handleApprovalAction(approvalId, action, remark)
    }

    fun addCalendarItem(item: CalendarItem) {
        repository.addCalendarItem(item)
    }

    fun deleteTask(taskId: String) {
        repository.deleteTask(taskId)
    }

    fun deleteEvent(eventId: String) {
        repository.deleteEvent(eventId)
    }

    fun deleteIssue(issueId: String) {
        repository.deleteIssue(issueId)
    }

    fun deleteCalendarItem(calendarId: String) {
        repository.deleteCalendarItem(calendarId)
    }

    fun deleteApproval(approvalId: String) {
        repository.deleteApproval(approvalId)
    }

    fun clearAllData() {
        repository.clearAllData()
    }

    fun seedOperationalFrameworkToFirestore(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncingCloud.value = true
            val result = repository.seedOperationalFrameworkToFirestore()
            _isSyncingCloud.value = false
            if (result.isSuccess) {
                onComplete(true, "Successfully initialized ${result.getOrNull()} items into Firebase Studio Console!")
            } else {
                onComplete(false, result.exceptionOrNull()?.message ?: "Failed to seed operational framework")
            }
        }
    }

    fun login(user: CurrentUser) {
        repository.login(user)
    }

    fun loginWithCustomProfile(name: String, email: String, role: UserRole, vertical: String, phone: String) {
        repository.loginWithCustomProfile(name, email, role, vertical, phone)
    }

    fun registerNewTeamMember(member: TeamMember) {
        repository.registerNewTeamMember(member)
    }

    fun logout() {
        repository.logout()
    }

    fun markNotificationAsRead(id: String) {
        repository.markNotificationAsRead(id)
    }

    fun markAllNotificationsAsRead() {
        repository.markAllNotificationsAsRead()
    }

    fun syncAllToCloud(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncingCloud.value = true
            val result = repository.syncAllToFirestore()
            _isSyncingCloud.value = false
            if (result.isSuccess) {
                onComplete(true, "Successfully synced ${result.getOrNull()} items to Cloud Firestore!")
            } else {
                onComplete(false, result.exceptionOrNull()?.message ?: "Sync encountered an issue")
            }
        }
    }

    fun sendAiAssistantMessage(query: String) {
        if (query.isBlank()) return
        val userMsg = AiChatMessage(
            id = System.currentTimeMillis().toString(),
            isUser = true,
            text = query.trim()
        )
        _aiChatMessages.update { it + userMsg }

        viewModelScope.launch {
            val responseText = repository.queryAiAssistant(query)
            val botMsg = AiChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                isUser = false,
                text = responseText
            )
            _aiChatMessages.update { it + botMsg }
        }
    }
}

data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
