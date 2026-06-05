package com.example.mymoney.presentation.viewmodel.recurring

import com.example.mymoney.domain.model.RecurringTransactionModel

// ─────────────────────────────────────────────────────────────────────────────
// Các tần suất giao dịch định kỳ
// ─────────────────────────────────────────────────────────────────────────────

enum class RecurringFrequency(val label: String, val code: String) {
    DAILY("Hàng ngày", "daily"),
    WEEKLY("Hàng tuần", "weekly"),
    MONTHLY("Hàng tháng", "monthly"),
    YEARLY("Hàng năm", "yearly");

    companion object {
        fun fromCode(code: String) = entries.firstOrNull { it.code == code } ?: MONTHLY
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class RecurringUiState(
    val items: List<RecurringTransactionModel> = emptyList(),
    val isLoading: Boolean = true,
    val walletName: String = "Ví chính",
    val errorMessage: String? = null,
    /** Bottom sheet hiện đang mở? */
    val isSheetOpen: Boolean = false,
    /** null = thêm mới, non-null = chỉnh sửa */
    val editingItem: RecurringTransactionModel? = null,
    // --- Form fields ---
    val formNote: String = "",
    val formAmount: String = "",
    val formType: String = "expense",   // "income" | "expense"
    val formCategory: String = "Khác",
    val formFrequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val formStartDate: Long = System.currentTimeMillis(),
    val formError: String? = null
) {
    val isEmpty: Boolean get() = items.isEmpty()
}

// ─────────────────────────────────────────────────────────────────────────────
// Events
// ─────────────────────────────────────────────────────────────────────────────

sealed class RecurringEvent {
    data object OnAddClicked : RecurringEvent()
    data class OnEditClicked(val item: RecurringTransactionModel) : RecurringEvent()
    data class OnToggleActive(val id: Long, val isActive: Boolean) : RecurringEvent()
    data class OnDeleteClicked(val id: Long) : RecurringEvent()
    data object OnSheetDismissed : RecurringEvent()

    // Form events
    data class OnFormNoteChanged(val note: String) : RecurringEvent()
    data class OnFormAmountChanged(val amount: String) : RecurringEvent()
    data class OnFormTypeChanged(val type: String) : RecurringEvent()
    data class OnFormCategoryChanged(val category: String) : RecurringEvent()
    data class OnFormFrequencyChanged(val frequency: RecurringFrequency) : RecurringEvent()
    data class OnFormStartDateChanged(val dateMs: Long) : RecurringEvent()
    data object OnFormSaveClicked : RecurringEvent()
}

// ─────────────────────────────────────────────────────────────────────────────
// Nav Events
// ─────────────────────────────────────────────────────────────────────────────

sealed class RecurringNavEvent {
    data object NavigateBack : RecurringNavEvent()
}

