package com.example.mymoney.domain.model

data class SavingGoalModel(
    val id: Long = 0L,
    val userId: String,
    val title: String,
    val currency: String = "VNĐ",  // Mặc định là VNĐ
    val targetAmount: Double, // Số tiền mục tiêu nhắm đến
    val savingType: SavingType,
    val targetDate: Long? = null, // Nếu Null thì là Weekly/Monthly, không thì One_Time
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val supabaseId: String? = null
)

enum class SavingType {
    ONE_TIME,
    WEEKLY,
    MONTHLY
}