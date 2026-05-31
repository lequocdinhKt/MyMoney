package com.example.mymoney.domain.model

data class SavingGoalDetailModel(
    val goal: SavingGoalModel,
    val records: List<SavingRecordModel>,
    val progress: Float,
    val remainingAmount: Double
)