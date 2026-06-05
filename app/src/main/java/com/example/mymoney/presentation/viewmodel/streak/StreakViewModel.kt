package com.example.mymoney.presentation.viewmodel.streak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StreakViewModel(
    private val transactionRepository: TransactionRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreakUiState())
    val uiState: StateFlow<StreakUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions(userId).collect { transactions ->
                val zone = ZoneId.systemDefault()
                // Group by date
                val daySummaryMap = transactions
                    .groupBy { tx ->
                        Instant.ofEpochMilli(tx.timestamp).atZone(zone).toLocalDate()
                    }
                    .mapValues { (date, txList) ->
                        DaySummary(
                            date = date,
                            transactions = txList.sortedByDescending { it.timestamp },
                            income = txList.filter { it.type == "income" }.sumOf { it.amount },
                            expense = txList.filter { it.type == "expense" }.sumOf { it.amount }
                        )
                    }

                val today = LocalDate.now()
                val currentStreak = calcCurrentStreak(daySummaryMap, today)
                val longestStreak = calcLongestStreak(daySummaryMap)
                val todayCount = daySummaryMap[today]?.transactions?.size ?: 0

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        daySummaryMap = daySummaryMap,
                        currentStreak = currentStreak,
                        longestStreak = longestStreak,
                        todayTransactionCount = todayCount
                    )
                }
            }
        }
    }

    /** Streak hiện tại: đếm lùi từ hôm nay. Nếu hôm nay chưa có giao dịch, đếm từ hôm qua. */
    private fun calcCurrentStreak(map: Map<LocalDate, DaySummary>, today: LocalDate): Int {
        var streak = 0
        var date = if (map.containsKey(today)) today else today.minusDays(1)
        while (map.containsKey(date)) {
            streak++
            date = date.minusDays(1)
        }
        return streak
    }

    /** Streak dài nhất từ trước đến nay */
    private fun calcLongestStreak(map: Map<LocalDate, DaySummary>): Int {
        if (map.isEmpty()) return 0
        val sorted = map.keys.sorted()
        var longest = 1
        var current = 1
        for (i in 1 until sorted.size) {
            if (sorted[i] == sorted[i - 1].plusDays(1)) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }

    fun onDaySelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onDayDismissed() {
        _uiState.update { it.copy(selectedDate = null) }
    }

    fun onPreviousMonth() {
        _uiState.update { it.copy(displayMonth = it.displayMonth.minusMonths(1)) }
    }

    fun onNextMonth() {
        _uiState.update { it.copy(displayMonth = it.displayMonth.plusMonths(1)) }
    }
}

