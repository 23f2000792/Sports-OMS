package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.*

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val vertical: String,
    val teamMemberId: String,
    val teamMemberName: String,
    val taskType: String,
    val priority: Priority,
    val assignedById: String,
    val assignedByName: String,
    val dateAssigned: String,
    val deadline: String,
    val completedOn: String? = null,
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val progressPercent: Int = 0,
    val blocker: String? = null,
    val remarks: String = "",
    val eventId: String? = null,
    val eventName: String? = null,
    val dependencies: List<String> = emptyList(),
    val evidenceList: List<EvidenceAttachment> = emptyList(),
    val activityHistory: List<TaskActivity> = emptyList(),
    val lastUpdated: String = ""
)

fun TaskEntity.toDomain() = TaskItem(
    id = id,
    title = title,
    description = description,
    vertical = vertical,
    teamMemberId = teamMemberId,
    teamMemberName = teamMemberName,
    taskType = taskType,
    priority = priority,
    assignedById = assignedById,
    assignedByName = assignedByName,
    dateAssigned = dateAssigned,
    deadline = deadline,
    completedOn = completedOn,
    status = status,
    progressPercent = progressPercent,
    blocker = blocker,
    remarks = remarks,
    eventId = eventId,
    eventName = eventName,
    dependencies = dependencies,
    evidenceList = evidenceList,
    activityHistory = activityHistory,
    lastUpdated = lastUpdated
)

fun TaskItem.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    vertical = vertical,
    teamMemberId = teamMemberId,
    teamMemberName = teamMemberName,
    taskType = taskType,
    priority = priority,
    assignedById = assignedById,
    assignedByName = assignedByName,
    dateAssigned = dateAssigned,
    deadline = deadline,
    completedOn = completedOn,
    status = status,
    progressPercent = progressPercent,
    blocker = blocker,
    remarks = remarks,
    eventId = eventId,
    eventName = eventName,
    dependencies = dependencies,
    evidenceList = evidenceList,
    activityHistory = activityHistory,
    lastUpdated = lastUpdated
)

@Entity(tableName = "events")
data class SportsEventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val society: String,
    val eventHead: String,
    val eventHeadContact: String,
    val sportsPoc: String,
    val coordinator: String,
    val currentStage: EventStage,
    val readinessPercent: Int = 0,
    val coreApproval: CoreApprovalStatus = CoreApprovalStatus.PENDING,
    val remarks: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val venue: String = "",
    val expectedParticipants: Int = 0
)

fun SportsEventEntity.toDomain() = SportsEvent(
    id = id,
    name = name,
    society = society,
    eventHead = eventHead,
    eventHeadContact = eventHeadContact,
    sportsPoc = sportsPoc,
    coordinator = coordinator,
    currentStage = currentStage,
    readinessPercent = readinessPercent,
    coreApproval = coreApproval,
    remarks = remarks,
    description = description,
    startDate = startDate,
    endDate = endDate,
    venue = venue,
    expectedParticipants = expectedParticipants
)

fun SportsEvent.toEntity() = SportsEventEntity(
    id = id,
    name = name,
    society = society,
    eventHead = eventHead,
    eventHeadContact = eventHeadContact,
    sportsPoc = sportsPoc,
    coordinator = coordinator,
    currentStage = currentStage,
    readinessPercent = readinessPercent,
    coreApproval = coreApproval,
    remarks = remarks,
    description = description,
    startDate = startDate,
    endDate = endDate,
    venue = venue,
    expectedParticipants = expectedParticipants
)

@Entity(tableName = "readiness_requirements")
data class ReadinessRequirementEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val phaseNumber: Int,
    val phaseTitle: String,
    val title: String,
    val pocStatus: RequirementResponsibilityState = RequirementResponsibilityState.PENDING,
    val coordinatorStatus: RequirementResponsibilityState = RequirementResponsibilityState.PENDING,
    val coreStatus: RequirementResponsibilityState = RequirementResponsibilityState.PENDING,
    val deadline: String = "",
    val notes: String = "",
    val evidenceUrl: String? = null,
    val lastUpdated: String = ""
)

fun ReadinessRequirementEntity.toDomain() = EventReadinessRequirement(
    id = id,
    eventId = eventId,
    phaseNumber = phaseNumber,
    phaseTitle = phaseTitle,
    title = title,
    pocStatus = pocStatus,
    coordinatorStatus = coordinatorStatus,
    coreStatus = coreStatus,
    deadline = deadline,
    notes = notes,
    evidenceUrl = evidenceUrl,
    lastUpdated = lastUpdated
)

fun EventReadinessRequirement.toEntity() = ReadinessRequirementEntity(
    id = id,
    eventId = eventId,
    phaseNumber = phaseNumber,
    phaseTitle = phaseTitle,
    title = title,
    pocStatus = pocStatus,
    coordinatorStatus = coordinatorStatus,
    coreStatus = coreStatus,
    deadline = deadline,
    notes = notes,
    evidenceUrl = evidenceUrl,
    lastUpdated = lastUpdated
)

@Entity(tableName = "issues")
data class IssueEntity(
    @PrimaryKey val id: String,
    val dateRaised: String,
    val vertical: String,
    val eventId: String? = null,
    val eventName: String? = null,
    val problem: String,
    val raisedById: String,
    val raisedByName: String,
    val assignedToId: String,
    val assignedToName: String,
    val severity: IssueSeverity,
    val status: IssueStatus = IssueStatus.OPEN,
    val actionRequired: String,
    val actionPlan: String = "",
    val deadline: String = "2026-08-20",
    val escalationLevel: EscalationLevel = EscalationLevel.L1_VOLUNTEER_COORDINATOR,
    val escalatedToId: String? = null,
    val escalatedToName: String? = null,
    val resolution: String? = null,
    val resolutionDate: String? = null,
    val evidenceList: List<EvidenceAttachment> = emptyList(),
    val remarks: String = "",
    val escalationHistory: List<EscalationHistoryEntry> = emptyList(),
    val lastUpdated: String = ""
)

fun IssueEntity.toDomain() = IssueItem(
    id = id,
    dateRaised = dateRaised,
    vertical = vertical,
    eventId = eventId,
    eventName = eventName,
    problem = problem,
    raisedById = raisedById,
    raisedByName = raisedByName,
    assignedToId = assignedToId,
    assignedToName = assignedToName,
    severity = severity,
    status = status,
    actionRequired = actionRequired,
    actionPlan = actionPlan,
    deadline = deadline,
    escalationLevel = escalationLevel,
    escalatedToId = escalatedToId,
    escalatedToName = escalatedToName,
    resolution = resolution,
    resolutionDate = resolutionDate,
    evidenceList = evidenceList,
    remarks = remarks,
    escalationHistory = escalationHistory,
    lastUpdated = lastUpdated
)

fun IssueItem.toEntity() = IssueEntity(
    id = id,
    dateRaised = dateRaised,
    vertical = vertical,
    eventId = eventId,
    eventName = eventName,
    problem = problem,
    raisedById = raisedById,
    raisedByName = raisedByName,
    assignedToId = assignedToId,
    assignedToName = assignedToName,
    severity = severity,
    status = status,
    actionRequired = actionRequired,
    actionPlan = actionPlan,
    deadline = deadline,
    escalationLevel = escalationLevel,
    escalatedToId = escalatedToId,
    escalatedToName = escalatedToName,
    resolution = resolution,
    resolutionDate = resolutionDate,
    evidenceList = evidenceList,
    remarks = remarks,
    escalationHistory = escalationHistory,
    lastUpdated = lastUpdated
)

@Entity(tableName = "calendar_items")
data class CalendarEntity(
    @PrimaryKey val id: String,
    val date: String,
    val time: String,
    val activity: String,
    val category: String,
    val eventOrArea: String,
    val audience: String = "All",
    val personResponsible: String = "Sports Team",
    val status: String = "Scheduled",
    val priority: Priority = Priority.MEDIUM,
    val deadlineType: DeadlineType = DeadlineType.SOFT_DEADLINE,
    val meetingUrl: String? = null,
    val resourceUrl: String? = null,
    val remarks: String = ""
)

fun CalendarEntity.toDomain() = CalendarItem(
    id = id,
    date = date,
    time = time,
    activity = activity,
    category = category,
    eventOrArea = eventOrArea,
    audience = audience,
    personResponsible = personResponsible,
    status = status,
    priority = priority,
    deadlineType = deadlineType,
    meetingUrl = meetingUrl,
    resourceUrl = resourceUrl,
    remarks = remarks
)

fun CalendarItem.toEntity() = CalendarEntity(
    id = id,
    date = date,
    time = time,
    activity = activity,
    category = category,
    eventOrArea = eventOrArea,
    audience = audience,
    personResponsible = personResponsible,
    status = status,
    priority = priority,
    deadlineType = deadlineType,
    meetingUrl = meetingUrl,
    resourceUrl = resourceUrl,
    remarks = remarks
)

@Entity(tableName = "approvals")
data class ApprovalEntity(
    @PrimaryKey val id: String,
    val type: ApprovalType,
    val title: String,
    val subtitle: String,
    val targetId: String,
    val requestedBy: String,
    val requestedDate: String,
    val status: CoreApprovalStatus = CoreApprovalStatus.PENDING,
    val remarks: String = "",
    val decidedBy: String? = null,
    val decidedAt: String? = null
)

fun ApprovalEntity.toDomain() = ApprovalItem(
    id = id,
    type = type,
    title = title,
    subtitle = subtitle,
    targetId = targetId,
    requestedBy = requestedBy,
    requestedDate = requestedDate,
    status = status,
    remarks = remarks,
    decidedBy = decidedBy,
    decidedAt = decidedAt
)

fun ApprovalItem.toEntity() = ApprovalEntity(
    id = id,
    type = type,
    title = title,
    subtitle = subtitle,
    targetId = targetId,
    requestedBy = requestedBy,
    requestedDate = requestedDate,
    status = status,
    remarks = remarks,
    decidedBy = decidedBy,
    decidedAt = decidedAt
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val user: String,
    val userRole: UserRole,
    val timestamp: String,
    val objectType: String,
    val objectId: String,
    val action: String,
    val details: String
)

fun AuditLogEntity.toDomain() = AuditLogEntry(
    id = id,
    user = user,
    userRole = userRole,
    timestamp = timestamp,
    objectType = objectType,
    objectId = objectId,
    action = action,
    details = details
)

fun AuditLogEntry.toEntity() = AuditLogEntity(
    id = id,
    user = user,
    userRole = userRole,
    timestamp = timestamp,
    objectType = objectType,
    objectId = objectId,
    action = action,
    details = details
)

@Entity(tableName = "proposal_reviews")
data class ProposalReviewEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val eventTitle: String,
    val reviewerId: String,
    val reviewerName: String,
    val criteriaScores: List<CriterionScore>,
    val totalScore: Int,
    val maxPossibleScore: Int,
    val recommendation: ProposalRecommendation,
    val strengths: String,
    val concerns: String,
    val suggestions: String,
    val isSubmitted: Boolean = false,
    val submittedAt: String? = null
)

fun ProposalReviewEntity.toDomain() = ProposalReview(
    id = id,
    eventId = eventId,
    eventTitle = eventTitle,
    reviewerId = reviewerId,
    reviewerName = reviewerName,
    criteriaScores = criteriaScores,
    totalScore = totalScore,
    maxPossibleScore = maxPossibleScore,
    recommendation = recommendation,
    strengths = strengths,
    concerns = concerns,
    suggestions = suggestions,
    isSubmitted = isSubmitted,
    submittedAt = submittedAt
)

fun ProposalReview.toEntity() = ProposalReviewEntity(
    id = id,
    eventId = eventId,
    eventTitle = eventTitle,
    reviewerId = reviewerId,
    reviewerName = reviewerName,
    criteriaScores = criteriaScores,
    totalScore = totalScore,
    maxPossibleScore = maxPossibleScore,
    recommendation = recommendation,
    strengths = strengths,
    concerns = concerns,
    suggestions = suggestions,
    isSubmitted = isSubmitted,
    submittedAt = submittedAt
)
