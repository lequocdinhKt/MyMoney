package com.example.mymoney.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saving_records",
    foreignKeys = [
        ForeignKey(
            entity = SavingGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["saving_goal_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("saving_goal_id")
    ]
)
data class SavingRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "supabase_id") val supabaseId: String? = null,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "saving_goal_id") val savingGoalId: Long,
    val amount: Double,
    val note: String?,
    @ColumnInfo(name = "record_date") val recordDate: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = SyncStatus.PENDING_INSERT
)