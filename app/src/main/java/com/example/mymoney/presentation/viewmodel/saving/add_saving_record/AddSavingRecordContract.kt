package com.example.mymoney.presentation.viewmodel.saving.add_saving_record

import com.example.mymoney.domain.model.WalletModel

data class AddSavingRecordUiState (
    val isLoading: Boolean = false,
    val note: String = "",
    val amount:  String = "",
    val currentDate: Long = System.currentTimeMillis(), // Hiện ngày hiện tại
    val wallets: List<WalletModel> = emptyList(),
    val selectedWalletId: Long? = null,
    val isSaved: Boolean = false,
    val error: String? = null
)

sealed interface AddSavingRecordEvent {
    data class OnWalletSelected(val walletId: Long) : AddSavingRecordEvent
    data class OnAmountChanged(val amount: String) : AddSavingRecordEvent
    data class OnNoteChanged(val note: String) : AddSavingRecordEvent
    data object SaveRecord : AddSavingRecordEvent
}