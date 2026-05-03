package com.example.mymoney.presentation.viewmodel.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.domain.repository.WalletRepository
import com.example.mymoney.domain.usecase.GetPeriodSummaryUseCase
import com.example.mymoney.domain.usecase.GetTotalBalanceUseCase
import com.example.mymoney.domain.usecase.GetTransactionsByPeriodUseCase
import com.example.mymoney.domain.usecase.MoneyFormatter
import com.example.mymoney.domain.usecase.PeriodRangeUtil
import com.example.mymoney.presentation.viewmodel.home.home.HomeEvent
import com.example.mymoney.presentation.viewmodel.home.home.HomeUiState
import com.example.mymoney.presentation.viewmodel.home.home.TimePeriod
import com.example.mymoney.presentation.viewmodel.home.home.TransactionItem
import com.example.mymoney.presentation.viewmodel.home.home.WalletItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getTransactionsByPeriod: GetTransactionsByPeriodUseCase,
    private val getPeriodSummary: GetPeriodSummaryUseCase,
    private val getTotalBalance: GetTotalBalanceUseCase,
    private val walletRepository: WalletRepository,
    private val userId: String
) : ViewModel() {

    private val TAG = "HomeViewModel"

    private val _selectedPeriod = MutableStateFlow(TimePeriod.DAY)

    /** null = chưa chọn, tự động dùng ví đầu tiên */
    private val _selectedWalletIdOverride = MutableStateFlow<Long?>(null)

    /** null = chưa có range tùy chỉnh; dùng khi period == CUSTOM */
    private val _customRange = MutableStateFlow<Pair<Long, Long>?>(null)

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val walletsFlow = walletRepository.getWallets(userId)

        // Combine 3 flow điều khiển (period + wallet + customRange) thành 1 "params" flow
        val paramsFlow = combine(
            _selectedPeriod, _selectedWalletIdOverride, _customRange
        ) { period, walletId, customRange ->
            Triple(period, walletId, customRange)
        }

        // Combine params với wallets → flatMapLatest query giao dịch
        combine(paramsFlow, walletsFlow) { params, wallets ->
            Pair(params, wallets)
        }.flatMapLatest { (params, walletModels) ->
            val (period, overrideId, customRange) = params
            val selectedId = overrideId ?: walletModels.firstOrNull()?.id ?: 0L

            // Tính khoảng thời gian: dùng customRange khi period == CUSTOM và đã có range
            val range = if (period == TimePeriod.CUSTOM && customRange != null)
                PeriodRangeUtil.Range(customRange.first, customRange.second)
            else
                PeriodRangeUtil.getRangeFor(period)

            val label = if (period == TimePeriod.CUSTOM && customRange != null)
                PeriodRangeUtil.getCustomLabel(customRange.first, customRange.second)
            else
                PeriodRangeUtil.getLabelFor(period)

            combine(
                getTransactionsByPeriod.byWallet(userId, selectedId, range.from, range.to),
                getPeriodSummary.getIncomeByWallet(userId, selectedId, range.from, range.to),
                getPeriodSummary.getExpenseByWallet(userId, selectedId, range.from, range.to),
            ) { transactions, income, expense ->
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

                val walletItems = walletModels.map { w ->
                    WalletItem(id = w.id, name = w.name,
                        formattedBalance = MoneyFormatter.formatBalance(w.balance), color = w.color)
                }

                val selectedWallet = walletModels.find { it.id == selectedId }

                HomeUiState(
                    isLoading         = false,
                    balance           = selectedWallet?.balance?.toLong() ?: 0L,
                    formattedBalance  = MoneyFormatter.formatBalance(selectedWallet?.balance ?: 0.0),
                    walletName        = selectedWallet?.name ?: "",
                    wallets           = walletItems,
                    selectedWalletId  = selectedId,
                    activeWalletColor = selectedWallet?.color ?: "#0088F0",
                    selectedPeriod    = period,
                    groupLabel        = label,
                    totalIncome       = MoneyFormatter.format(income),
                    totalExpense      = MoneyFormatter.format(expense),
                    totalBalance      = MoneyFormatter.format(income - expense),
                    transactions      = items
                )
            }
        }
        .onEach { state -> _uiState.value = state }
        .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SelectPeriod -> _selectedPeriod.update { event.period }
            is HomeEvent.SelectCustomPeriod -> {
                // Set custom range rồi switch period → trigger requery
                _customRange.update { Pair(event.fromMs, event.toMs) }
                _uiState.update { it.copy(isLoading = true) }
                _selectedPeriod.update { TimePeriod.CUSTOM }
            }
            is HomeEvent.SelectWallet  -> {
                Log.d(TAG, "SelectWallet event: walletId=${event.walletId} (prev=${_selectedWalletIdOverride.value})")
                // Hiện skeleton toàn màn hình trong khi load dữ liệu ví mới
                _uiState.update { it.copy(isLoading = true) }
                _selectedWalletIdOverride.update { event.walletId }
            }
            is HomeEvent.ReorderWallets -> {
                viewModelScope.launch {
                    val orders = event.orderedIds.mapIndexed { index, id -> Pair(id, index) }
                    walletRepository.updateSortOrders(orders)
                }
            }
            is HomeEvent.AddTransactionClick -> { /* NavHost xử lý */ }
            is HomeEvent.AddWalletClick      -> { /* NavHost xử lý */ }
            is HomeEvent.EditWalletClick     -> { /* NavHost xử lý */ }
        }
    }

    private fun formatTimestamp(ts: Long): String {
        val instant  = java.time.Instant.ofEpochMilli(ts)
        val dateTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
        val hour = dateTime.hour.toString().padStart(2, '0')
        val min  = dateTime.minute.toString().padStart(2, '0')
        return "$hour:$min, ${dateTime.dayOfMonth.toString().padStart(2,'0')}/${dateTime.monthValue.toString().padStart(2,'0')}/${dateTime.year}"
    }
}
