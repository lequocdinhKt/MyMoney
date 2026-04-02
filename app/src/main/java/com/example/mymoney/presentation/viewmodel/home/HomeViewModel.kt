package com.example.mymoney.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.domain.usecase.GetPeriodSummaryUseCase
import com.example.mymoney.domain.usecase.GetTotalBalanceUseCase
import com.example.mymoney.domain.usecase.GetTransactionsByPeriodUseCase
import com.example.mymoney.domain.usecase.MoneyFormatter
import com.example.mymoney.domain.usecase.PeriodRangeUtil
import com.example.mymoney.presentation.viewmodel.home.home.HomeEvent
import com.example.mymoney.presentation.viewmodel.home.home.HomeUiState
import com.example.mymoney.presentation.viewmodel.home.home.TimePeriod
import com.example.mymoney.presentation.viewmodel.home.home.TransactionItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel màn hình Trang chủ — dùng data thật từ Room.
 *
 * Luồng:
 *  1. selectedPeriod thay đổi → tính lại range → flatMapLatest query Room
 *  2. transactions + income + expense + balance → combine → emit HomeUiState
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getTransactionsByPeriod: GetTransactionsByPeriodUseCase,
    private val getPeriodSummary: GetPeriodSummaryUseCase,
    private val getTotalBalance: GetTotalBalanceUseCase,
    private val userId: String
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.DAY)

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        // Khi period thay đổi → tính range → query Room
        _selectedPeriod
            .flatMapLatest { period ->
                val range = PeriodRangeUtil.getRangeFor(period)
                val label = PeriodRangeUtil.getLabelFor(period)

                combine(
                    getTransactionsByPeriod(range.from, range.to),
                    getPeriodSummary.getIncome(range.from, range.to),
                    getPeriodSummary.getExpense(range.from, range.to),
                    getTotalBalance(userId)
                ) { transactions, income, expense, totalBalance ->

                    val items = transactions.map { model ->
                        val amountVal = if (model.type == "income") model.amount else -model.amount
                        TransactionItem(
                            id              = model.id.toString(),
                            categoryIconRes = null,
                            title           = model.note.ifBlank { model.category },
                            dateTime        = formatTimestamp(model.timestamp),
                            amount          = amountVal.toLong(),
                            formattedAmount = MoneyFormatter.formatWithSign(amountVal)
                        )
                    }

                    HomeUiState(
                        isLoading        = false,
                        balance          = totalBalance.toLong(),
                        formattedBalance = MoneyFormatter.formatBalance(totalBalance),
                        selectedPeriod   = period,
                        groupLabel       = label,
                        totalIncome      = MoneyFormatter.format(income),
                        totalExpense     = MoneyFormatter.format(expense),
                        totalBalance     = MoneyFormatter.format(totalBalance),
                        transactions     = items
                    )
                }
            }
            .onEach { state -> _uiState.value = state }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SelectPeriod -> _selectedPeriod.update { event.period }
            is HomeEvent.AddTransactionClick -> { /* điều hướng xử lý từ NavHost */ }
        }
    }

    private fun formatTimestamp(ts: Long): String {
        val instant  = java.time.Instant.ofEpochMilli(ts)
        val dateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
        val hour     = dateTime.hour.toString().padStart(2, '0')
        val min      = dateTime.minute.toString().padStart(2, '0')
        return "$hour:$min, ${dateTime.dayOfMonth.toString().padStart(2,'0')}/${dateTime.monthValue.toString().padStart(2,'0')}/${dateTime.year}"
    }
}
