package com.example.mymoney.domain.usecase

import java.text.NumberFormat
import java.util.Locale

/**
 * Format số tiền VNĐ với dấu phân cách hàng nghìn.
 * Ví dụ: 1000000.0 → "1.000.000"
 */
object MoneyFormatter {
    private val viLocale = Locale.Builder().setLanguage("vi").setRegion("VN").build()

    private fun getFormatter(useGrouping: Boolean): NumberFormat {
        return NumberFormat.getNumberInstance(viLocale).apply {
            maximumFractionDigits = 0
            isGroupingUsed = useGrouping
        }
    }

    fun format(amount: Double, useGrouping: Boolean = true): String {
        return getFormatter(useGrouping).format(Math.abs(amount))
    }

    fun formatWithSign(amount: Double, useGrouping: Boolean = true): String = when {
        amount > 0  -> "+${format(amount, useGrouping)}"
        amount < 0  -> "-${format(amount, useGrouping)}"
        else        -> "0"
    }

    fun formatBalance(amount: Double, useGrouping: Boolean = true): String =
        "${format(amount, useGrouping)} vnđ"

    /**
     * Format chuỗi nhập vào từ TextField.
     * Ví dụ: "1000" -> "1.000"
     */
    fun formatInput(input: String, useGrouping: Boolean): String {
        val cleanString = input.replace(".", "").replace(",", "")
        if (cleanString.isBlank()) return ""
        if (!useGrouping) return cleanString

        val parsed = cleanString.toDoubleOrNull() ?: return cleanString
        return getFormatter(true).format(parsed)
    }
}
