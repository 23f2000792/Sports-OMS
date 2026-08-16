package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY lastUpdated DESC, id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

@Dao
interface SportsEventDao {
    @Query("SELECT * FROM events ORDER BY id ASC")
    fun getAllEvents(): Flow<List<SportsEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(event: SportsEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<SportsEventEntity>)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM events")
    suspend fun deleteAll()
}

@Dao
interface ReadinessDao {
    @Query("SELECT * FROM readiness_requirements ORDER BY eventId ASC, phaseNumber ASC, id ASC")
    fun getAllRequirements(): Flow<List<ReadinessRequirementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(req: ReadinessRequirementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reqs: List<ReadinessRequirementEntity>)

    @Query("DELETE FROM readiness_requirements")
    suspend fun deleteAll()
}

@Dao
interface IssueDao {
    @Query("SELECT * FROM issues ORDER BY lastUpdated DESC, dateRaised DESC")
    fun getAllIssues(): Flow<List<IssueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(issue: IssueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(issues: List<IssueEntity>)

    @Query("DELETE FROM issues WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM issues")
    suspend fun deleteAll()
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_items ORDER BY date ASC, time ASC")
    fun getAllCalendarItems(): Flow<List<CalendarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: CalendarEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CalendarEntity>)

    @Query("DELETE FROM calendar_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM calendar_items")
    suspend fun deleteAll()
}

@Dao
interface ApprovalDao {
    @Query("SELECT * FROM approvals ORDER BY requestedDate DESC, id DESC")
    fun getAllApprovals(): Flow<List<ApprovalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: ApprovalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ApprovalEntity>)

    @Query("DELETE FROM approvals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM approvals")
    suspend fun deleteAll()
}

@Dao
interface ProposalReviewDao {
    @Query("SELECT * FROM proposal_reviews ORDER BY id ASC")
    fun getAllReviews(): Flow<List<ProposalReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(review: ProposalReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<ProposalReviewEntity>)

    @Query("DELETE FROM proposal_reviews")
    suspend fun deleteAll()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC, id DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<AuditLogEntity>)

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAll()
}
