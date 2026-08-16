package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TaskEntity::class,
        SportsEventEntity::class,
        ReadinessRequirementEntity::class,
        IssueEntity::class,
        CalendarEntity::class,
        ApprovalEntity::class,
        ProposalReviewEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun eventDao(): SportsEventDao
    abstract fun readinessDao(): ReadinessDao
    abstract fun issueDao(): IssueDao
    abstract fun calendarDao(): CalendarDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun proposalReviewDao(): ProposalReviewDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sports_ops_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
