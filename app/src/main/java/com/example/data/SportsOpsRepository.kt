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
    val isAuthenticated: StateFlow<Boolean>

    fun login(user: CurrentUser)
    fun loginWithCustomProfile(name: String, email: String, role: UserRole, vertical: String, phone: String)
    fun registerNewTeamMember(member: TeamMember)
    fun logout()
    fun switchUser(userId: String)
    
    fun updateTaskStatus(taskId: String, newStatus: TaskStatus, progress: Int? = null, remark: String? = null)
    fun updateTaskProgress(taskId: String, newProgress: Int)
    fun addTaskBlocker(taskId: String, blockerText: String)
    fun clearTaskBlocker(taskId: String)
    fun addEvidenceToTask(taskId: String, attachment: EvidenceAttachment)
    fun createOrUpdateTask(task: TaskItem)
    fun deleteTask(taskId: String)
    fun reassignTask(taskId: String, newAssigneeId: String)
    
    fun updateEventStage(eventId: String, newStage: EventStage)
    fun createOrUpdateEvent(event: SportsEvent)
    fun deleteEvent(eventId: String)

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
    fun deleteIssue(issueId: String)

    fun submitProposalReview(review: ProposalReview)

    fun handleApprovalAction(approvalId: String, action: CoreApprovalStatus, remark: String)
    fun deleteApproval(approvalId: String)

    fun addCalendarItem(item: CalendarItem)
    fun deleteCalendarItem(calendarId: String)

    fun markNotificationAsRead(notificationId: String)
    fun markAllNotificationsAsRead()

    fun queryAiAssistant(prompt: String): String

    fun clearAllData()
    suspend fun seedOperationalFrameworkToFirestore(): Result<Int>
    suspend fun syncAllToFirestore(): Result<Int>
}

class SportsOpsRepositoryImpl(
    private val context: Context? = com.example.SportsOpsApp.applicationContextOrNull
) : SportsOpsRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val db: AppDatabase? = context?.let { AppDatabase.getDatabase(it) }
    private val firestoreManager = FirestoreSyncManager(repositoryScope)

    private val prefs = context?.getSharedPreferences("sports_ops_prefs", Context.MODE_PRIVATE)
    private val _isAuthenticated = MutableStateFlow(prefs?.getBoolean("is_authenticated", false) ?: false)
    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow(
        prefs?.getString("logged_user_id", null)?.let { savedId ->
            SportsOpsSeedData.users.find { it.id == savedId }
        } ?: SportsOpsSeedData.users[0]
    )
    override val currentUser: StateFlow<CurrentUser> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow(SportsOpsSeedData.users)
    override val allUsers: StateFlow<List<CurrentUser>> = _allUsers.asStateFlow()

    private val _teamMembers = MutableStateFlow(SportsOpsSeedData.teamMembers)
    override val teamMembers: StateFlow<List<TeamMember>> = _teamMembers.asStateFlow()

    // Real collections - start empty, populated directly by Firestore and SQLite cache
    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    override val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _events = MutableStateFlow<List<SportsEvent>>(emptyList())
    override val events: StateFlow<List<SportsEvent>> = _events.asStateFlow()

    private val _readinessRequirements = MutableStateFlow<List<EventReadinessRequirement>>(emptyList())
    override val readinessRequirements: StateFlow<List<EventReadinessRequirement>> = _readinessRequirements.asStateFlow()

    private val _issues = MutableStateFlow<List<IssueItem>>(emptyList())
    override val issues: StateFlow<List<IssueItem>> = _issues.asStateFlow()

    private val _calendarItems = MutableStateFlow<List<CalendarItem>>(emptyList())
    override val calendarItems: StateFlow<List<CalendarItem>> = _calendarItems.asStateFlow()

    private val _proposalReviews = MutableStateFlow<List<ProposalReview>>(emptyList())
    override val proposalReviews: StateFlow<List<ProposalReview>> = _proposalReviews.asStateFlow()

    private val _rubricCriteria = MutableStateFlow(SportsOpsSeedData.proposalRubricCriteria)
    override val rubricCriteria: StateFlow<List<ReviewCriterion>> = _rubricCriteria.asStateFlow()

    private val _approvals = MutableStateFlow<List<ApprovalItem>>(emptyList())
    override val approvals: StateFlow<List<ApprovalItem>> = _approvals.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    override val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    override val cloudSyncSummary: StateFlow<CloudSyncSummary> = firestoreManager.syncSummary

    init {
        // Observe local SQLite Database for immediate offline caching
        if (db != null) {
            repositoryScope.launch {
                launch { db.taskDao().getAllTasks().collect { list -> _tasks.value = list.map { it.toDomain() } } }
                launch { db.eventDao().getAllEvents().collect { list -> _events.value = list.map { it.toDomain() } } }
                launch { db.readinessDao().getAllRequirements().collect { list -> _readinessRequirements.value = list.map { it.toDomain() } } }
                launch { db.issueDao().getAllIssues().collect { list -> _issues.value = list.map { it.toDomain() } } }
                launch { db.calendarDao().getAllCalendarItems().collect { list -> _calendarItems.value = list.map { it.toDomain() } } }
                launch { db.approvalDao().getAllApprovals().collect { list -> _approvals.value = list.map { it.toDomain() } } }
                launch { db.proposalReviewDao().getAllReviews().collect { list -> _proposalReviews.value = list.map { it.toDomain() } } }
                launch { db.auditLogDao().getAllLogs().collect { list -> _auditLogs.value = list.map { it.toDomain() } } }
            }
        }

        // Setup live real-time listeners directly to Firebase Firestore Collections
        firestoreManager.setupRealtimeListeners(
            onTasksUpdated = { remoteTasks ->
                _tasks.value = remoteTasks
                db?.let { repositoryScope.launch { it.taskDao().insertAll(remoteTasks.map { t -> t.toEntity() }) } }
            },
            onEventsUpdated = { remoteEvents ->
                _events.value = remoteEvents
                db?.let { repositoryScope.launch { it.eventDao().insertAll(remoteEvents.map { e -> e.toEntity() }) } }
            },
            onReadinessUpdated = { remoteReqs ->
                _readinessRequirements.value = remoteReqs
                db?.let { repositoryScope.launch { it.readinessDao().insertAll(remoteReqs.map { r -> r.toEntity() }) } }
            },
            onIssuesUpdated = { remoteIssues ->
                _issues.value = remoteIssues
                db?.let { repositoryScope.launch { it.issueDao().insertAll(remoteIssues.map { i -> i.toEntity() }) } }
            },
            onCalendarUpdated = { remoteCal ->
                _calendarItems.value = remoteCal
                db?.let { repositoryScope.launch { it.calendarDao().insertAll(remoteCal.map { c -> c.toEntity() }) } }
            },
            onApprovalsUpdated = { remoteApps ->
                _approvals.value = remoteApps
                db?.let { repositoryScope.launch { it.approvalDao().insertAll(remoteApps.map { a -> a.toEntity() }) } }
            },
            onReviewsUpdated = { remoteRev ->
                _proposalReviews.value = remoteRev
                db?.let { repositoryScope.launch { it.proposalReviewDao().insertAll(remoteRev.map { p -> p.toEntity() }) } }
            },
            onAuditLogsUpdated = { remoteLogs ->
                _auditLogs.value = remoteLogs
                db?.let { repositoryScope.launch { it.auditLogDao().insertAll(remoteLogs.map { l -> l.toEntity() }) } }
            }
        )
    }

    private fun addAudit(objectType: String, objectId: String, action: String, details: String) {
        val user = _currentUser.value
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val entry = AuditLogEntry(
            id = "LOG-${System.currentTimeMillis() % 100000}",
            user = user.name,
            userRole = user.role,
            timestamp = now,
            objectType = objectType,
            objectId = objectId,
            action = action,
            details = details
        )
        _auditLogs.update { listOf(entry) + it }
        db?.let { repositoryScope.launch { it.auditLogDao().insert(entry.toEntity()) } }
        firestoreManager.syncAuditLog(entry)
    }

    override fun login(user: CurrentUser) {
        _currentUser.value = user
        _isAuthenticated.value = true
        prefs?.edit()?.putBoolean("is_authenticated", true)?.putString("logged_user_id", user.id)?.apply()
        addAudit("Auth Session", user.id, "Logged In", "User ${user.name} (${user.role.displayName}) authenticated into operations platform.")
    }

    override fun loginWithCustomProfile(name: String, email: String, role: UserRole, vertical: String, phone: String) {
        val newUserId = "USER-${System.currentTimeMillis() % 100000}"
        val colors = listOf(0xFF1E3A8AL, 0xFF0D9488L, 0xFF7C3AEDL, 0xFFB45309L, 0xFFBE123CL, 0xFF047857L, 0xFF2563EBL)
        val customUser = CurrentUser(
            id = newUserId,
            name = name,
            email = email,
            role = role,
            vertical = vertical,
            avatarColor = colors.random()
        )
        _allUsers.update { it + customUser }
        val member = TeamMember(
            id = newUserId,
            name = name,
            email = email,
            phone = phone,
            role = role,
            vertical = vertical,
            avatarColor = customUser.avatarColor
        )
        _teamMembers.update { it + member }
        firestoreManager.syncTeamMember(member)
        login(customUser)
    }

    override fun registerNewTeamMember(member: TeamMember) {
        _teamMembers.update { it + member }
        val newUser = CurrentUser(
            id = member.id,
            name = member.name,
            email = member.email,
            role = member.role,
            vertical = member.vertical,
            avatarColor = member.avatarColor
        )
        _allUsers.update { it + newUser }
        firestoreManager.syncTeamMember(member)
        addAudit("Team Management", member.id, "New Member Added", "Added ${member.name} (${member.role.displayName}) to ${member.vertical}")
    }

    override fun logout() {
        _isAuthenticated.value = false
        prefs?.edit()?.putBoolean("is_authenticated", false)?.apply()
        addAudit("Auth Session", _currentUser.value.id, "Logged Out", "User ${_currentUser.value.name} logged out of workspace.")
    }

    override fun switchUser(userId: String) {
        val found = _allUsers.value.find { it.id == userId }
        if (found != null) {
            _currentUser.value = found
            prefs?.edit()?.putString("logged_user_id", found.id)?.apply()
            addAudit("User Session", userId, "Switched Role", "Switched active workspace session to ${found.name} (${found.role.displayName})")
        }
    }

    override fun updateTaskStatus(taskId: String, newStatus: TaskStatus, progress: Int?, remark: String?) {
        val user = _currentUser.value
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
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
                    val newCompletedOn = if (newStatus == TaskStatus.COMPLETED) now.substringBefore(" ") else null
                    val newActivity = TaskActivity(
                        id = "ACT-${System.currentTimeMillis() % 10000}",
                        user = user.name,
                        action = "Changed status from ${prevStatus.displayName} to ${newStatus.displayName}",
                        timestamp = now,
                        previousValue = prevStatus.displayName,
                        newValue = newStatus.displayName
                    )
                    val mod = task.copy(
                        status = newStatus,
                        progressPercent = finalProgress,
                        completedOn = newCompletedOn,
                        remarks = remark ?: task.remarks,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = now
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
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
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
                        timestamp = now,
                        previousValue = "${task.progressPercent}%",
                        newValue = "$clamped%"
                    )
                    val mod = task.copy(
                        progressPercent = clamped,
                        status = newStatus,
                        completedOn = if (clamped == 100) now.substringBefore(" ") else task.completedOn,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = now
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Progress Update", "${user.name} updated progress to $clamped%")
        }
    }

    override fun addTaskBlocker(taskId: String, blockerText: String) {
        val user = _currentUser.value
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        var updatedTask: TaskItem? = null
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val newActivity = TaskActivity(
                        id = "ACT-${System.currentTimeMillis() % 10000}",
                        user = user.name,
                        action = "Reported Blocker: $blockerText",
                        timestamp = now
                    )
                    val mod = task.copy(
                        blocker = blockerText,
                        status = TaskStatus.BLOCKED,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = now
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Blocker Reported", "Critical blocker logged by ${user.name}: $blockerText")
        }
    }

    override fun clearTaskBlocker(taskId: String) {
        val user = _currentUser.value
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        var updatedTask: TaskItem? = null
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val newActivity = TaskActivity(
                        id = "ACT-${System.currentTimeMillis() % 10000}",
                        user = user.name,
                        action = "Cleared Blocker: ${task.blocker ?: ""}",
                        timestamp = now
                    )
                    val mod = task.copy(
                        blocker = null,
                        status = TaskStatus.IN_PROGRESS,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = now
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Blocker Cleared", "Blocker resolved by ${user.name}")
        }
    }

    override fun addEvidenceToTask(taskId: String, attachment: EvidenceAttachment) {
        val user = _currentUser.value
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        var updatedTask: TaskItem? = null
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val mod = task.copy(
                        evidenceList = task.evidenceList + attachment,
                        lastUpdated = now
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Evidence Attached", "${user.name} uploaded ${attachment.title}")
        }
    }

    override fun createOrUpdateTask(task: TaskItem) {
        val user = _currentUser.value
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val taskWithId = if (task.id.isBlank()) {
            val nextNum = _tasks.value.size + 1
            task.copy(id = "TASK-${String.format("%03d", nextNum)}", lastUpdated = now)
        } else {
            task.copy(lastUpdated = now)
        }

        _tasks.update { list ->
            val exists = list.any { it.id == taskWithId.id }
            if (exists) list.map { if (it.id == taskWithId.id) taskWithId else it }
            else listOf(taskWithId) + list
        }

        db?.let { repositoryScope.launch { it.taskDao().insertOrUpdate(taskWithId.toEntity()) } }
        firestoreManager.syncTask(taskWithId)
        addAudit("Task", taskWithId.id, "Saved Task", "${user.name} saved task: ${taskWithId.title}")
    }

    override fun deleteTask(taskId: String) {
        val user = _currentUser.value
        _tasks.update { list -> list.filterNot { it.id == taskId } }
        db?.let { repositoryScope.launch { it.taskDao().deleteById(taskId) } }
        firestoreManager.deleteTask(taskId)
        addAudit("Task", taskId, "Deleted Task", "${user.name} deleted task $taskId")
    }

    override fun reassignTask(taskId: String, newAssigneeId: String) {
        val user = _currentUser.value
        val targetUser = _allUsers.value.find { it.id == newAssigneeId }
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        var updatedTask: TaskItem? = null
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val prevName = task.teamMemberName
                    val newName = targetUser?.name ?: newAssigneeId
                    val newActivity = TaskActivity(
                        id = "ACT-${System.currentTimeMillis() % 10000}",
                        user = user.name,
                        action = "Reassigned from $prevName to $newName",
                        timestamp = now,
                        previousValue = prevName,
                        newValue = newName
                    )
                    val mod = task.copy(
                        teamMemberId = newAssigneeId,
                        teamMemberName = newName,
                        activityHistory = task.activityHistory + newActivity,
                        lastUpdated = now
                    )
                    updatedTask = mod
                    mod
                } else task
            }
        }
        updatedTask?.let {
            db?.let { d -> repositoryScope.launch { d.taskDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncTask(it)
            addAudit("Task", taskId, "Reassigned", "${user.name} reassigned to ${targetUser?.name ?: newAssigneeId}")
        }
    }

    override fun updateEventStage(eventId: String, newStage: EventStage) {
        val user = _currentUser.value
        var updatedEvent: SportsEvent? = null
        _events.update { list ->
            list.map { ev ->
                if (ev.id == eventId) {
                    val mod = ev.copy(currentStage = newStage)
                    updatedEvent = mod
                    mod
                } else ev
            }
        }
        updatedEvent?.let {
            db?.let { d -> repositoryScope.launch { d.eventDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncEvent(it)
            addAudit("Event", eventId, "Stage Transition", "${user.name} advanced stage to ${newStage.order}: ${newStage.displayName}")
        }
    }

    override fun createOrUpdateEvent(event: SportsEvent) {
        val user = _currentUser.value
        val eventWithId = if (event.id.isBlank()) {
            val nextNum = _events.value.size + 1
            event.copy(id = "EVT-${String.format("%03d", nextNum)}")
        } else event

        _events.update { list ->
            val exists = list.any { it.id == eventWithId.id }
            if (exists) list.map { if (it.id == eventWithId.id) eventWithId else it }
            else listOf(eventWithId) + list
        }
        db?.let { repositoryScope.launch { it.eventDao().insertOrUpdate(eventWithId.toEntity()) } }
        firestoreManager.syncEvent(eventWithId)
        addAudit("Event", eventWithId.id, "Saved Event", "${user.name} updated event details for ${eventWithId.name}")
    }

    override fun deleteEvent(eventId: String) {
        val user = _currentUser.value
        _events.update { list -> list.filterNot { it.id == eventId } }
        db?.let { repositoryScope.launch { it.eventDao().deleteById(eventId) } }
        firestoreManager.deleteEvent(eventId)
        addAudit("Event", eventId, "Deleted Event", "${user.name} deleted event $eventId")
    }

    override fun updateRequirementStatus(
        reqId: String,
        pocState: RequirementResponsibilityState?,
        coordState: RequirementResponsibilityState?,
        coreState: RequirementResponsibilityState?,
        notes: String?
    ) {
        val user = _currentUser.value
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        var updatedReq: EventReadinessRequirement? = null
        _readinessRequirements.update { list ->
            list.map { req ->
                if (req.id == reqId) {
                    val mod = req.copy(
                        pocStatus = pocState ?: req.pocStatus,
                        coordinatorStatus = coordState ?: req.coordinatorStatus,
                        coreStatus = coreState ?: req.coreStatus,
                        notes = notes ?: req.notes,
                        lastUpdated = now
                    )
                    updatedReq = mod
                    mod
                } else req
            }
        }
        updatedReq?.let {
            db?.let { d -> repositoryScope.launch { d.readinessDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncRequirement(it)
            addAudit("Readiness", reqId, "Requirement Sign-off", "${user.name} updated readiness status for ${it.title}")
        }
    }

    override fun createIssue(issue: IssueItem) {
        val user = _currentUser.value
        val nextNum = _issues.value.size + 1
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val newIssue = if (issue.id.isBlank()) issue.copy(id = "ISSUE-${String.format("%03d", nextNum)}", lastUpdated = now) else issue.copy(lastUpdated = now)
        _issues.update { listOf(newIssue) + it }
        db?.let { repositoryScope.launch { it.issueDao().insertOrUpdate(newIssue.toEntity()) } }
        firestoreManager.syncIssue(newIssue)
        addAudit("Issue", newIssue.id, "Logged Issue", "${user.name} logged ${newIssue.severity.displayName} issue: ${newIssue.problem}")
    }

    override fun updateIssueStatus(issueId: String, newStatus: IssueStatus, resolution: String?) {
        val user = _currentUser.value
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        var updatedIssue: IssueItem? = null
        _issues.update { list ->
            list.map { issue ->
                if (issue.id == issueId) {
                    val mod = issue.copy(
                        status = newStatus,
                        resolution = resolution ?: issue.resolution,
                        resolutionDate = if (newStatus == IssueStatus.RESOLVED || newStatus == IssueStatus.CLOSED) now.substringBefore(" ") else issue.resolutionDate,
                        lastUpdated = now
                    )
                    updatedIssue = mod
                    mod
                } else issue
            }
        }
        updatedIssue?.let {
            db?.let { d -> repositoryScope.launch { d.issueDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncIssue(it)
            addAudit("Issue", issueId, "Status Update", "${user.name} marked issue as ${newStatus.displayName}")
        }
    }

    override fun escalateIssue(issueId: String, targetUserId: String, reason: String) {
        val user = _currentUser.value
        val target = _allUsers.value.find { it.id == targetUserId }
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        var updatedIssue: IssueItem? = null
        _issues.update { list ->
            list.map { issue ->
                if (issue.id == issueId) {
                    val nextLevel = when (issue.escalationLevel) {
                        EscalationLevel.L1_VOLUNTEER_COORDINATOR -> EscalationLevel.L2_SUPER_COORDINATOR
                        EscalationLevel.L2_SUPER_COORDINATOR -> EscalationLevel.L3_DEPUTY_CORE
                        EscalationLevel.L3_DEPUTY_CORE -> EscalationLevel.L4_CORE
                        EscalationLevel.L4_CORE -> EscalationLevel.L4_CORE
                    }
                    val mod = issue.copy(
                        escalationLevel = nextLevel,
                        escalatedToId = targetUserId,
                        escalatedToName = target?.name ?: targetUserId,
                        remarks = "${issue.remarks}\n[${now}] Escalated by ${user.name}: $reason",
                        lastUpdated = now
                    )
                    updatedIssue = mod
                    mod
                } else issue
            }
        }
        updatedIssue?.let {
            db?.let { d -> repositoryScope.launch { d.issueDao().insertOrUpdate(it.toEntity()) } }
            firestoreManager.syncIssue(it)
            addAudit("Issue", issueId, "Escalation", "Escalated to ${target?.name ?: targetUserId} (${it.escalationLevel.displayName}): $reason")
        }
    }

    override fun addEvidenceToIssue(issueId: String, attachment: EvidenceAttachment) {
        val user = _currentUser.value
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        var updatedIssue: IssueItem? = null
        _issues.update { list ->
            list.map { issue ->
                if (issue.id == issueId) {
                    val mod = issue.copy(
                        evidenceList = issue.evidenceList + attachment,
                        lastUpdated = now
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

    override fun deleteIssue(issueId: String) {
        val user = _currentUser.value
        _issues.update { list -> list.filterNot { it.id == issueId } }
        db?.let { repositoryScope.launch { it.issueDao().deleteById(issueId) } }
        firestoreManager.deleteIssue(issueId)
        addAudit("Issue", issueId, "Deleted Issue", "${user.name} deleted issue $issueId")
    }

    override fun submitProposalReview(review: ProposalReview) {
        val user = _currentUser.value
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val submitted = review.copy(
            isSubmitted = true,
            submittedAt = now
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
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        var updatedApp: ApprovalItem? = null
        _approvals.update { list ->
            list.map { app ->
                if (app.id == approvalId) {
                    val mod = app.copy(
                        status = action,
                        remarks = remark,
                        decidedBy = user.name,
                        decidedAt = now
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

    override fun deleteApproval(approvalId: String) {
        val user = _currentUser.value
        _approvals.update { list -> list.filterNot { it.id == approvalId } }
        db?.let { repositoryScope.launch { it.approvalDao().deleteById(approvalId) } }
        addAudit("Approval", approvalId, "Deleted Approval", "${user.name} deleted approval item $approvalId")
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

    override fun deleteCalendarItem(calendarId: String) {
        val user = _currentUser.value
        _calendarItems.update { list -> list.filterNot { it.id == calendarId } }
        db?.let { repositoryScope.launch { it.calendarDao().deleteById(calendarId) } }
        firestoreManager.deleteCalendarItem(calendarId)
        addAudit("Calendar", calendarId, "Deleted Milestone", "${user.name} deleted calendar item $calendarId")
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

    override fun clearAllData() {
        _tasks.value = emptyList()
        _events.value = emptyList()
        _readinessRequirements.value = emptyList()
        _issues.value = emptyList()
        _calendarItems.value = emptyList()
        _approvals.value = emptyList()
        _proposalReviews.value = emptyList()
        _auditLogs.value = emptyList()

        db?.let {
            repositoryScope.launch {
                it.taskDao().deleteAll()
                it.eventDao().deleteAll()
                it.readinessDao().deleteAll()
                it.issueDao().deleteAll()
                it.calendarDao().deleteAll()
                it.approvalDao().deleteAll()
                it.proposalReviewDao().deleteAll()
                it.auditLogDao().deleteAll()
            }
        }
    }

    override suspend fun seedOperationalFrameworkToFirestore(): Result<Int> {
        return firestoreManager.seedAllToFirestore(
            tasks = SportsOpsSeedData.tasks,
            events = SportsOpsSeedData.events,
            readiness = SportsOpsSeedData.readinessRequirements,
            issues = SportsOpsSeedData.issues,
            calendar = SportsOpsSeedData.calendarItems,
            approvals = SportsOpsSeedData.approvals,
            proposals = SportsOpsSeedData.proposalReviews,
            auditLogs = SportsOpsSeedData.auditLogs
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
