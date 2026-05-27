package com.example.mymoney.ui.common

import androidx.compose.runtime.compositionLocalOf
import com.example.mymoney.presentation.viewmodel.setting.setting.NumberFormat

/**
 * Cấu hình định dạng số tiền, lấy từ DataStore settings.
 * Cung cấp qua CompositionLocal để mọi Composable trong cây đều dùng được.
 */
data class MoneyFormatConfig(
    val useThousandSep: Boolean = true,
    val numberFormat: NumberFormat = NumberFormat.DOT
)

val LocalMoneyFormatConfig = compositionLocalOf { MoneyFormatConfig() }

