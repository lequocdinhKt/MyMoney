package com.example.mymoney.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saving_goals",
    indices = [
        Index(value = ["user_id", "name"], unique = true)
    ]
)
data class SavingGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "supabase_id") val supabaseId: String? = null,
    @ColumnInfo(name = "user_id") val userId: String,
    val name: String,
    @ColumnInfo(name = "currency") val currency: String = "VNĐ",
    @ColumnInfo(name = "saving_type") val savingType: String = "manual",
    @ColumnInfo(name = "target_amount") val targetAmount: Double,
    @ColumnInfo(name = "current_amount") val currentAmount: Double = 0.0,
    val icon: String? = null,
    val color: String? = null,
    @ColumnInfo(name = "target_date") val targetDate : Long? = null,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = SyncStatus.PENDING_INSERT
)