package com.example.mymoney.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Giao dịch định kỳ — lặp lại theo tần suất đã cài đặt.
 * Worker sẽ chạy hàng ngày, kiểm tra nextDueDate và tự tạo giao dịch nếu đến hạn.
 */
@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["wallet_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wallet_id"), Index("user_id")]
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "wallet_id") val walletId: Long,
    @ColumnInfo(name = "category_name") val categoryName: String = "Khác",
    val amount: Double,
    /** "income" | "expense" */
    val type: String,
    val note: String = "",
    /** "daily" | "weekly" | "monthly" | "yearly" */
    val frequency: String,
    @ColumnInfo(name = "start_date") val startDate: Long,
    /** Ngày hạn tiếp theo để tạo giao dịch. Worker sẽ cập nhật sau khi xử lý. */
    @ColumnInfo(name = "next_due_date") val nextDueDate: Long,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

