package com.example.data.firebase

import android.util.Log
import com.example.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class CloudConnectionStatus(val label: String, val isOnline: Boolean) {
    ONLINE("Connected to Firestore", true),
    SYNCING("Syncing with Cloud Firestore...", true),
    OFFLINE_CACHE("Local SQLite Cache", false),
    UNCONFIGURED("Firebase Standby", false),
    ERROR("Connection Warning", false)
}

data class CloudSyncSummary(
    val status: CloudConnectionStatus = CloudConnectionStatus.ONLINE,
    val lastSyncTime: String = "Live Firestore Listening",
    val syncedDocumentsCount: Int = 0,
    val pendingOperations: Int = 0,
    val firestoreProject: String = "Firebase Cloud Firestore Console",
    val statusMessage: String = "Connected directly to Live Firebase Firestore"
)

class FirestoreSyncManager(
    private val coroutineScope: CoroutineScope
) {
    private val TAG = "FirestoreSyncManager"

    private var firestore: FirebaseFirestore? = null
    private val listeners = mutableListOf<ListenerRegistration>()

    private val _syncSummary = MutableStateFlow(CloudSyncSummary())
    val syncSummary: StateFlow<CloudSyncSummary> = _syncSummary.asStateFlow()

    init {
        try {
            firestore = FirebaseFirestore.getInstance()
            _syncSummary.value = _syncSummary.value.copy(
                status = CloudConnectionStatus.ONLINE,
                statusMessage = "Connected to Live Firebase Firestore Console"
            )
            Log.d(TAG, "FirebaseFirestore initialized successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore init notice: ${e.message}")
            _syncSummary.value = _syncSummary.value.copy(
                status = CloudConnectionStatus.OFFLINE_CACHE,
                statusMessage = "Local SQLite Active (Ready for Firestore connection)"
            )
        }
    }

    val isFirebaseAvailable: Boolean
        get() = firestore != null

    // Firestore Collections
    private val COLLECTION_TASKS = "sports_tasks"
    private val COLLECTION_EVENTS = "sports_events"
    private val COLLECTION_READINESS = "sports_readiness"
    private val COLLECTION_ISSUES = "sports_issues"
    private val COLLECTION_CALENDAR = "sports_calendar"
    private val COLLECTION_APPROVALS = "sports_approvals"
    private val COLLECTION_PROPOSALS = "sports_proposals"
    private val COLLECTION_AUDIT_LOGS = "sports_audit_logs"
    private val COLLECTION_TEAM = "sports_team"

    // Real-time snapshot listeners connecting directly to Firebase Console
    fun setupRealtimeListeners(
        onTasksUpdated: (List<TaskItem>) -> Unit,
        onEventsUpdated: (List<SportsEvent>) -> Unit,
        onReadinessUpdated: (List<EventReadinessRequirement>) -> Unit,
        onIssuesUpdated: (List<IssueItem>) -> Unit,
        onCalendarUpdated: (List<CalendarItem>) -> Unit,
        onApprovalsUpdated: (List<ApprovalItem>) -> Unit,
        onReviewsUpdated: (List<ProposalReview>) -> Unit,
        onAuditLogsUpdated: (List<AuditLogEntry>) -> Unit
    ) {
        val db = firestore ?: return

        try {
            // 1. Tasks Listener
            val tasksReg = db.collection(COLLECTION_TASKS).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Tasks listen error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try { docToTask(doc.data ?: return@mapNotNull null) } catch (e: Exception) { null }
                    }
                    onTasksUpdated(list)
                    updateSyncCount(snapshot.size())
                }
            }
            listeners.add(tasksReg)

            // 2. Events Listener
            val eventsReg = db.collection(COLLECTION_EVENTS).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try { docToEvent(doc.data ?: return@mapNotNull null) } catch (e: Exception) { null }
                    }
                    onEventsUpdated(list)
                }
            }
            listeners.add(eventsReg)

            // 3. Readiness Listener
            val readinessReg = db.collection(COLLECTION_READINESS).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try { docToReadiness(doc.data ?: return@mapNotNull null) } catch (e: Exception) { null }
                    }
                    onReadinessUpdated(list)
                }
            }
            listeners.add(readinessReg)

            // 4. Issues Listener
            val issuesReg = db.collection(COLLECTION_ISSUES).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try { docToIssue(doc.data ?: return@mapNotNull null) } catch (e: Exception) { null }
                    }
                    onIssuesUpdated(list)
                }
            }
            listeners.add(issuesReg)

            // 5. Calendar Listener
            val calendarReg = db.collection(COLLECTION_CALENDAR).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try { docToCalendar(doc.data ?: return@mapNotNull null) } catch (e: Exception) { null }
                    }
                    onCalendarUpdated(list)
                }
            }
            listeners.add(calendarReg)

            // 6. Approvals Listener
            val approvalsReg = db.collection(COLLECTION_APPROVALS).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try { docToApproval(doc.data ?: return@mapNotNull null) } catch (e: Exception) { null }
                    }
                    onApprovalsUpdated(list)
                }
            }
            listeners.add(approvalsReg)

            // 7. Proposals Listener
            val proposalsReg = db.collection(COLLECTION_PROPOSALS).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try { docToProposal(doc.data ?: return@mapNotNull null) } catch (e: Exception) { null }
                    }
                    onReviewsUpdated(list)
                }
            }
            listeners.add(proposalsReg)

            // 8. Audit Logs Listener
            val auditReg = db.collection(COLLECTION_AUDIT_LOGS).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try { docToAuditLog(doc.data ?: return@mapNotNull null) } catch (e: Exception) { null }
                    }
                    onAuditLogsUpdated(list)
                }
            }
            listeners.add(auditReg)

        } catch (e: Exception) {
            Log.e(TAG, "Failed setting up Firestore snapshot listeners: ${e.message}")
        }
    }

    fun removeListeners() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    // Direct Real-Time Mutation Operations to Firebase Cloud Firestore
    fun syncTask(task: TaskItem) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_TASKS).document(task.id)
                    .set(taskToMap(task), SetOptions.merge())
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error writing task to Firestore: ${e.message}")
            }
        }
    }

    fun deleteTask(taskId: String) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_TASKS).document(taskId).delete()
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting task from Firestore: ${e.message}")
            }
        }
    }

    fun syncEvent(event: SportsEvent) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_EVENTS).document(event.id)
                    .set(eventToMap(event), SetOptions.merge())
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error writing event to Firestore: ${e.message}")
            }
        }
    }

    fun deleteEvent(eventId: String) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_EVENTS).document(eventId).delete()
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting event from Firestore: ${e.message}")
            }
        }
    }

    fun syncRequirement(req: EventReadinessRequirement) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_READINESS).document(req.id)
                    .set(readinessToMap(req), SetOptions.merge())
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error writing readiness to Firestore: ${e.message}")
            }
        }
    }

    fun syncIssue(issue: IssueItem) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_ISSUES).document(issue.id)
                    .set(issueToMap(issue), SetOptions.merge())
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error writing issue to Firestore: ${e.message}")
            }
        }
    }

    fun deleteIssue(issueId: String) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_ISSUES).document(issueId).delete()
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting issue from Firestore: ${e.message}")
            }
        }
    }

    fun syncCalendarItem(item: CalendarItem) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_CALENDAR).document(item.id)
                    .set(calendarToMap(item), SetOptions.merge())
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error writing calendar to Firestore: ${e.message}")
            }
        }
    }

    fun deleteCalendarItem(calendarId: String) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_CALENDAR).document(calendarId).delete()
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting calendar item from Firestore: ${e.message}")
            }
        }
    }

    fun syncApproval(approval: ApprovalItem) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_APPROVALS).document(approval.id)
                    .set(approvalToMap(approval), SetOptions.merge())
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error writing approval to Firestore: ${e.message}")
            }
        }
    }

    fun syncProposalReview(review: ProposalReview) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_PROPOSALS).document(review.id)
                    .set(proposalToMap(review), SetOptions.merge())
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error writing proposal to Firestore: ${e.message}")
            }
        }
    }

    fun syncAuditLog(log: AuditLogEntry) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_AUDIT_LOGS).document(log.id)
                    .set(auditLogToMap(log), SetOptions.merge())
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error writing audit log to Firestore: ${e.message}")
            }
        }
    }

    fun syncTeamMember(member: TeamMember) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_TEAM).document(member.id)
                    .set(teamMemberToMap(member), SetOptions.merge())
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error writing team member to Firestore: ${e.message}")
            }
        }
    }

    // Direct push of standard operational framework to Firestore Console
    suspend fun seedAllToFirestore(
        tasks: List<TaskItem>,
        events: List<SportsEvent>,
        readiness: List<EventReadinessRequirement>,
        issues: List<IssueItem>,
        calendar: List<CalendarItem>,
        approvals: List<ApprovalItem>,
        proposals: List<ProposalReview>,
        auditLogs: List<AuditLogEntry>
    ): Result<Int> {
        val db = firestore ?: return Result.failure(Exception("Firebase Firestore is not initialized"))

        return try {
            var count = 0
            val batch = db.batch()

            tasks.forEach {
                batch.set(db.collection(COLLECTION_TASKS).document(it.id), taskToMap(it), SetOptions.merge())
                count++
            }
            events.forEach {
                batch.set(db.collection(COLLECTION_EVENTS).document(it.id), eventToMap(it), SetOptions.merge())
                count++
            }
            readiness.forEach {
                batch.set(db.collection(COLLECTION_READINESS).document(it.id), readinessToMap(it), SetOptions.merge())
                count++
            }
            issues.forEach {
                batch.set(db.collection(COLLECTION_ISSUES).document(it.id), issueToMap(it), SetOptions.merge())
                count++
            }
            calendar.forEach {
                batch.set(db.collection(COLLECTION_CALENDAR).document(it.id), calendarToMap(it), SetOptions.merge())
                count++
            }
            approvals.forEach {
                batch.set(db.collection(COLLECTION_APPROVALS).document(it.id), approvalToMap(it), SetOptions.merge())
                count++
            }
            proposals.forEach {
                batch.set(db.collection(COLLECTION_PROPOSALS).document(it.id), proposalToMap(it), SetOptions.merge())
                count++
            }
            auditLogs.forEach {
                batch.set(db.collection(COLLECTION_AUDIT_LOGS).document(it.id), auditLogToMap(it), SetOptions.merge())
                count++
            }

            batch.commit().await()

            _syncSummary.value = _syncSummary.value.copy(
                status = CloudConnectionStatus.ONLINE,
                lastSyncTime = "Just now",
                syncedDocumentsCount = count,
                statusMessage = "All $count operational items active in Firestore Console"
            )

            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing data to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun updateLastSync() {
        val currentTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _syncSummary.value = _syncSummary.value.copy(
            lastSyncTime = currentTime,
            status = CloudConnectionStatus.ONLINE
        )
    }

    private fun updateSyncCount(count: Int) {
        _syncSummary.value = _syncSummary.value.copy(
            syncedDocumentsCount = count,
            status = CloudConnectionStatus.ONLINE
        )
    }

    // Mapping Functions for Firestore Documents
    private fun taskToMap(task: TaskItem): Map<String, Any?> = mapOf(
        "id" to task.id,
        "title" to task.title,
        "description" to task.description,
        "vertical" to task.vertical,
        "teamMemberId" to task.teamMemberId,
        "teamMemberName" to task.teamMemberName,
        "taskType" to task.taskType,
        "priority" to task.priority.name,
        "assignedById" to task.assignedById,
        "assignedByName" to task.assignedByName,
        "dateAssigned" to task.dateAssigned,
        "deadline" to task.deadline,
        "completedOn" to task.completedOn,
        "status" to task.status.name,
        "progressPercent" to task.progressPercent,
        "blocker" to task.blocker,
        "remarks" to task.remarks,
        "eventId" to task.eventId,
        "eventName" to task.eventName,
        "dependencies" to task.dependencies,
        "evidenceCount" to task.evidenceList.size,
        "lastUpdated" to task.lastUpdated
    )

    private fun docToTask(m: Map<String, Any?>): TaskItem = TaskItem(
        id = m["id"] as? String ?: "",
        title = m["title"] as? String ?: "",
        description = m["description"] as? String ?: "",
        vertical = m["vertical"] as? String ?: "",
        teamMemberId = m["teamMemberId"] as? String ?: "",
        teamMemberName = m["teamMemberName"] as? String ?: "",
        taskType = m["taskType"] as? String ?: "Operational",
        priority = try { Priority.valueOf(m["priority"] as? String ?: "MEDIUM") } catch(e: Exception) { Priority.MEDIUM },
        assignedById = m["assignedById"] as? String ?: "TM-01",
        assignedByName = m["assignedByName"] as? String ?: "Core Lead",
        dateAssigned = m["dateAssigned"] as? String ?: "2026-08-16",
        deadline = m["deadline"] as? String ?: "2026-08-20",
        completedOn = m["completedOn"] as? String,
        status = try { TaskStatus.valueOf(m["status"] as? String ?: "NOT_STARTED") } catch(e: Exception) { TaskStatus.NOT_STARTED },
        progressPercent = (m["progressPercent"] as? Number)?.toInt() ?: 0,
        blocker = m["blocker"] as? String,
        remarks = m["remarks"] as? String ?: "",
        eventId = m["eventId"] as? String,
        eventName = m["eventName"] as? String,
        dependencies = (m["dependencies"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
        evidenceList = emptyList(),
        activityHistory = emptyList(),
        lastUpdated = m["lastUpdated"] as? String ?: "2026-08-16"
    )

    private fun eventToMap(e: SportsEvent): Map<String, Any?> = mapOf(
        "id" to e.id,
        "name" to e.name,
        "society" to e.society,
        "eventHead" to e.eventHead,
        "eventHeadContact" to e.eventHeadContact,
        "sportsPoc" to e.sportsPoc,
        "coordinator" to e.coordinator,
        "currentStage" to e.currentStage.name,
        "readinessPercent" to e.readinessPercent,
        "coreApproval" to e.coreApproval.name,
        "remarks" to e.remarks,
        "description" to e.description,
        "startDate" to e.startDate,
        "endDate" to e.endDate,
        "venue" to e.venue,
        "expectedParticipants" to e.expectedParticipants
    )

    private fun docToEvent(m: Map<String, Any?>): SportsEvent = SportsEvent(
        id = m["id"] as? String ?: "",
        name = m["name"] as? String ?: "",
        society = m["society"] as? String ?: "",
        eventHead = m["eventHead"] as? String ?: "",
        eventHeadContact = m["eventHeadContact"] as? String ?: "",
        sportsPoc = m["sportsPoc"] as? String ?: "",
        coordinator = m["coordinator"] as? String ?: "",
        currentStage = try { EventStage.valueOf(m["currentStage"] as? String ?: "PROPOSAL") } catch(e: Exception) { EventStage.PROPOSAL },
        readinessPercent = (m["readinessPercent"] as? Number)?.toInt() ?: 0,
        coreApproval = try { CoreApprovalStatus.valueOf(m["coreApproval"] as? String ?: "PENDING") } catch(e: Exception) { CoreApprovalStatus.PENDING },
        remarks = m["remarks"] as? String ?: "",
        description = m["description"] as? String ?: "",
        startDate = m["startDate"] as? String ?: "",
        endDate = m["endDate"] as? String ?: "",
        venue = m["venue"] as? String ?: "",
        expectedParticipants = (m["expectedParticipants"] as? Number)?.toInt() ?: 0
    )

    private fun readinessToMap(r: EventReadinessRequirement): Map<String, Any?> = mapOf(
        "id" to r.id,
        "eventId" to r.eventId,
        "phaseNumber" to r.phaseNumber,
        "phaseTitle" to r.phaseTitle,
        "title" to r.title,
        "pocStatus" to r.pocStatus.name,
        "coordinatorStatus" to r.coordinatorStatus.name,
        "coreStatus" to r.coreStatus.name,
        "deadline" to r.deadline,
        "notes" to r.notes,
        "evidenceUrl" to r.evidenceUrl,
        "lastUpdated" to r.lastUpdated
    )

    private fun docToReadiness(m: Map<String, Any?>): EventReadinessRequirement = EventReadinessRequirement(
        id = m["id"] as? String ?: "",
        eventId = m["eventId"] as? String ?: "",
        phaseNumber = (m["phaseNumber"] as? Number)?.toInt() ?: 1,
        phaseTitle = m["phaseTitle"] as? String ?: "",
        title = m["title"] as? String ?: "",
        pocStatus = try { RequirementResponsibilityState.valueOf(m["pocStatus"] as? String ?: "PENDING") } catch(e: Exception) { RequirementResponsibilityState.PENDING },
        coordinatorStatus = try { RequirementResponsibilityState.valueOf(m["coordinatorStatus"] as? String ?: "PENDING") } catch(e: Exception) { RequirementResponsibilityState.PENDING },
        coreStatus = try { RequirementResponsibilityState.valueOf(m["coreStatus"] as? String ?: "PENDING") } catch(e: Exception) { RequirementResponsibilityState.PENDING },
        deadline = m["deadline"] as? String ?: "2026-08-20",
        notes = m["notes"] as? String ?: "",
        evidenceUrl = m["evidenceUrl"] as? String,
        lastUpdated = m["lastUpdated"] as? String ?: ""
    )

    private fun issueToMap(i: IssueItem): Map<String, Any?> = mapOf(
        "id" to i.id,
        "dateRaised" to i.dateRaised,
        "vertical" to i.vertical,
        "eventId" to i.eventId,
        "eventName" to i.eventName,
        "problem" to i.problem,
        "raisedById" to i.raisedById,
        "raisedByName" to i.raisedByName,
        "assignedToId" to i.assignedToId,
        "assignedToName" to i.assignedToName,
        "severity" to i.severity.name,
        "status" to i.status.name,
        "actionRequired" to i.actionRequired,
        "actionPlan" to i.actionPlan,
        "deadline" to i.deadline,
        "escalationLevel" to i.escalationLevel.name,
        "escalatedToId" to i.escalatedToId,
        "escalatedToName" to i.escalatedToName,
        "resolution" to i.resolution,
        "resolutionDate" to i.resolutionDate,
        "remarks" to i.remarks,
        "lastUpdated" to i.lastUpdated
    )

    private fun docToIssue(m: Map<String, Any?>): IssueItem = IssueItem(
        id = m["id"] as? String ?: "",
        dateRaised = m["dateRaised"] as? String ?: "",
        vertical = m["vertical"] as? String ?: "",
        eventId = m["eventId"] as? String,
        eventName = m["eventName"] as? String,
        problem = m["problem"] as? String ?: "",
        raisedById = m["raisedById"] as? String ?: "",
        raisedByName = m["raisedByName"] as? String ?: "",
        assignedToId = m["assignedToId"] as? String ?: "",
        assignedToName = m["assignedToName"] as? String ?: "",
        severity = try { IssueSeverity.valueOf(m["severity"] as? String ?: "MEDIUM") } catch(e: Exception) { IssueSeverity.MEDIUM },
        status = try { IssueStatus.valueOf(m["status"] as? String ?: "OPEN") } catch(e: Exception) { IssueStatus.OPEN },
        actionRequired = m["actionRequired"] as? String ?: "",
        actionPlan = m["actionPlan"] as? String ?: "",
        deadline = m["deadline"] as? String ?: "2026-08-20",
        escalationLevel = try { EscalationLevel.valueOf(m["escalationLevel"] as? String ?: "L1_VOLUNTEER_COORDINATOR") } catch(e: Exception) { EscalationLevel.L1_VOLUNTEER_COORDINATOR },
        escalatedToId = m["escalatedToId"] as? String,
        escalatedToName = m["escalatedToName"] as? String,
        resolution = m["resolution"] as? String,
        resolutionDate = m["resolutionDate"] as? String,
        remarks = m["remarks"] as? String ?: "",
        lastUpdated = m["lastUpdated"] as? String ?: ""
    )

    private fun calendarToMap(c: CalendarItem): Map<String, Any?> = mapOf(
        "id" to c.id,
        "date" to c.date,
        "time" to c.time,
        "activity" to c.activity,
        "category" to c.category,
        "eventOrArea" to c.eventOrArea,
        "audience" to c.audience,
        "personResponsible" to c.personResponsible,
        "status" to c.status,
        "priority" to c.priority.name,
        "deadlineType" to c.deadlineType.name,
        "meetingUrl" to c.meetingUrl,
        "resourceUrl" to c.resourceUrl,
        "remarks" to c.remarks
    )

    private fun docToCalendar(m: Map<String, Any?>): CalendarItem = CalendarItem(
        id = m["id"] as? String ?: "",
        date = m["date"] as? String ?: "2026-08-16",
        time = m["time"] as? String ?: "09:00",
        activity = m["activity"] as? String ?: "",
        category = m["category"] as? String ?: "Event",
        eventOrArea = m["eventOrArea"] as? String ?: "",
        audience = m["audience"] as? String ?: "",
        personResponsible = m["personResponsible"] as? String ?: "",
        status = m["status"] as? String ?: "Scheduled",
        priority = try { Priority.valueOf(m["priority"] as? String ?: "MEDIUM") } catch(e: Exception) { Priority.MEDIUM },
        deadlineType = try { DeadlineType.valueOf(m["deadlineType"] as? String ?: "SOFT_DEADLINE") } catch(e: Exception) { DeadlineType.SOFT_DEADLINE },
        meetingUrl = m["meetingUrl"] as? String,
        resourceUrl = m["resourceUrl"] as? String,
        remarks = m["remarks"] as? String ?: ""
    )

    private fun approvalToMap(a: ApprovalItem): Map<String, Any?> = mapOf(
        "id" to a.id,
        "type" to a.type.name,
        "title" to a.title,
        "subtitle" to a.subtitle,
        "targetId" to a.targetId,
        "requestedBy" to a.requestedBy,
        "requestedDate" to a.requestedDate,
        "status" to a.status.name,
        "remarks" to a.remarks,
        "decidedBy" to a.decidedBy,
        "decidedAt" to a.decidedAt
    )

    private fun docToApproval(m: Map<String, Any?>): ApprovalItem = ApprovalItem(
        id = m["id"] as? String ?: "",
        type = try { ApprovalType.valueOf(m["type"] as? String ?: "EVENT_EXECUTION") } catch(e: Exception) { ApprovalType.EVENT_EXECUTION },
        title = m["title"] as? String ?: "",
        subtitle = m["subtitle"] as? String ?: "",
        targetId = m["targetId"] as? String ?: "",
        requestedBy = m["requestedBy"] as? String ?: "",
        requestedDate = m["requestedDate"] as? String ?: "",
        status = try { CoreApprovalStatus.valueOf(m["status"] as? String ?: "PENDING") } catch(e: Exception) { CoreApprovalStatus.PENDING },
        remarks = m["remarks"] as? String ?: "",
        decidedBy = m["decidedBy"] as? String,
        decidedAt = m["decidedAt"] as? String
    )

    private fun proposalToMap(p: ProposalReview): Map<String, Any?> = mapOf(
        "id" to p.id,
        "eventId" to p.eventId,
        "eventTitle" to p.eventTitle,
        "reviewerId" to p.reviewerId,
        "reviewerName" to p.reviewerName,
        "totalScore" to p.totalScore,
        "maxPossibleScore" to p.maxPossibleScore,
        "recommendation" to p.recommendation.name,
        "strengths" to p.strengths,
        "concerns" to p.concerns,
        "suggestions" to p.suggestions,
        "isSubmitted" to p.isSubmitted,
        "submittedAt" to p.submittedAt
    )

    private fun docToProposal(m: Map<String, Any?>): ProposalReview = ProposalReview(
        id = m["id"] as? String ?: "",
        eventId = m["eventId"] as? String ?: "",
        eventTitle = m["eventTitle"] as? String ?: "",
        reviewerId = m["reviewerId"] as? String ?: "",
        reviewerName = m["reviewerName"] as? String ?: "",
        criteriaScores = emptyList(),
        totalScore = (m["totalScore"] as? Number)?.toInt() ?: 0,
        maxPossibleScore = (m["maxPossibleScore"] as? Number)?.toInt() ?: 100,
        recommendation = try { ProposalRecommendation.valueOf(m["recommendation"] as? String ?: "RECOMMEND") } catch(e: Exception) { ProposalRecommendation.RECOMMEND },
        strengths = m["strengths"] as? String ?: "",
        concerns = m["concerns"] as? String ?: "",
        suggestions = m["suggestions"] as? String ?: "",
        isSubmitted = m["isSubmitted"] as? Boolean ?: false,
        submittedAt = m["submittedAt"] as? String
    )

    private fun auditLogToMap(l: AuditLogEntry): Map<String, Any?> = mapOf(
        "id" to l.id,
        "user" to l.user,
        "userRole" to l.userRole.name,
        "timestamp" to l.timestamp,
        "objectType" to l.objectType,
        "objectId" to l.objectId,
        "action" to l.action,
        "details" to l.details
    )

    private fun docToAuditLog(m: Map<String, Any?>): AuditLogEntry = AuditLogEntry(
        id = m["id"] as? String ?: "",
        user = m["user"] as? String ?: "",
        userRole = try { UserRole.valueOf(m["userRole"] as? String ?: "COORDINATOR") } catch(e: Exception) { UserRole.COORDINATOR },
        timestamp = m["timestamp"] as? String ?: "",
        objectType = m["objectType"] as? String ?: "",
        objectId = m["objectId"] as? String ?: "",
        action = m["action"] as? String ?: "",
        details = m["details"] as? String ?: ""
    )

    private fun teamMemberToMap(t: TeamMember): Map<String, Any?> = mapOf(
        "id" to t.id,
        "name" to t.name,
        "email" to t.email,
        "phone" to t.phone,
        "role" to t.role.name,
        "vertical" to t.vertical,
        "reportsToId" to t.reportsToId,
        "reportsToName" to t.reportsToName,
        "active" to t.active,
        "joinedOn" to t.joinedOn,
        "avatarColor" to t.avatarColor
    )
}
