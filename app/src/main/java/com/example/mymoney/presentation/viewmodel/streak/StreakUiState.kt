package com.example.mymoney.presentation.viewmodel.streak

import com.example.mymoney.domain.model.TransactionModel
import java.time.LocalDate
import java.time.YearMonth

/**
 * Tóm tắt giao dịch trong một ngày.
 */
data class DaySummary(
    val date: LocalDate,
    val transactions: List<TransactionModel>,
    val income: Double,
    val expense: Double
) {
    val net: Double get() = income - expense
    val hasTransactions: Boolean get() = transactions.isNotEmpty()
}

/**
 * UI State cho màn hình Chuỗi ngày (Streak).
 */
data class StreakUiState(
    val isLoading: Boolean = true,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val todayTransactionCount: Int = 0,
    val displayMonth: YearMonth = YearMonth.now(),
    val daySummaryMap: Map<LocalDate, DaySummary> = emptyMap(),
    val selectedDate: LocalDate? = null
) {
    val selectedDaySummary: DaySummary?
        get() = selectedDate?.let { daySummaryMap[it] }
}

