package com.example.mymoney.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "supabase_id") val supabaseId: String? = null,
    @ColumnInfo(name = "user_id") val userId: String,
    val name: String,
    val balance: Double = 0.0,
    val icon: String,
    val color: String,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = SyncStatus.PENDING_INSERT,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)

