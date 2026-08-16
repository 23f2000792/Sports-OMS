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
    ONLINE("Cloud Connected", true),
    SYNCING("Syncing with Firestore...", true),
    OFFLINE_CACHE("Local Cache (Offline Mode)", false),
    UNCONFIGURED("Firebase Standby (Ready)", false),
    ERROR("Cloud Sync Warning", false)
}

data class CloudSyncSummary(
    val status: CloudConnectionStatus = CloudConnectionStatus.OFFLINE_CACHE,
    val lastSyncTime: String = "Not synced yet",
    val syncedDocumentsCount: Int = 0,
    val pendingOperations: Int = 0,
    val firestoreProject: String = "Firebase Cloud Firestore",
    val statusMessage: String = "Local SQLite & Cloud Firestore Ready"
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
                statusMessage = "Firestore Backend Active & Listening"
            )
            Log.d(TAG, "FirebaseFirestore initialized successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore not initialized or google-services.json not found: ${e.message}")
            _syncSummary.value = _syncSummary.value.copy(
                status = CloudConnectionStatus.OFFLINE_CACHE,
                statusMessage = "Local SQLite Storage Active (Cloud ready on connection)"
            )
        }
    }

    val isFirebaseAvailable: Boolean
        get() = firestore != null

    // Collections
    private val COLLECTION_TASKS = "sports_tasks"
    private val COLLECTION_EVENTS = "sports_events"
    private val COLLECTION_READINESS = "sports_readiness"
    private val COLLECTION_ISSUES = "sports_issues"
    private val COLLECTION_CALENDAR = "sports_calendar"
    private val COLLECTION_APPROVALS = "sports_approvals"
    private val COLLECTION_PROPOSALS = "sports_proposals"
    private val COLLECTION_AUDIT_LOGS = "sports_audit_logs"

    // Listeners for real-time remote updates
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
            // Tasks Listener
            val tasksReg = db.collection(COLLECTION_TASKS).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Tasks listen error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            docToTask(doc.data ?: return@mapNotNull null)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (list.isNotEmpty()) onTasksUpdated(list)
                }
            }
            listeners.add(tasksReg)

            // Events Listener
            val eventsReg = db.collection(COLLECTION_EVENTS).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            docToEvent(doc.data ?: return@mapNotNull null)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (list.isNotEmpty()) onEventsUpdated(list)
                }
            }
            listeners.add(eventsReg)

            // Issues Listener
            val issuesReg = db.collection(COLLECTION_ISSUES).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            docToIssue(doc.data ?: return@mapNotNull null)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (list.isNotEmpty()) onIssuesUpdated(list)
                }
            }
            listeners.add(issuesReg)

            // Approvals Listener
            val approvalsReg = db.collection(COLLECTION_APPROVALS).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            docToApproval(doc.data ?: return@mapNotNull null)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (list.isNotEmpty()) onApprovalsUpdated(list)
                }
            }
            listeners.add(approvalsReg)

        } catch (e: Exception) {
            Log.e(TAG, "Failed setting up Firestore snapshot listeners: ${e.message}")
        }
    }

    fun removeListeners() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    // PUSH OPERATIONS (Fire and forget or suspend)
    fun syncTask(task: TaskItem) {
        val db = firestore ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                db.collection(COLLECTION_TASKS).document(task.id)
                    .set(taskToMap(task), SetOptions.merge())
                updateLastSync()
            } catch (e: Exception) {
                Log.w(TAG, "Error syncing task to Firestore: ${e.message}")
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
                Log.w(TAG, "Error syncing event to Firestore: ${e.message}")
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
                Log.w(TAG, "Error syncing readiness to Firestore: ${e.message}")
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
                Log.w(TAG, "Error syncing issue to Firestore: ${e.message}")
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
                Log.w(TAG, "Error syncing calendar to Firestore: ${e.message}")
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
                Log.w(TAG, "Error syncing approval to Firestore: ${e.message}")
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
                Log.w(TAG, "Error syncing proposal to Firestore: ${e.message}")
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
                Log.w(TAG, "Error syncing audit log to Firestore: ${e.message}")
            }
        }
    }

    // SEED ALL TO FIRESTORE
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
        val db = firestore ?: return Result.failure(Exception("Firebase Firestore instance not available"))

        _syncSummary.value = _syncSummary.value.copy(
            status = CloudConnectionStatus.SYNCING,
            statusMessage = "Uploading full sports operations dataset to Firestore..."
        )

        var count = 0
        try {
            val batch = db.batch()

            tasks.forEach {
                batch.set(db.collection(COLLECTION_TASKS).document(it.id), taskToMap(it))
                count++
            }
            events.forEach {
                batch.set(db.collection(COLLECTION_EVENTS).document(it.id), eventToMap(it))
                count++
            }
            readiness.forEach {
                batch.set(db.collection(COLLECTION_READINESS).document(it.id), readinessToMap(it))
                count++
            }
            issues.forEach {
                batch.set(db.collection(COLLECTION_ISSUES).document(it.id), issueToMap(it))
                count++
            }
            calendar.forEach {
                batch.set(db.collection(COLLECTION_CALENDAR).document(it.id), calendarToMap(it))
                count++
            }
            approvals.forEach {
                batch.set(db.collection(COLLECTION_APPROVALS).document(it.id), approvalToMap(it))
                count++
            }
            proposals.forEach {
                batch.set(db.collection(COLLECTION_PROPOSALS).document(it.id), proposalToMap(it))
                count++
            }
            auditLogs.take(20).forEach {
                batch.set(db.collection(COLLECTION_AUDIT_LOGS).document(it.id), auditLogToMap(it))
                count++
            }

            batch.commit().await()

            _syncSummary.value = _syncSummary.value.copy(
                status = CloudConnectionStatus.ONLINE,
                lastSyncTime = "Just now",
                syncedDocumentsCount = count,
                statusMessage = "Successfully pushed $count records to Cloud Firestore!"
            )
            return Result.success(count)
        } catch (e: Exception) {
            _syncSummary.value = _syncSummary.value.copy(
                status = CloudConnectionStatus.ERROR,
                statusMessage = "Cloud Sync Error: ${e.message ?: "Unknown error"}"
            )
            return Result.failure(e)
        }
    }

    private fun updateLastSync() {
        _syncSummary.value = _syncSummary.value.copy(
            status = CloudConnectionStatus.ONLINE,
            lastSyncTime = "Just now",
            statusMessage = "Synced live with Firestore"
        )
    }

    // MAP SERIALIZATION HELPERS
    private fun taskToMap(t: TaskItem): Map<String, Any?> = mapOf(
        "id" to t.id,
        "title" to t.title,
        "description" to t.description,
        "vertical" to t.vertical,
        "teamMemberId" to t.teamMemberId,
        "teamMemberName" to t.teamMemberName,
        "taskType" to t.taskType,
        "priority" to t.priority.name,
        "assignedById" to t.assignedById,
        "assignedByName" to t.assignedByName,
        "dateAssigned" to t.dateAssigned,
        "deadline" to t.deadline,
        "completedOn" to t.completedOn,
        "status" to t.status.name,
        "progressPercent" to t.progressPercent,
        "blocker" to t.blocker,
        "remarks" to t.remarks,
        "eventId" to t.eventId,
        "eventName" to t.eventName,
        "dependencies" to t.dependencies,
        "lastUpdated" to t.lastUpdated
    )

    private fun docToTask(m: Map<String, Any?>): TaskItem = TaskItem(
        id = m["id"] as? String ?: "",
        title = m["title"] as? String ?: "",
        description = m["description"] as? String ?: "",
        vertical = m["vertical"] as? String ?: "",
        teamMemberId = m["teamMemberId"] as? String ?: "",
        teamMemberName = m["teamMemberName"] as? String ?: "",
        taskType = m["taskType"] as? String ?: "",
        priority = try { Priority.valueOf(m["priority"] as? String ?: "MEDIUM") } catch(e: Exception) { Priority.MEDIUM },
        assignedById = m["assignedById"] as? String ?: "",
        assignedByName = m["assignedByName"] as? String ?: "",
        dateAssigned = m["dateAssigned"] as? String ?: "",
        deadline = m["deadline"] as? String ?: "",
        completedOn = m["completedOn"] as? String,
        status = try { TaskStatus.valueOf(m["status"] as? String ?: "NOT_STARTED") } catch(e: Exception) { TaskStatus.NOT_STARTED },
        progressPercent = (m["progressPercent"] as? Number)?.toInt() ?: 0,
        blocker = m["blocker"] as? String,
        remarks = m["remarks"] as? String ?: "",
        eventId = m["eventId"] as? String,
        eventName = m["eventName"] as? String,
        lastUpdated = m["lastUpdated"] as? String ?: ""
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
}
