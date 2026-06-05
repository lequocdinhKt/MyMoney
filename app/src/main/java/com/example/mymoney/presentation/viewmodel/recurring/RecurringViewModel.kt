package com.example.mymoney.presentation.viewmodel.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.dao.RecurringTransactionDao
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.entity.RecurringTransactionEntity
import com.example.mymoney.domain.model.RecurringTransactionModel
import com.example.mymoney.domain.repository.WalletRepository
import com.example.mymoney.domain.usecase.EnsureDefaultWalletUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class RecurringViewModel(
    private val recurringDao: RecurringTransactionDao,
    private val walletRepository: WalletRepository,
    private val ensureDefaultWallet: EnsureDefaultWalletUseCase,
    private val settingPreferences: SettingPreferences,
    private val selectedWalletId: Long = 0L
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    private val _navEvent = MutableSharedFlow<RecurringNavEvent>()
    val navEvent: SharedFlow<RecurringNavEvent> = _navEvent.asSharedFlow()

    private var userId: String = ""

    init {
        viewModelScope.launch {
            userId = settingPreferences.currentUserId.first() ?: return@launch

            // Load wallet name
            val wallet = if (selectedWalletId != 0L) {
                runCatching { walletRepository.getWalletById(selectedWalletId) }.getOrNull()
                    ?: runCatching { ensureDefaultWallet(userId) }.getOrNull()
            } else {
                runCatching { ensureDefaultWallet(userId) }.getOrNull()
            }
            if (wallet != null) {
                _uiState.update { it.copy(walletName = wallet.name) }
            }

            // Observe recurring transactions
            recurringDao.observeAll(userId).collect { entities ->
                val models = entities.map { it.toModel() }
                _uiState.update {
                    it.copy(items = models, isLoading = false)
                }
            }
        }
    }

    fun onEvent(event: RecurringEvent) {
        when (event) {
            is RecurringEvent.OnAddClicked         -> openSheet(null)
            is RecurringEvent.OnEditClicked        -> openSheet(event.item)
            is RecurringEvent.OnToggleActive       -> toggleActive(event.id, event.isActive)
            is RecurringEvent.OnDeleteClicked      -> deleteItem(event.id)
            is RecurringEvent.OnSheetDismissed     -> closeSheet()
            is RecurringEvent.OnFormNoteChanged    -> _uiState.update { it.copy(formNote = event.note) }
            is RecurringEvent.OnFormAmountChanged  -> _uiState.update { it.copy(formAmount = event.amount) }
            is RecurringEvent.OnFormTypeChanged    -> _uiState.update { it.copy(formType = event.type) }
            is RecurringEvent.OnFormCategoryChanged -> _uiState.update { it.copy(formCategory = event.category) }
            is RecurringEvent.OnFormFrequencyChanged -> _uiState.update { it.copy(formFrequency = event.frequency) }
            is RecurringEvent.OnFormStartDateChanged -> _uiState.update { it.copy(formStartDate = event.dateMs) }
            is RecurringEvent.OnFormSaveClicked    -> saveForm()
        }
    }

    // ──────────────────────────────────────────────────────────────────────────

    private fun openSheet(item: RecurringTransactionModel?) {
        _uiState.update { state ->
            state.copy(
                isSheetOpen  = true,
                editingItem  = item,
                formNote     = item?.note ?: "",
                formAmount   = if (item != null) formatAmount(item.amount) else "",
                formType     = item?.type ?: "expense",
                formCategory = item?.categoryName ?: "Khác",
                formFrequency = RecurringFrequency.fromCode(item?.frequency ?: "monthly"),
                formStartDate = item?.startDate ?: System.currentTimeMillis(),
                formError    = null
            )
        }
    }

    private fun closeSheet() {
        _uiState.update { it.copy(isSheetOpen = false, editingItem = null, formError = null) }
    }

    private fun toggleActive(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            runCatching { recurringDao.setActive(id, isActive) }
        }
    }

    private fun deleteItem(id: Long) {
        viewModelScope.launch {
            runCatching { recurringDao.delete(id) }
        }
    }

    private fun saveForm() {
        val state = _uiState.value
        val note = state.formNote.trim()
        val amount = state.formAmount.trim().replace(".", "").replace(",", ".").toDoubleOrNull()

        if (note.isBlank()) {
            _uiState.update { it.copy(formError = "Vui lòng nhập tên giao dịch") }
            return
        }
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(formError = "Vui lòng nhập số tiền hợp lệ") }
            return
        }

        viewModelScope.launch {
            val walletId = if (selectedWalletId != 0L) selectedWalletId
            else runCatching { ensureDefaultWallet(userId) }.getOrNull()?.id ?: 0L

            val nextDue = computeNextDue(state.formStartDate, state.formFrequency)

            val entity = if (state.editingItem != null) {
                // Update existing
                RecurringTransactionEntity(
                    id           = state.editingItem.id,
                    userId       = userId,
                    walletId     = walletId,
                    categoryName = state.formCategory,
                    amount       = amount,
                    type         = state.formType,
                    note         = note,
                    frequency    = state.formFrequency.code,
                    startDate    = state.formStartDate,
                    nextDueDate  = nextDue,
                    isActive     = state.editingItem.isActive,
                    createdAt    = state.editingItem.createdAt
                )
            } else {
                // Insert new
                RecurringTransactionEntity(
                    userId       = userId,
                    walletId     = walletId,
                    categoryName = state.formCategory,
                    amount       = amount,
                    type         = state.formType,
                    note         = note,
                    frequency    = state.formFrequency.code,
                    startDate    = state.formStartDate,
                    nextDueDate  = nextDue
                )
            }

            runCatching { recurringDao.insert(entity) }.onFailure {
                _uiState.update { s -> s.copy(formError = "Lưu thất bại: ${it.message}") }
                return@launch
            }
            closeSheet()
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Tính nextDueDate = startDate nếu trong tương lai, ngược lại tính lần kế tiếp từ hôm nay */
    private fun computeNextDue(startDate: Long, frequency: RecurringFrequency): Long {
        val now = System.currentTimeMillis()
        if (startDate >= now) return startDate

        val cal = Calendar.getInstance().apply { timeInMillis = startDate }
        while (cal.timeInMillis < now) {
            when (frequency) {
                RecurringFrequency.DAILY   -> cal.add(Calendar.DAY_OF_YEAR, 1)
                RecurringFrequency.WEEKLY  -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                RecurringFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
                RecurringFrequency.YEARLY  -> cal.add(Calendar.YEAR, 1)
            }
        }
        return cal.timeInMillis
    }

    private fun formatAmount(amount: Double): String =
        String.format(Locale.US, "%.0f", amount)

    private fun RecurringTransactionEntity.toModel() = RecurringTransactionModel(
        id           = id,
        userId       = userId,
        walletId     = walletId,
        categoryName = categoryName,
        amount       = amount,
        type         = type,
        note         = note,
        frequency    = frequency,
        startDate    = startDate,
        nextDueDate  = nextDueDate,
        isActive     = isActive,
        createdAt    = createdAt
    )
}

