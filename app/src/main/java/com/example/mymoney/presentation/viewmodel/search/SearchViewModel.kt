package com.example.mymoney.presentation.viewmodel.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.presentation.viewmodel.search.search.SearchEvent
import com.example.mymoney.presentation.viewmodel.search.search.SearchUiState
import com.example.mymoney.domain.usecase.GetTransactionsUseCase
import com.example.mymoney.presentation.viewmodel.search.search.FilterType
import java.time.Instant
import java.time.ZoneId



/**
 * View Model quản lý logic và trạng thái cho màn hình Tìm kiếm
 */
class SearchViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase
): ViewModel() {

    private val _uistate = MutableStateFlow(SearchUiState())

    /** State duy nhất mà UI observe */
    val uiState: StateFlow<SearchUiState> = _uistate.asStateFlow()

    /**
     * Biến chứa toàn bộ dữ liệu giao dịch (chưa lọc)
     * Không filter trực tiếp từ DB mỗi lần
     * Lưu lại để filter nhanh hơn
     * */
    private var allTransactions: List<TransactionModel> = emptyList()

    init {
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            getTransactionsUseCase().collect { list ->
                allTransactions = list
                filter(list)
            }
        }
    }

    /** Xử lý sự kiện từ UI */
    fun onEvent(event: SearchEvent) {
        when(event) {
            /**
             * Khi user nhập text -> Cập nhật lại query và lọc lại danh sách
             */
            is SearchEvent.onQueryChange -> {
                _uistate.update { it.copy(query = event.query) }
                filter(allTransactions)
            }

            /**
             * Khi user đổi bộ lọc -> Cập nhật lại filter và lọc lại dữ liệu
             */
            is SearchEvent.onFilterChange -> {
                _uistate.update { it.copy(selectedFilter = event.filter) }
                filter(allTransactions)
            }
        }
    }

    /** Hàm lọc */
    private fun filter(all: List<TransactionModel>) {
        val state = _uistate.value

        val result = all.filter {
            match(it, state.query, state.selectedFilter)
        }

        _uistate.update { it.copy(transactions = result) }
    }

    /** Hàm kiểm tra có khớp  */
    fun match(
        tx: TransactionModel,
        query: String,
        filter: FilterType
    ): Boolean {
        if (query.isBlank()) return true

        return when (filter) {
            FilterType.NAME -> {
                tx.note.contains(query, ignoreCase = true)
            }

            FilterType.CATEGORY -> {
                tx.category.contains(query, ignoreCase = true)
            }

            FilterType.DAY,
            FilterType.MONTH,
            FilterType.YEAR -> {
                matchDate(tx.timestamp, query, filter)
            }

            FilterType.All -> {
                tx.note.contains(query, true) ||
                        tx.category.contains(query, true)
            }
        }
    }

    fun matchDate(timestamp: Long, query: String, filter: FilterType): Boolean {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        return when (filter) {
            FilterType.DAY -> date.dayOfMonth.toString() == query
            FilterType.MONTH -> date.monthValue.toString() == query
            FilterType.YEAR -> date.year.toString() == query
            else -> false
        }

    }
}