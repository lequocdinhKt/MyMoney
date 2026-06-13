package com.example.mymoney.domain.model

data class SavingGoalDetailModel(
    val goal: SavingGoalModel,
    val records: List<SavingRecordModel>,
    val totalSavedAllTime: Double,
    val progress: Float,
    val remainingAmount: Double,
    val daysRemaining: Long? = null,
    val currentCycleSaved: Double,  // Chu kỳ hiện tại
    // Hiển thị ngày đầu - cuối của 1 chu kỳ
    val currentCycleStart: Long? = null,
    val currentCycleEnd: Long? = null
)