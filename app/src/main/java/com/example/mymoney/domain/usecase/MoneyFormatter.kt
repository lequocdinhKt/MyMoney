package com.example.mymoney.domain.usecase

import java.text.NumberFormat
import java.util.Locale

/**
 * Format số tiền VNĐ với dấu phân cách hàng nghìn.
 * Ví dụ: 1000000.0 → "1.000.000"
 */
object MoneyFormatter {
    private val formatter = NumberFormat.getNumberInstance(
        Locale.Builder().setLanguage("vi").setRegion("VN").build()
    ).apply {
        maximumFractionDigits = 0
        isGroupingUsed = true
    }

    fun format(amount: Double): String = formatter.format(Math.abs(amount))

    fun formatWithSign(amount: Double): String = when {
        amount > 0  -> "+${format(amount)}"
        amount < 0  -> "-${format(amount)}"
        else        -> "0"
    }

    fun formatBalance(amount: Double): String = "${format(amount)} vnđ"
}
