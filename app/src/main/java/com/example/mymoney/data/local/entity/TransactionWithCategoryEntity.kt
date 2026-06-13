package com.example.mymoney.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * POJO dùng để JOIN bảng transactions và categories.
 * Room sẽ tự động thực hiện query Relation dựa trên category_id.
 */
data class TransactionWithCategoryEntity(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
