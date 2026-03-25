package com.example.mymoney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mymoney.domain.model.TransactionModel

/**
 * Room Entity cho bảng "transactions".
 * Ánh xạ 1:1 với [TransactionModel] (Domain model).
 *
 * Mapper functions nằm ngay trong class này để tiện sử dụng:
 *   - toDomain(): Entity → Domain model
 *   - fromDomain(): Domain model → Entity (companion)
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Ghi chú / mô tả giao dịch */
    val note: String,

    /** Số tiền (VNĐ). Dương = thu, Âm = chi */
    val amount: Double,

    /** Loại: "income" hoặc "expense" */
    val type: String,

    /** Danh mục giao dịch */
    val category: String,

    /** Thời điểm tạo giao dịch (epoch millis) */
    val timestamp: Long
) {
    // ── Mapper: Entity → Domain ──
    fun toDomain(): TransactionModel = TransactionModel(
        id = id,
        note = note,
        amount = amount,
        type = type,
        category = category,
        timestamp = timestamp
    )

    companion object {
        // ── Mapper: Domain → Entity ──
        fun fromDomain(model: TransactionModel): TransactionEntity = TransactionEntity(
            id = model.id,
            note = model.note,
            amount = model.amount,
            type = model.type,
            category = model.category,
            timestamp = model.timestamp
        )
    }
}
