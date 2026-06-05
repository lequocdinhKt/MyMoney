package com.example.mymoney.presentation.viewmodel.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.model.WalletModel
import com.example.mymoney.domain.repository.WalletRepository
import com.example.mymoney.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Thiết lập ví.
 *
 * Chế độ:
 *  - Tạo mới : walletId == null  → name rỗng, balance = 0, màu mặc định
 *  - Chỉnh sửa: walletId != null → load dữ liệu từ DB rồi điền vào form
 */
class WalletSetupViewModel(
    private val walletRepository: WalletRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val userId: String,
    private val walletId: Long?             // null = tạo mới
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletSetupUiState())
    val uiState: StateFlow<WalletSetupUiState> = _uiState.asStateFlow()

    init {
        if (walletId != null) {
            loadWallet(walletId)
        }
    }

    private fun loadWallet(id: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val wallet = walletRepository.getWallets(userId).first()
                    .firstOrNull { it.id == id }
                if (wallet != null) {
                    _uiState.update {
                        it.copy(
                            id              = wallet.id,
                            isEditMode      = true,
                            name            = wallet.name,
                            initialBalance  = wallet.balance.toLong().toString(),
                            originalBalance = wallet.balance,
                            selectedColor   = wallet.color,
                            isDefault       = wallet.isDefault,
                            isLoading       = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, isEditMode = true, id = id) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: WalletSetupEvent) {
        when (event) {
            is WalletSetupEvent.NameChanged    -> _uiState.update { it.copy(name = event.name) }
            is WalletSetupEvent.BalanceChanged -> _uiState.update { it.copy(initialBalance = event.balance) }
            is WalletSetupEvent.ColorChanged   -> _uiState.update { it.copy(selectedColor = event.color) }
            is WalletSetupEvent.DefaultChanged -> _uiState.update { it.copy(isDefault = event.isDefault) }
            is WalletSetupEvent.Submit         -> submit()
            is WalletSetupEvent.DeleteConfirm  -> delete()
            is WalletSetupEvent.DeleteDismissed -> _uiState.update { it.copy(showDeleteDialog = false) }
            is WalletSetupEvent.DismissError   -> _uiState.update { it.copy(error = null) }
            is WalletSetupEvent.DeleteClicked  -> {
                _uiState.update { it.copy(showDeleteDialog = true) }
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Tên ví không được để trống") }
            return
        }
        val balance = state.initialBalance.replace(".", "").replace(",", "").toDoubleOrNull() ?: 0.0

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                if (state.isEditMode) {
                    walletRepository.updateWallet(
                        WalletModel(
                            id        = state.id,
                            userId    = userId,
                            name      = state.name.trim(),
                            balance   = balance,
                            icon      = "wallet",
                            color     = state.selectedColor,
                            isDefault = state.isDefault,
                            updatedAt = now
                        )
                    )
                    // Ghi transaction khi số dư thay đổi
                    val diff = balance - state.originalBalance
                    if (diff != 0.0) {
                        runCatching {
                            addTransactionUseCase(
                                TransactionModel(
                                    userId    = userId,
                                    note      = if (diff > 0) "Ví tăng số dư" else "Ví giảm số dư",
                                    amount    = Math.abs(diff),
                                    type      = if (diff > 0) "income" else "expense",
                                    category  = if (diff > 0) "Thu nhập" else "Chi tiêu",
                                    walletId  = state.id,
                                    timestamp = now
                                )
                            )
                        }
                    }
                } else {
                    val newWalletId = walletRepository.addWallet(
                        WalletModel(
                            userId    = userId,
                            name      = state.name.trim(),
                            balance   = balance,
                            icon      = "wallet",
                            color     = state.selectedColor,
                            isDefault = state.isDefault,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                    // Ghi transaction "income - Tạo ví" nếu số dư ban đầu > 0
                    if (balance > 0) {
                        runCatching {
                            addTransactionUseCase(
                                TransactionModel(
                                    userId      = userId,
                                    note        = "Tạo ví",
                                    amount      = balance,
                                    type        = "income",
                                    category    = "Thu nhập",
                                    walletId    = newWalletId,
                                    timestamp   = now
                                )
                            )
                        }
                    }
                }
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Lưu thất bại: ${e.message}") }
            }
        }
    }

    private fun delete() {
        val state = _uiState.value
        if (!state.isEditMode || state.isDeleting) return

        _uiState.update { it.copy(
            isDeleting = true,
            showDeleteDialog = false,
            error = null
        ) }

        viewModelScope.launch {
            try {
                // Nếu xóa ví mặc định, cần chuyển ví khác thành mặc định
                if (state.isDefault) {
                    val allWallets = walletRepository.getWallets(userId).first()
                        .filterNot { it.id == state.id }  // loại bỏ ví sắp xóa

                    if (allWallets.isEmpty()) {
                        // Đây là ví duy nhất → không cho xóa
                        _uiState.update {
                            it.copy(
                                isDeleting = false,
                                error = "Phải có ít nhất một ví. Vui lòng tạo ví khác trước khi xóa ví này."
                            )
                        }
                        return@launch
                    }

                    // Chuyển ví đầu tiên (sau khi loại bỏ ví hiện tại) thành mặc định
                    val nextDefault = allWallets.first().copy(isDefault = true)
                    walletRepository.updateWallet(nextDefault)
                }

                walletRepository.deleteWallet(state.id)

                _uiState.update { it.copy(
                    isDeleting = false,
                    isSaved = true
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isDeleting = false,
                    error = "Xóa thất bại: ${e.message}"
                ) }
            }
        }
    }
}
