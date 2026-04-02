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
    val note: String,
    val amount: Double,
    val type: String,
    val category: String,
    val walletId: Long = 0L,   // 0 = chưa gán ví, > 0 = ID ví trong Room
    val timestamp: Long
) {
    fun toDomain(): TransactionModel = TransactionModel(
        id        = id,
        note      = note,
        amount    = amount,
        type      = type,
        category  = category,
        walletId  = walletId,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(model: TransactionModel): TransactionEntity = TransactionEntity(
            id        = model.id,
            note      = model.note,
            amount    = model.amount,
            type      = model.type,
            category  = model.category,
            walletId  = model.walletId,
            timestamp = model.timestamp
        )
    }
}
