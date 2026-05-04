package com.example.mymoney.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["wallet_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("wallet_id"), Index("category_id")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "supabase_id") val supabaseId: String? = null,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "wallet_id") val walletId: Long,
    /** null = category không được liên kết (ví dụ: AI chưa seed categories) */
    @ColumnInfo(name = "category_id") val categoryId: Long? = null,
    /** Tên category dạng chuỗi — lưu luôn để hiển thị mà không cần JOIN */
    @ColumnInfo(name = "category_name") val categoryName: String = "",
    val amount: Double,
    val type: String,
    val note: String = "",
    @ColumnInfo(name = "transaction_date") val transactionDate: Long,
    @ColumnInfo(name = "ai_generated") val aiGenerated: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "sync_status") val syncStatus: Int = SyncStatus.PENDING_INSERT,
    @ColumnInfo(name = "image_path") val imagePath: String? = null
)

