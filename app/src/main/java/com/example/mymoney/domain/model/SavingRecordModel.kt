package com.example.mymoney.domain.model

data class SavingRecordModel(
    val id: Long = 0L,
    val userId: String,
    val savingGoalId: Long,
    val amount: Double,
    val note: String? = null,
    val recordDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)