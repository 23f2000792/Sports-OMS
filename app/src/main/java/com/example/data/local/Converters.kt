package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Strings List
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return stringListAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            stringListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // EvidenceAttachment List
    private val evidenceListType = Types.newParameterizedType(List::class.java, EvidenceAttachment::class.java)
    private val evidenceListAdapter = moshi.adapter<List<EvidenceAttachment>>(evidenceListType)

    @TypeConverter
    fun fromEvidenceList(list: List<EvidenceAttachment>?): String {
        return evidenceListAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toEvidenceList(json: String?): List<EvidenceAttachment> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            evidenceListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // TaskActivity List
    private val taskActivityListType = Types.newParameterizedType(List::class.java, TaskActivity::class.java)
    private val taskActivityListAdapter = moshi.adapter<List<TaskActivity>>(taskActivityListType)

    @TypeConverter
    fun fromTaskActivityList(list: List<TaskActivity>?): String {
        return taskActivityListAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toTaskActivityList(json: String?): List<TaskActivity> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            taskActivityListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // EscalationHistoryEntry List
    private val escalationListType = Types.newParameterizedType(List::class.java, EscalationHistoryEntry::class.java)
    private val escalationListAdapter = moshi.adapter<List<EscalationHistoryEntry>>(escalationListType)

    @TypeConverter
    fun fromEscalationList(list: List<EscalationHistoryEntry>?): String {
        return escalationListAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toEscalationList(json: String?): List<EscalationHistoryEntry> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            escalationListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // CriterionScore List
    private val criterionScoreListType = Types.newParameterizedType(List::class.java, CriterionScore::class.java)
    private val criterionScoreListAdapter = moshi.adapter<List<CriterionScore>>(criterionScoreListType)

    @TypeConverter
    fun fromCriterionScoreList(list: List<CriterionScore>?): String {
        return criterionScoreListAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toCriterionScoreList(json: String?): List<CriterionScore> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            criterionScoreListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Enums
    @TypeConverter
    fun fromPriority(value: Priority?): String = (value ?: Priority.MEDIUM).name

    @TypeConverter
    fun toPriority(value: String?): Priority = try {
        Priority.valueOf(value ?: Priority.MEDIUM.name)
    } catch (e: Exception) {
        Priority.MEDIUM
    }

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus?): String = (value ?: TaskStatus.NOT_STARTED).name

    @TypeConverter
    fun toTaskStatus(value: String?): TaskStatus = try {
        TaskStatus.valueOf(value ?: TaskStatus.NOT_STARTED.name)
    } catch (e: Exception) {
        TaskStatus.NOT_STARTED
    }

    @TypeConverter
    fun fromEventStage(value: EventStage?): String = (value ?: EventStage.PROPOSAL).name

    @TypeConverter
    fun toEventStage(value: String?): EventStage = try {
        EventStage.valueOf(value ?: EventStage.PROPOSAL.name)
    } catch (e: Exception) {
        EventStage.PROPOSAL
    }

    @TypeConverter
    fun fromCoreApprovalStatus(value: CoreApprovalStatus?): String = (value ?: CoreApprovalStatus.PENDING).name

    @TypeConverter
    fun toCoreApprovalStatus(value: String?): CoreApprovalStatus = try {
        CoreApprovalStatus.valueOf(value ?: CoreApprovalStatus.PENDING.name)
    } catch (e: Exception) {
        CoreApprovalStatus.PENDING
    }

    @TypeConverter
    fun fromReqRespState(value: RequirementResponsibilityState?): String = (value ?: RequirementResponsibilityState.PENDING).name

    @TypeConverter
    fun toReqRespState(value: String?): RequirementResponsibilityState = try {
        RequirementResponsibilityState.valueOf(value ?: RequirementResponsibilityState.PENDING.name)
    } catch (e: Exception) {
        RequirementResponsibilityState.PENDING
    }

    @TypeConverter
    fun fromIssueSeverity(value: IssueSeverity?): String = (value ?: IssueSeverity.MEDIUM).name

    @TypeConverter
    fun toIssueSeverity(value: String?): IssueSeverity = try {
        IssueSeverity.valueOf(value ?: IssueSeverity.MEDIUM.name)
    } catch (e: Exception) {
        IssueSeverity.MEDIUM
    }

    @TypeConverter
    fun fromIssueStatus(value: IssueStatus?): String = (value ?: IssueStatus.OPEN).name

    @TypeConverter
    fun toIssueStatus(value: String?): IssueStatus = try {
        IssueStatus.valueOf(value ?: IssueStatus.OPEN.name)
    } catch (e: Exception) {
        IssueStatus.OPEN
    }

    @TypeConverter
    fun fromEscalationLevel(value: EscalationLevel?): String = (value ?: EscalationLevel.L1_VOLUNTEER_COORDINATOR).name

    @TypeConverter
    fun toEscalationLevel(value: String?): EscalationLevel = try {
        EscalationLevel.valueOf(value ?: EscalationLevel.L1_VOLUNTEER_COORDINATOR.name)
    } catch (e: Exception) {
        EscalationLevel.L1_VOLUNTEER_COORDINATOR
    }

    @TypeConverter
    fun fromDeadlineType(value: DeadlineType?): String = (value ?: DeadlineType.SOFT_DEADLINE).name

    @TypeConverter
    fun toDeadlineType(value: String?): DeadlineType = try {
        DeadlineType.valueOf(value ?: DeadlineType.SOFT_DEADLINE.name)
    } catch (e: Exception) {
        DeadlineType.SOFT_DEADLINE
    }

    @TypeConverter
    fun fromProposalRecommendation(value: ProposalRecommendation?): String = (value ?: ProposalRecommendation.HOLD).name

    @TypeConverter
    fun toProposalRecommendation(value: String?): ProposalRecommendation = try {
        ProposalRecommendation.valueOf(value ?: ProposalRecommendation.HOLD.name)
    } catch (e: Exception) {
        ProposalRecommendation.HOLD
    }

    @TypeConverter
    fun fromUserRole(value: UserRole?): String = (value ?: UserRole.VOLUNTEER).name

    @TypeConverter
    fun toUserRole(value: String?): UserRole = try {
        UserRole.valueOf(value ?: UserRole.VOLUNTEER.name)
    } catch (e: Exception) {
        UserRole.VOLUNTEER
    }

    @TypeConverter
    fun fromApprovalType(value: ApprovalType?): String = (value ?: ApprovalType.EVENT_EXECUTION).name

    @TypeConverter
    fun toApprovalType(value: String?): ApprovalType = try {
        ApprovalType.valueOf(value ?: ApprovalType.EVENT_EXECUTION.name)
    } catch (e: Exception) {
        ApprovalType.EVENT_EXECUTION
    }

    @TypeConverter
    fun fromNotificationPriority(value: NotificationPriority?): String = (value ?: NotificationPriority.NORMAL).name

    @TypeConverter
    fun toNotificationPriority(value: String?): NotificationPriority = try {
        NotificationPriority.valueOf(value ?: NotificationPriority.NORMAL.name)
    } catch (e: Exception) {
        NotificationPriority.NORMAL
    }
}
