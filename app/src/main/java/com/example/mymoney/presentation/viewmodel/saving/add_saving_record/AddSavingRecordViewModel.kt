package com.example.mymoney.presentation.viewmodel.saving.add_saving_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.domain.repository.WalletRepository
import com.example.mymoney.domain.usecase.AddSavingRecordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddSavingRecordViewModel(
    private val walletRepository: WalletRepository,
    private val addSavingRecordUseCase: AddSavingRecordUseCase,
    private val userId: String,
    private val goalId: Long,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddSavingRecordUiState())
    val uiState: StateFlow<AddSavingRecordUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            walletRepository.getWallets(userId).collect { wallets ->
                _uiState.update { it.copy(
                    wallets = wallets,
                    selectedWalletId = it.selectedWalletId ?: wallets.firstOrNull()?.id
                ) }
            }
        }
    }

    fun onEvent(event: AddSavingRecordEvent) {
        when(event) {
            is AddSavingRecordEvent.OnWalletSelected -> _uiState.update { it.copy(selectedWalletId = event.walletId) }
            is AddSavingRecordEvent.OnNoteChanged    -> _uiState.update { it.copy(note = event.note) }
            is AddSavingRecordEvent.OnAmountChanged  -> _uiState.update { it.copy(amount = event.amount) }
            is AddSavingRecordEvent.SaveRecord     -> saveRecord()
        }
    }

    private fun saveRecord() {
        val state = uiState.value

        val walletId = state.selectedWalletId ?: run {
            _uiState.update {
                it.copy(error = "Vui lòng chọn ví")
            }
            return
        }

        val amount = state.amount.toDoubleOrNull()

        if (amount == null) {
            _uiState.update {
                it.copy(error = "Số tiền không hợp lệ")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                addSavingRecordUseCase(
                    userId = userId,
                    goalId = goalId,
                    walletId = walletId,
                    amount = amount,
                    note = state.note
                )
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }

    }
}