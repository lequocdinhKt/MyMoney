package com.example.mymoney.presentation.viewmodel.wallet

import androidx.compose.runtime.Immutable

/** Danh sách màu preset cho ví */
val WALLET_PRESET_COLORS = listOf(
    "#FF6B9D", // Hồng Phấn
    "#FF9F6B", // Cam Past
    "#FFD700", // Vàng Pasty
    "#6BCB77", // Xanh Pastel
    "#4D96FF", // Xanh Dương
    "#C77DFF", // Tím Pasty
    "#0088F0", // Xanh dương đậm (mặc định)
    "#00B4A0", // Xanh ngọc
)

@Immutable
data class WalletSetupUiState(
    val id: Long = 0L,
    val isEditMode: Boolean = false,
    val name: String = "",
    val initialBalance: String = "0",
    val originalBalance: Double = 0.0,   // số dư gốc khi load, để so sánh khi lưu
    val selectedColor: String = "#0088F0",
    val isDefault: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

sealed interface WalletSetupEvent {
    data class NameChanged(val name: String) : WalletSetupEvent
    data class BalanceChanged(val balance: String) : WalletSetupEvent
    data class ColorChanged(val color: String) : WalletSetupEvent
    data class DefaultChanged(val isDefault: Boolean) : WalletSetupEvent
    data object Submit : WalletSetupEvent
    data object DismissError : WalletSetupEvent
}

