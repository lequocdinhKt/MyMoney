package com.example.mymoney.presentation.viewmodel.statistics

import com.example.mymoney.presentation.viewmodel.home.home.TimePeriod

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val selectedTab: StatisticsTab = StatisticsTab.DISTRIBUTION,
    val selectedType: String = "expense",
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val customRange: Pair<Long, Long>? = null,
    val periodLabel: String = "",
    val totalIncome: String = "0",
    val totalExpense: String = "0",
    val expenseCategories: List<CategoryStatsItem> = emptyList(),
    val incomeCategories: List<CategoryStatsItem> = emptyList(),
    val trendData: List<TrendStatsItem> = emptyList(),
    val trendComparison: String = "",
    val trendSummary: String = "",
    val showPeriodPicker: Boolean = false
)

enum class StatisticsTab {
    DISTRIBUTION, // Phân bổ
    TREND         // Xu hướng
}

data class CategoryStatsItem(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val amount: Double,
    val formattedAmount: String,
    val percentage: Float,
    val color: String,
    val changeAmount: String = "" // For trend view
)

data class TrendStatsItem(
    val label: String, // Th04, Th05, Th06
    val amount: Double,
    val isSelected: Boolean = false
)

sealed interface StatisticsEvent {
    data class SelectTab(val tab: StatisticsTab) : StatisticsEvent
    data class SelectType(val type: String) : StatisticsEvent
    data class SelectPeriod(val period: TimePeriod) : StatisticsEvent
    data class SelectCustomPeriod(val from: Long, val to: Long) : StatisticsEvent
    data object TogglePeriodPicker : StatisticsEvent
    data object OnBackClick : StatisticsEvent
}
