package com.example.data

import android.content.Context
import com.example.data.firebase.CloudConnectionStatus
import com.example.data.firebase.CloudSyncSummary
import com.example.data.firebase.FirestoreSyncManager
import com.example.data.local.AppDatabase
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.logic.SportsOpsLogic
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface SportsOpsRepository {
    val currentUser: StateFlow<CurrentUser>
    val allUsers: StateFlow<List<CurrentUser>>
    val teamMembers: StateFlow<List<TeamMember>>
    val tasks: StateFlow<List<TaskItem>>
    val events: StateFlow<List<SportsEvent>>
    val readinessRequirements: StateFlow<List<EventReadinessRequirement>>
    val issues: StateFlow<List<IssueItem>>
    val calendarItems: StateFlow<List<CalendarItem>>
    val proposalReviews: StateFlow<List<ProposalReview>>
    val rubricCriteria: StateFlow<List<ReviewCriterion>>
    val approvals: StateFlow<List<ApprovalItem>>
    val notifications: StateFlow<List<NotificationItem>>
    val auditLogs: StateFlow<List<AuditLogEntry>>
    val cloudSyncSummary: StateFlow<CloudSyncSummary>

    fun switchUser(userId: String)
    fun updateTaskStatus(taskId: String, newStatus: TaskStatus, progress: Int? = null, remark: String? = null)
    fun updateTaskProgress(taskId: String, newProgress: Int)
    fun addTaskBlocker(taskId: String, blockerText: String)
    fun clearTaskBlocker(taskId: String)
    fun addEvidenceToTask(taskId: String, attachment: EvidenceAttachment)
    fun createOrUpdateTask(task: TaskItem)
    fun reassignTask(taskId: String, newAssigneeId: String)
    
    fun updateEventStage(eventId: String, newStage: EventStage)
    fun createOrUpdateEvent(event: SportsEvent)
    fun updateRequirementStatus(
        reqId: String,
        pocState: RequirementResponsibilityState? = null,
        coordState: RequirementResponsibilityState? = null,
        coreState: RequirementResponsibilityState? = null,
        notes: String? = null
    )

    fun createIssue(issue: IssueItem)
    fun updateIssueStatus(issueId: String, newStatus: IssueStatus, resolution: String? = null)
    fun escalateIssue(issueId: String, targetUserId: String, reason: String)
    fun addEvidenceToIssue(issueId: String, attachment: EvidenceAttachment)

    fun submitProposalReview(review: ProposalReview)

    fun handleApprovalAction(approvalId: String, action: CoreApprovalStatus, remark: String)

    fun addCalendarItem(item: CalendarItem)

    fun markNotificationAsRead(notificationId: String)
    fun markAllNotificationsAsRead()

    fun queryAiAssistant(prompt: String): String

    suspend fun syncAllToFirestore(): Result<Int>
}

class SportsOpsRepositoryImpl(
    private val context: Context? = com.example.SportsOpsApp.applicationContextOrNull
) : SportsOpsRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val db: AppDatabase? = context?.let { AppDatabase.getDatabase(it) }
    private val firestoreManager = FirestoreSyncManager(repositoryScope)

    private val _currentUser = MutableStateFlow(SportsOpsSeedData.users[0])
    override val currentUser: StateFlow<CurrentUser> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow(SportsOpsSeedData.users)
    override val allUsers: StateFlow<List<CurrentUser>> = _allUsers.asStateFlow()

    private val _teamMembers = MutableStateFlow(SportsOpsSeedData.teamMembers)
    override val teamMembers: StateFlow<List<TeamMember>> = _teamMembers.asStateFlow()

    private val _tasks = MutableStateFlow(SportsOpsSeedData.tasks)
    override val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _events = MutableStateFlow(SportsOpsSeedData.events)
    override val events: StateFlow<List<SportsEvent>> = _events.asStateFlow()

    private val _readinessRequirements = MutableStateFlow(SportsOpsSeedData.readinessRequirements)
    override val readinessRequirements: StateFlow<List<EventReadinessRequirement>> = _readinessRequirements.asStateFlow()

    private val _issues = MutableStateFlow(SportsOpsSeedData.issues)
    override val issues: StateFlow<List<IssueItem>> = _issues.asStateFlow()

    private val _calendarItems = MutableStateFlow(SportsOpsSeedData.calendarItems)
    override val calendarItems: StateFlow<List<CalendarItem>> = _calendarItems.asStateFlow()

    private val _proposalReviews = MutableStateFlow(SportsOpsSeedData.proposalReviews)
    override val proposalReviews: StateFlow<List<ProposalReview>> = _proposalReviews.asStateFlow()

    private val _rubricCriteria = MutableStateFlow(SportsOpsSeedData.proposalRubricCriteria)
    override val rubricCriteria: StateFlow<List<ReviewCriterion>> = _rubricCriteria.asStateFlow()

    private val _approvals = MutableStateFlow(SportsOpsSeedData.approvals)
    override val approvals: StateFlow<List<ApprovalItem>> = _approvals.asStateFlow()

    private val _notifications = MutableStateFlow(SportsOpsSeedData.notifications)
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _auditLogs = MutableStateFlow(SportsOpsSeedData.auditLogs)
    override val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    override val cloudSyncSummary: StateFlow<CloudSyncSummary> = firestoreManager.syncSummary

    init {
        // Setup Room Database observations if context exists
        if (db != null) {
            repositoryScope.launch {
                // Initialize seed data into database if empty
                val existingTasks = db.taskDao().getAllTasks().firstOrNull() ?: emptyList()
                if (existingTasks.isEmpty()) {
                    db.taskDao().insertAll(SportsOpsSeedData.tasks.map { it.toEntity() })
                    db.eventDao().insertAll(SportsOpsSeedData.events.map { it.toEntity() })
                    db.readinessDao().insertAll(SportsOpsSeedData.readinessRequirements.map { it.toEntity() })
                    db.issueDao().insertAll(SportsOpsSeedData.issues.map { it.toEntity() })
                    db.calendarDao().insertAll(SportsOpsSeedData.calendarItems.map { it.toEntity() })
                    db.approvalDao().insertAll(SportsOpsSeedData.approvals.map { it.toEntity() })
                    db.proposalReviewDao().insertAll(SportsOpsSeedData.proposalReviews.map { it.toEntity() })
                    db.auditLogDao().insertAll(SportsOpsSeedData.auditLogs.map { it.toEntity() })
                }

                // Collect from Room and update StateFlows
                launch { db.taskDao().getAllTasks().collect { list -> if (list.isNotEmpty()) _tasks.value = list.map { it.toDomain() } } }
                launch { db.eventDao().getAllEvents().collect { list -> if (list.isNotEmpty()) _events.value = list.map { it.toDomain() } } }
                launch { db.readinessDao().getAllRequirements().collect { list -> if (list.isNotEmpty()) _readinessRequirements.value = list.map { it.toDomain() } } }
                launch { db.issueDao().getAllIssues().collect { list -> if (list.isNotEmpty()) _issues.value = list.map { it.toDomain() } } }
                launch { db.calendarDao().getAllCalendarItems().collect { list -> if (list.isNotEmpty()) _calendarItems.value = list.map { it.toDomain() } } }
                launch { db.approvalDao().getAllApprovals().collect { list -> if (list.isNotEmpty()) _approvals.value = list.map { it.toDomain() } } }
                launch { db.proposalReviewDao().getAllReviews().collect { list -> if (list.isNotEmpty()) _proposalReviews.value = list.map { it.toDomain() } } }
                launch { db.auditLogDao().getAllLogs().collect { list -> if (list.isNotEmpty()) _auditLogs.value = list.map { it.toDomain() } } }
            }
        }

        // Setup real-time listener from Firestore
        firestoreManager.setupRealtimeListeners(
            onTasksUpdated = { remoteTasks ->
                if (remoteTasks.isNotEmpty()) {
                    _tasks.value = remoteTasks
                    db?.let { repositoryScope.launch { it.taskDao().insertAll(remoteTasks.map { t -> t.toEntity() }) } }
                }
            },
            onEventsUpdated = { remoteEvents ->
                if (remoteEvents.isNotEmpty()) {
                    _events.value = remoteEvents
                    db?.let { repositoryScope.launch { it.eventDao().insertAll(remoteEvents.map { e -> e.toEntity() }) } }
                }
            },
            onReadinessUpdated = { remoteReqs ->
                if (remoteReqs.isNotEmpty()) {
                    _readinessRequirements.value = remoteReqs
                    db?.let { repositoryScope.launch { it.readinessDao().insertAll(remoteReqs.map { r -> r.toEntity() }) } }
                }
            },
            onIssuesUpdated = { remoteIssues ->
                if (remoteIssues.isNotEmpty()) {
                    _issues.value = remoteIssues
                    db?.let { repositoryScope.launch { it.issueDao().insertAll(remoteIssues.map { i -> i.toEntity() }) } }
                }
            },
            onCalendarUpdated = { remoteCal ->
                if (remoteCal.isNotEmpty()) {
                    _calendarItems.value = remoteCal
                    db?.let { repositoryScope.launch { it.calendarDao().insertAll(remoteCal.map { c -> c.toEntity() }) } }
                }
            },
            onApprovalsUpdated = { remoteApps ->
                if (remoteApps.isNotEmpty()) {
                    _approvals.value = remoteApps
                    db?.let { repositoryScope.launch { it.approvalDao().insertAll(remoteApps.map { a -> a.toEntity() }) } }
                }
            },
            onReviewsUpdated = { remoteRev ->
                if (remoteRev.isNotEmpty()) {
                    _proposalReviews.value = remoteRev
                    db?.let { repositoryScope.launch { it.proposalReviewDao().insertAll(remoteRev.map { p -> p.toEntity() }) } }
                }
            },
            onAuditLogsUpdated = { remoteLogs ->
                if (remoteLogs.isNotEmpty()) {
                    _auditLogs.value = remoteLogs
                    db?.let { repositoryScope.launch { it.auditLogDao().insertAll(remoteLogs.map { l -> l.toEntity() }) } }
                }
            }
        )
    }

    private fun addAudit(objectType: String, objectId: String, action: String, details: String) {
        val user = _currentUser.value
        val entry = AuditLogEntry(
            id = "LOG-${System.currentTimeMillis() % 100000}",
            user = user.name,
            userRole = user.role,
            timestamp = "2026-08-16 12:00",
            objectType = objectType,
            objectId = objectId,
            action = action,
            details = details
        )
        _auditLogs.update { listOf(entry) + it }
        db?.let { repositoryScope.launch { it.auditLogDao().insert(entry.toEntity()) } }
        firestoreManager.syncAuditLog(entry)
    }

    override fun switchUser(userId: String) {
        val found = _allUsers.value.find { it.id == userId }
        if (found != null) {
            _currentUser.value = found
            addAudit("User Session", userId, "Switched Role", "Switched active workspace session to ${found.name} (${found.role.displayName})")
        }
    }

    override fun updateTaskStatus(taskId: String, newStatus: TaskStatus, progress: Int?, remark: String?) {
        val user = _currentUser.value
        var updatedTask: TaskItem? = null
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val prevStatus = task.status
                    val finalProgress = when {
                        newStatus == TaskStatus.COMPLETED -> 100
                        progress != null -> progress
                        newStatus == TaskStatus.IN_PROGRESS && task.progressPercent == 0 -> 20
                        else -> task.progressPercent
                    }
                    val newCompletedOn = if (newStatus == TaskStatus.COMPLETED) "2026-08-16" else null
                    val newActivity = TaskActivity(
                        id = "ACT-${System.currentTimeMillis() % 10000}",
                        user = user.name,
                        action = "Changed status from ${prevStatus.displayName} to ${newStatus.displayName}",
                        timestamp = "2026-08-16 12:00",
                        previousValue = prevStatus.displayName,
                        newValue = newStatus.displayName
                    )
                    val mod = task.copy(
                        status = newStatus,
                        progressPercent = finalProgress,
                        completedOn = newCompletedOn,
                        remarks = remark ?: task.remarks,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = "2026-08-16 12:00"
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Status Update", "${user.name} updated status to ${newStatus.displayName} (${it.progressPercent}%)")
        }
    }

    override fun updateTaskProgress(taskId: String, newProgress: Int) {
        val user = _currentUser.value
        val clamped = newProgress.coerceIn(0, 100)
        var updatedTask: TaskItem? = null
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val newStatus = when {
                        clamped == 100 -> TaskStatus.COMPLETED
                        clamped > 0 && task.status == TaskStatus.NOT_STARTED -> TaskStatus.IN_PROGRESS
                        else -> task.status
                    }
                    val newActivity = TaskActivity(
                        id = "ACT-${System.currentTimeMillis() % 10000}",
                        user = user.name,
                        action = "Updated progress to $clamped%",
                        timestamp = "2026-08-16 12:00",
                        previousValue = "${task.progressPercent}%",
                        newValue = "$clamped%"
                    )
                    val mod = task.copy(
                        progressPercent = clamped,
                        status = newStatus,
                        completedOn = if (clamped == 100) "2026-08-16" else null,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = "2026-08-16 12:00"
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Progress Update", "${user.name} set progress to $clamped%")
        }
    }

    override fun addTaskBlocker(taskId: String, blockerText: String) {
        val user = _currentUser.value
        var updatedTask: TaskItem? = null
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val newActivity = TaskActivity(
                        id = "ACT-${System.currentTimeMillis() % 10000}",
                        user = user.name,
                        action = "Added Blocker: $blockerText",
                        timestamp = "2026-08-16 12:00"
                    )
                    val mod = task.copy(
                        status = TaskStatus.BLOCKED,
                        blocker = blockerText,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = "2026-08-16 12:00"
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Blocker Added", "${user.name} flagged blocker: $blockerText")
        }
    }

    override fun clearTaskBlocker(taskId: String) {
        val user = _currentUser.value
        var updatedTask: TaskItem? = null
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val newActivity = TaskActivity(
                        id = "ACT-${System.currentTimeMillis() % 10000}",
                        user = user.name,
                        action = "Cleared blocker",
                        timestamp = "2026-08-16 12:00"
                    )
                    val mod = task.copy(
                        status = TaskStatus.IN_PROGRESS,
                        blocker = null,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = "2026-08-16 12:00"
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Blocker Cleared", "${user.name} resolved blocker")
        }
    }

    override fun addEvidenceToTask(taskId: String, attachment: EvidenceAttachment) {
        val user = _currentUser.value
        var updatedTask: TaskItem? = null
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val newActivity = TaskActivity(
                        id = "ACT-${System.currentTimeMillis() % 10000}",
                        user = user.name,
                        action = "Attached evidence: ${attachment.title}",
                        timestamp = "2026-08-16 12:00"
                    )
                    val mod = task.copy(
                        evidenceList = task.evidenceList + attachment,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = "2026-08-16 12:00"
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Evidence Upload", "${user.name} uploaded evidence: ${attachment.title}")
        }
    }

    override fun createOrUpdateTask(task: TaskItem) {
        val user = _currentUser.value
        val exists = _tasks.value.any { it.id == task.id }
        if (exists) {
            _tasks.update { list -> list.map { if (it.id == task.id) task else it } }
            db?.let { repositoryScope.launch { it.taskDao().insertOrUpdate(task.toEntity()) } }
            firestoreManager.syncTask(task)
            addAudit("Task", task.id, "Edited Task", "${user.name} updated task details: ${task.title}")
        } else {
            val nextNum = _tasks.value.size + 1
            val generatedId = if (task.id.isBlank() || task.id.startsWith("NEW")) "TASK-${String.format("%04d", nextNum)}" else task.id
            val newTask = task.copy(
                id = generatedId,
                assignedById = user.id,
                assignedByName = user.name,
                dateAssigned = "2026-08-16",
                lastUpdated = "2026-08-16 12:00",
                activityHistory = listOf(
                    TaskActivity("ACT-${System.currentTimeMillis() % 10000}", user.name, "Created task", "2026-08-16 12:00")
                )
            )
            _tasks.update { listOf(newTask) + it }
            db?.let { repositoryScope.launch { it.taskDao().insertOrUpdate(newTask.toEntity()) } }
            firestoreManager.syncTask(newTask)
            addAudit("Task", generatedId, "Created Task", "${user.name} created task: ${newTask.title}")
        }
    }

    override fun reassignTask(taskId: String, newAssigneeId: String) {
        val user = _currentUser.value
        val member = _teamMembers.value.find { it.id == newAssigneeId } ?: return
        var updatedTask: TaskItem? = null
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val prev = task.teamMemberName
                    val newActivity = TaskActivity(
                        id = "ACT-${System.currentTimeMillis() % 10000}",
                        user = user.name,
                        action = "Reassigned task from $prev to ${member.name}",
                        timestamp = "2026-08-16 12:00"
                    )
                    val mod = task.copy(
                        teamMemberId = member.id,
                        teamMemberName = member.name,
                        vertical = member.vertical,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = "2026-08-16 12:00"
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Reassigned", "${user.name} reassigned task to ${member.name}")
        }
    }

    override fun updateEventStage(eventId: String, newStage: EventStage) {
        val user = _currentUser.value
        var updatedEvent: SportsEvent? = null
        _events.update { list ->
            list.map { event ->
                if (event.id == eventId) {
                    val mod = event.copy(currentStage = newStage)
                    updatedEvent = mod
                    mod
                } else event
            }
        }
        updatedEvent?.let {
            db?.let { d -> repositoryScope.launch { d.eventDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncEvent(it)
            addAudit("Event", eventId, "Stage Changed", "${user.name} transitioned stage to ${newStage.displayName}")
        }
    }

    override fun createOrUpdateEvent(event: SportsEvent) {
        val user = _currentUser.value
        val exists = _events.value.any { it.id == event.id }
        if (exists) {
            _events.update { list -> list.map { if (it.id == event.id) event else it } }
            db?.let { repositoryScope.launch { it.eventDao().insertOrUpdate(event.toEntity()) } }
            firestoreManager.syncEvent(event)
            addAudit("Event", event.id, "Updated Event", "${user.name} modified event: ${event.name}")
        } else {
            val nextNum = _events.value.size + 1
            val generatedId = if (event.id.isBlank() || event.id.startsWith("NEW")) "E${String.format("%02d", nextNum)}" else event.id
            val newEvent = event.copy(id = generatedId)
            _events.update { it + newEvent }
            val newReqs = SportsOpsSeedData.generateRequirementsForEvent(generatedId)
            _readinessRequirements.update { it + newReqs }

            db?.let {
                repositoryScope.launch {
                    it.eventDao().insertOrUpdate(newEvent.toEntity())
                    it.readinessDao().insertAll(newReqs.map { r -> r.toEntity() })
                }
            }
            firestoreManager.syncEvent(newEvent)
            newReqs.forEach { firestoreManager.syncRequirement(it) }

            addAudit("Event", generatedId, "Created Event", "${user.name} created event: ${newEvent.name}")
        }
    }

    override fun updateRequirementStatus(
        reqId: String,
        pocState: RequirementResponsibilityState?,
        coordState: RequirementResponsibilityState?,
        coreState: RequirementResponsibilityState?,
        notes: String?
    ) {
        val user = _currentUser.value
        var targetEventId = ""
        var updatedReq: EventReadinessRequirement? = null
        _readinessRequirements.update { list ->
            list.map { req ->
                if (req.id == reqId) {
                    targetEventId = req.eventId
                    val mod = req.copy(
                        pocStatus = pocState ?: req.pocStatus,
                        coordinatorStatus = coordState ?: req.coordinatorStatus,
                        coreStatus = coreState ?: req.coreStatus,
                        notes = notes ?: req.notes,
                        lastUpdated = "2026-08-16 12:00"
                    )
                    updatedReq = mod
                    mod
                } else req
            }
        }

        updatedReq?.let {
            db?.let { d -> repositoryScope.launch { d.readinessDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncRequirement(it)
        }

        // Recalculate event readiness
        if (targetEventId.isNotBlank()) {
            val allReqs = _readinessRequirements.value
            val summary = SportsOpsLogic.calculateEventReadiness(targetEventId, allReqs)
            _events.update { list ->
                list.map { event ->
                    if (event.id == targetEventId) {
                        val mod = event.copy(readinessPercent = summary.overallPercent)
                        db?.let { d -> repositoryScope.launch { d.eventDao().insertOrUpdate(mod.toEntity()) } }
                        firestoreManager.syncEvent(mod)
                        mod
                    } else event
                }
            }
        }
        addAudit("Readiness", reqId, "Requirement Updated", "${user.name} updated requirement $reqId")
    }

    override fun createIssue(issue: IssueItem) {
        val user = _currentUser.value
        val nextNum = _issues.value.size + 1
        val generatedId = if (issue.id.isBlank() || issue.id.startsWith("NEW")) "ISS-${String.format("%03d", nextNum)}" else issue.id
        val newIssue = issue.copy(
            id = generatedId,
            raisedById = user.id,
            raisedByName = user.name,
            dateRaised = "2026-08-16",
            lastUpdated = "2026-08-16 12:00"
        )
        _issues.update { listOf(newIssue) + it }
        db?.let { repositoryScope.launch { it.issueDao().insertOrUpdate(newIssue.toEntity()) } }
        firestoreManager.syncIssue(newIssue)
        addAudit("Issue", generatedId, "Created Issue", "${user.name} logged issue: ${newIssue.problem} (${newIssue.severity.displayName})")
    }

    override fun updateIssueStatus(issueId: String, newStatus: IssueStatus, resolution: String?) {
        val user = _currentUser.value
        var updatedIssue: IssueItem? = null
        _issues.update { list ->
            list.map { issue ->
                if (issue.id == issueId) {
                    val mod = issue.copy(
                        status = newStatus,
                        resolution = resolution ?: issue.resolution,
                        resolutionDate = if (newStatus == IssueStatus.RESOLVED || newStatus == IssueStatus.CLOSED) "2026-08-16" else issue.resolutionDate,
                        lastUpdated = "2026-08-16 12:00"
                    )
                    updatedIssue = mod
                    mod
                } else issue
            }
        }
        updatedIssue?.let {
            db?.let { d -> repositoryScope.launch { d.issueDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncIssue(it)
            addAudit("Issue", issueId, "Status Update", "${user.name} marked issue $issueId as ${newStatus.displayName}")
        }
    }

    override fun escalateIssue(issueId: String, targetUserId: String, reason: String) {
        val user = _currentUser.value
        val target = _teamMembers.value.find { it.id == targetUserId } ?: return
        var updatedIssue: IssueItem? = null
        _issues.update { list ->
            list.map { issue ->
                if (issue.id == issueId) {
                    val step = EscalationHistoryEntry(
                        fromUser = user.name,
                        toUser = target.name,
                        timestamp = "2026-08-16 12:00",
                        reason = reason
                    )
                    val mod = issue.copy(
                        escalatedToId = target.id,
                        escalatedToName = target.name,
                        status = IssueStatus.UNDER_REVIEW,
                        escalationHistory = issue.escalationHistory + step,
                        lastUpdated = "2026-08-16 12:00"
                    )
                    updatedIssue = mod
                    mod
                } else issue
            }
        }
        updatedIssue?.let {
            db?.let { d -> repositoryScope.launch { d.issueDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncIssue(it)
            addAudit("Issue", issueId, "Escalated", "${user.name} escalated $issueId to ${target.name}: $reason")
        }
    }

    override fun addEvidenceToIssue(issueId: String, attachment: EvidenceAttachment) {
        val user = _currentUser.value
        var updatedIssue: IssueItem? = null
        _issues.update { list ->
            list.map { issue ->
                if (issue.id == issueId) {
                    val mod = issue.copy(
                        evidenceList = issue.evidenceList + attachment,
                        lastUpdated = "2026-08-16 12:00"
                    )
                    updatedIssue = mod
                    mod
                } else issue
            }
        }
        updatedIssue?.let {
            db?.let { d -> repositoryScope.launch { d.issueDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncIssue(it)
            addAudit("Issue", issueId, "Evidence Added", "${user.name} attached evidence to $issueId: ${attachment.title}")
        }
    }

    override fun submitProposalReview(review: ProposalReview) {
        val user = _currentUser.value
        val submitted = review.copy(
            isSubmitted = true,
            submittedAt = "2026-08-16 12:00"
        )
        _proposalReviews.update { list ->
            val filtered = list.filterNot { it.id == review.id }
            filtered + submitted
        }
        db?.let { repositoryScope.launch { it.proposalReviewDao().insertOrUpdate(submitted.toEntity()) } }
        firestoreManager.syncProposalReview(submitted)
        addAudit("Proposal Review", review.eventId, "Review Submitted", "${user.name} submitted evaluation score ${review.totalScore}/100 for ${review.eventTitle}")
    }

    override fun handleApprovalAction(approvalId: String, action: CoreApprovalStatus, remark: String) {
        val user = _currentUser.value
        var updatedApp: ApprovalItem? = null
        _approvals.update { list ->
            list.map { app ->
                if (app.id == approvalId) {
                    val mod = app.copy(
                        status = action,
                        remarks = remark,
                        decidedBy = user.name,
                        decidedAt = "2026-08-16 12:00"
                    )
                    updatedApp = mod
                    mod
                } else app
            }
        }
        updatedApp?.let {
            db?.let { d -> repositoryScope.launch { d.approvalDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncApproval(it)
            addAudit("Approval", approvalId, "Decision Rendered", "${user.name} marked approval as ${action.displayName}: $remark")
        }
    }

    override fun addCalendarItem(item: CalendarItem) {
        val user = _currentUser.value
        val nextNum = _calendarItems.value.size + 1
        val genId = if (item.id.isBlank()) "CAL-${String.format("%03d", nextNum)}" else item.id
        val newItem = item.copy(id = genId)
        _calendarItems.update { it + newItem }
        db?.let { repositoryScope.launch { it.calendarDao().insertOrUpdate(newItem.toEntity()) } }
        firestoreManager.syncCalendarItem(newItem)
        addAudit("Calendar", genId, "Created Milestone", "${user.name} added calendar item: ${newItem.activity}")
    }

    override fun markNotificationAsRead(notificationId: String) {
        _notifications.update { list ->
            list.map { if (it.id == notificationId) it.copy(isRead = true) else it }
        }
    }

    override fun markAllNotificationsAsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    override fun queryAiAssistant(prompt: String): String {
        val reqs = _readinessRequirements.value
        val evs = _events.value
        val readinessMap = evs.associate { it.id to SportsOpsLogic.calculateEventReadiness(it.id, reqs) }
        return SportsOpsLogic.answerOpsAssistantQuery(
            query = prompt,
            tasks = _tasks.value,
            events = evs,
            issues = _issues.value,
            calendar = _calendarItems.value,
            team = _teamMembers.value,
            readinessSummaries = readinessMap
        )
    }

    override suspend fun syncAllToFirestore(): Result<Int> {
        return firestoreManager.seedAllToFirestore(
            tasks = _tasks.value,
            events = _events.value,
            readiness = _readinessRequirements.value,
            issues = _issues.value,
            calendar = _calendarItems.value,
            approvals = _approvals.value,
            proposals = _proposalReviews.value,
            auditLogs = _auditLogs.value
        )
    }
}
