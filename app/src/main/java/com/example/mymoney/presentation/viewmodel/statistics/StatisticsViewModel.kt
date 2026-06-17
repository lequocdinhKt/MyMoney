package com.example.mymoney.presentation.viewmodel.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.repository.TransactionRepository
import com.example.mymoney.presentation.viewmodel.home.home.TimePeriod
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class StatisticsViewModel(
    private val repository: TransactionRepository,
    private val userId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val calendar = Calendar.getInstance()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))

    init {
        loadData()
    }

    fun onEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.SelectTab -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
            }
            is StatisticsEvent.SelectType -> {
                _uiState.update { it.copy(selectedType = event.type) }
            }
            is StatisticsEvent.SelectPeriod -> {
                _uiState.update { it.copy(selectedPeriod = event.period, customRange = null) }
                loadData()
            }
            is StatisticsEvent.SelectCustomPeriod -> {
                _uiState.update { 
                    it.copy(
                        selectedPeriod = TimePeriod.CUSTOM,
                        customRange = Pair(event.from, event.to)
                    )
                }
                loadData()
            }
            is StatisticsEvent.TogglePeriodPicker -> {
                _uiState.update { it.copy(showPeriodPicker = !it.showPeriodPicker) }
            }
            is StatisticsEvent.OnBackClick -> {
                // Handled in UI
            }
        }
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        val (start, end) = getPeriodRange(_uiState.value.selectedPeriod, _uiState.value.customRange)
        
        // Cập nhật nhãn thời gian
        val label = getPeriodLabel(_uiState.value.selectedPeriod, start, end)
        _uiState.update { it.copy(periodLabel = label) }

        viewModelScope.launch {
            repository.getTransactionsWithCategoryByPeriod(userId, start, end)
                .collect { transactions ->
                    processTransactions(transactions)
                }
        }
        
        // Load dữ liệu xu hướng nếu đang ở tab TREND
        if (_uiState.value.selectedTab == StatisticsTab.TREND) {
            loadTrendData()
        }
    }

    private fun getPeriodLabel(period: TimePeriod, start: Long, end: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
        return when (period) {
            TimePeriod.DAY -> "Hôm nay, " + SimpleDateFormat("dd MMMM", Locale.forLanguageTag("vi-VN")).format(Date(start))
            TimePeriod.WEEK -> "Tuần này (${SimpleDateFormat("dd/MM", Locale.forLanguageTag("vi-VN")).format(Date(start))} - ${SimpleDateFormat("dd/MM", Locale.forLanguageTag("vi-VN")).format(Date(end))})"
            TimePeriod.MONTH -> SimpleDateFormat("'Th'MM yyyy", Locale.forLanguageTag("vi-VN")).format(Date(start))
            TimePeriod.YEAR -> SimpleDateFormat("yyyy", Locale.forLanguageTag("vi-VN")).format(Date(start))
            TimePeriod.CUSTOM -> "${sdf.format(Date(start))} - ${sdf.format(Date(end))}"
        }
    }

    private fun processTransactions(transactions: List<TransactionModel>) {
        val incomes = transactions.filter { it.type == "income" }
        val expenses = transactions.filter { it.type == "expense" }

        val totalIncome = incomes.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }

        val expenseByCat = expenses.groupBy { it.categoryId ?: -1L }
            .map { (catId, list) ->
                val amount = list.sumOf { it.amount }
                CategoryStatsItem(
                    categoryId = catId,
                    categoryName = list.first().category,
                    categoryIcon = list.first().categoryIcon,
                    amount = amount,
                    formattedAmount = formatCurrency(amount),
                    percentage = if (totalExpense > 0) (amount / totalExpense).toFloat() * 100 else 0f,
                    color = getCategoryColor(list.first().category)
                )
            }.sortedByDescending { it.amount }

        val incomeByCat = incomes.groupBy { it.categoryId ?: -1L }
            .map { (catId, list) ->
                val amount = list.sumOf { it.amount }
                CategoryStatsItem(
                    categoryId = catId,
                    categoryName = list.first().category,
                    categoryIcon = list.first().categoryIcon,
                    amount = amount,
                    formattedAmount = formatCurrency(amount),
                    percentage = if (totalIncome > 0) (amount / totalIncome).toFloat() * 100 else 0f,
                    color = getCategoryColor(list.first().category)
                )
            }.sortedByDescending { it.amount }

        _uiState.update {
            it.copy(
                isLoading = false,
                totalIncome = formatCurrency(totalIncome),
                totalExpense = formatCurrency(totalExpense),
                expenseCategories = expenseByCat,
                incomeCategories = incomeByCat
            )
        }
    }

    private fun loadTrendData() {
        viewModelScope.launch {
            // Get last 6 months trend
            val trendItems = mutableListOf<TrendStatsItem>()
            
            for (i in 5 downTo 0) {
                val tempCal = Calendar.getInstance()
                tempCal.add(Calendar.MONTH, -i)
                val start = getStartOfMonth(tempCal)
                val end = getEndOfOfMonth(tempCal)
                
                val sdf = SimpleDateFormat("ThMM", Locale("vi", "VN"))
                val label = sdf.format(tempCal.time)
                
                // We need a one-shot query or sum for this.
                // For simplicity, we'll use sumExpense from Dao via repository if available
                // But TransactionRepository doesn't have a suspend sumExpense, only Flow.
                // Let's use getTransactionsByPeriod and take the sum.
                repository.getTransactionsByPeriod(userId, start, end).first().let { list ->
                    val sum = list.filter { it.type == "expense" }.sumOf { it.amount }
                    trendItems.add(TrendStatsItem(
                        label = label,
                        amount = sum,
                        isSelected = i == 0
                    ))
                }
            }
            
            _uiState.update { 
                it.copy(
                    trendData = trendItems,
                    trendComparison = calculateTrendComparison(trendItems)
                ) 
            }
        }
    }

    private fun calculateTrendComparison(items: List<TrendStatsItem>): String {
        if (items.size < 2) return ""
        val current = items.last().amount
        val previous = items[items.size - 2].amount
        val diff = current - previous
        val absDiff = kotlin.math.abs(diff)
        val action = if (diff >= 0) "tăng" else "giảm"
        return "Chi tiêu $action ${formatCurrency(absDiff)} so với cùng kỳ tháng trước"
    }

    private fun getPeriodRange(period: TimePeriod, customRange: Pair<Long, Long>? = null): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        return when (period) {
            TimePeriod.DAY -> {
                val start = cal.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val end = cal.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                Pair(start, end)
            }
            TimePeriod.WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val start = getStartOfDay(cal)
                cal.add(Calendar.DAY_OF_WEEK, 6)
                val end = getEndOfDay(cal)
                Pair(start, end)
            }
            TimePeriod.MONTH -> {
                Pair(getStartOfMonth(cal), getEndOfOfMonth(cal))
            }
            TimePeriod.YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                val start = getStartOfDay(cal)
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                val end = getEndOfDay(cal)
                Pair(start, end)
            }
            TimePeriod.CUSTOM -> {
                customRange ?: Pair(0L, System.currentTimeMillis())
            }
        }
    }

    private fun getStartOfDay(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun getEndOfDay(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    private fun getStartOfMonth(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun getEndOfOfMonth(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount).replace("¤", "đ").trim()
    }

    private fun getCategoryColor(categoryName: String): String {
        return when (categoryName.lowercase()) {
            "mua sắm" -> "#FFD700" // Gold
            "ăn uống" -> "#FF6347" // Tomato
            "di chuyển" -> "#1E90FF" // DodgerBlue
            "chuyển tiền" -> "#FF7F50" // Coral
            "điện" -> "#FFA500" // Orange
            "nước" -> "#00CED1" // DarkTurquoise
            "nhà cửa" -> "#8B4513" // SaddleBrown
            "giáo dục" -> "#9370DB" // MediumPurple
            "sức khỏe" -> "#32CD32" // LimeGreen
            "giải trí" -> "#FF69B4" // HotPink
            else -> {
                // Tạo màu ngẫu nhiên dựa trên tên để nhất quán
                val hash = categoryName.hashCode()
                val r = (hash and 0xFF0000 shr 16) % 200 + 55
                val g = (hash and 0x00FF00 shr 8) % 200 + 55
                val b = (hash and 0x0000FF) % 200 + 55
                String.format("#%02x%02x%02x", r, g, b)
            }
        }
    }
}
