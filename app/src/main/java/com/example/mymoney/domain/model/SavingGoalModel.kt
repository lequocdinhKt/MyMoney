package com.example.mymoney.domain.model

data class SavingGoalModel(
    val id: Long = 0L,
    val userId: String,
    val name: String,
    val currency: String = "VNĐ",
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val savingType: SavingType,
    val icon: String? = null,
    val color: String? = null,
    val targetDate: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val supabaseId: String? = null
)

enum class SavingType {
    ONE_TIME,
    WEEKLY,
    MONTHLY
}
