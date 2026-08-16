package com.example.model

enum class UserRole(val displayName: String, val level: Int) {
    CORE("Core", 5),
    DEPUTY_CORE("Deputy Core", 4),
    SUPER_COORDINATOR("Super Coordinator", 3),
    COORDINATOR("Coordinator", 2),
    VOLUNTEER("Volunteer", 1)
}

data class CurrentUser(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val vertical: String,
    val reportsTo: String? = null,
    val avatarColor: Long = 0xFF1E88E5
)

enum class Priority(val displayName: String) {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class TaskStatus(val displayName: String) {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    BLOCKED("Blocked"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    ON_HOLD("On Hold")
}

enum class TaskHealth(val displayName: String) {
    COMPLETED("Completed"),
    ON_TRACK("On Track"),
    AT_RISK("At Risk"),
    OVERDUE("Overdue"),
    BLOCKED("Blocked"),
    NO_DEADLINE("No Deadline"),
    CANCELLED("Cancelled")
}

data class EvidenceAttachment(
    val id: String,
    val title: String,
    val url: String,
    val type: String = "Link", // Document, Sheet, Image, Drive, Form
    val uploadedBy: String,
    val uploadedAt: String
)

data class TaskActivity(
    val id: String,
    val user: String,
    val action: String,
    val timestamp: String,
    val previousValue: String? = null,
    val newValue: String? = null
)

data class TaskItem(
    val id: String, // e.g. TASK-0001
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
    val deadline: String, // YYYY-MM-DD
    val completedOn: String? = null,
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val progressPercent: Int = 0,
    val blocker: String? = null,
    val remarks: String = "",
    val eventId: String? = null,
    val eventName: String? = null,
    val dependencies: List<String> = emptyList(), // Task IDs
    val evidenceList: List<EvidenceAttachment> = emptyList(),
    val activityHistory: List<TaskActivity> = emptyList(),
    val lastUpdated: String = ""
)

enum class EventStage(val displayName: String, val order: Int) {
    PROPOSAL("Proposal", 1),
    REVIEW("Review", 2),
    SHORTLISTED("Shortlisted", 3),
    INTERVIEW("Interview", 4),
    APPROVED("Approved", 5),
    ONBOARDING("Onboarding", 6),
    DOCUMENTATION("Documentation", 7),
    BRANDING("Branding", 8),
    REGISTRATION("Registration", 9),
    TECHNICAL_SETUP("Technical Setup", 10),
    MOCK_TRIALS("Mock Trials", 11),
    READY_FOR_EXECUTION("Ready for Execution", 12),
    LIVE("Live", 13),
    COMPLETED("Completed", 14),
    CLOSURE("Closure", 15),
    ARCHIVED("Archived", 16)
}

enum class CoreApprovalStatus(val displayName: String) {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REWORK_REQUESTED("Rework Requested"),
    REJECTED("Rejected")
}

data class SportsEvent(
    val id: String, // E01, E02...
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

enum class RequirementResponsibilityState {
    PENDING,
    COMPLETED,
    REWORK,
    REJECTED
}

data class EventReadinessRequirement(
    val id: String, // e.g. E01-DOC-01
    val eventId: String,
    val phaseNumber: Int, // 1..7
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

enum class IssueSeverity(val displayName: String) {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class IssueStatus(val displayName: String) {
    OPEN("Open"),
    UNDER_REVIEW("Under Review"),
    ACTION_TAKEN("Action Taken"),
    RESOLVED("Resolved"),
    CLOSED("Closed")
}

enum class EscalationLevel(val displayName: String) {
    L1_VOLUNTEER_COORDINATOR("L1 - Coordinator"),
    L2_SUPER_COORDINATOR("L2 - Super Coordinator"),
    L3_DEPUTY_CORE("L3 - Deputy Core"),
    L4_CORE("L4 - Core")
}

data class EscalationHistoryEntry(
    val fromUser: String,
    val toUser: String,
    val timestamp: String,
    val reason: String
)

data class IssueItem(
    val id: String, // ISS-001
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

enum class DeadlineType(val displayName: String) {
    HARD_DEADLINE("Hard Deadline"),
    SOFT_DEADLINE("Soft Deadline"),
    INFORMATIONAL("Informational")
}

data class CalendarItem(
    val id: String, // CAL-001
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM AM/PM
    val activity: String,
    val category: String, // Meeting, Review, Setup, Milestone, Deadlines, Match
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

data class ReviewCriterion(
    val id: String,
    val name: String,
    val description: String,
    val maxScore: Int = 10,
    val weight: Double = 1.0
)

enum class ProposalRecommendation(val displayName: String) {
    STRONGLY_RECOMMEND("Strongly Recommend"),
    RECOMMEND("Recommend"),
    RECOMMEND_WITH_CHANGES("Recommend with Changes"),
    HOLD("Hold"),
    REJECT("Reject")
}

data class CriterionScore(
    val criterionId: String,
    val criterionName: String,
    val score: Int,
    val maxScore: Int = 10,
    val comment: String = ""
)

data class ProposalReview(
    val id: String,
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

data class TeamMember(
    val id: String, // TM-01
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val vertical: String,
    val reportsToId: String? = null,
    val reportsToName: String? = null,
    val active: Boolean = true,
    val joinedOn: String = "2026-01-15",
    val avatarColor: Long = 0xFF1E88E5
)

enum class NotificationPriority(val displayName: String) {
    CRITICAL("Critical"),
    HIGH("High"),
    NORMAL("Normal")
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val timestamp: String,
    val isRead: Boolean = false,
    val forUserId: String? = null, // null means broadcast to role/all
    val targetType: String = "TASK", // TASK, EVENT, ISSUE, CALENDAR, APPROVAL
    val targetId: String? = null
)

enum class ApprovalType(val displayName: String) {
    EVENT_EXECUTION("Event Execution Authorization"),
    READINESS_PHASE("Phase Readiness Sign-off"),
    PROPOSAL_DECISION("Proposal Selection Approval"),
    REWORK_REQUEST("Rework Completion Sign-off"),
    ISSUE_ESCALATION("Escalated Action Authorization")
}

data class ApprovalItem(
    val id: String,
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

data class AuditLogEntry(
    val id: String,
    val user: String,
    val userRole: UserRole,
    val timestamp: String,
    val objectType: String, // Task, Event, Issue, Review, Member
    val objectId: String,
    val action: String, // Created, Updated, Reassigned, Status Changed, Escalated, Approved, Rejected
    val details: String
)
