package com.example.mymoney.domain.usecase

import com.example.mymoney.presentation.viewmodel.setting.setting.NumberFormat
import java.text.NumberFormat as JavaNumberFormat
import java.util.Locale

/**
 * Format số tiền VNĐ theo cấu hình người dùng.
 */
object MoneyFormatter {

    // Vietnamese Locale: dấu phân cách nghìn = ".", thập phân = ","  -> 1.000.000
    private val dotFormatter = JavaNumberFormat.getNumberInstance(
        Locale.Builder().setLanguage("vi").setRegion("VN").build()
    ).apply {
        maximumFractionDigits = 0
        isGroupingUsed = true
    }

    // English Locale: dấu phân cách nghìn = ",", -> 1,000,000
    private val commaFormatter = JavaNumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
        isGroupingUsed = true
    }

    // ── API có settings (dùng cho toàn app) ──────────────────────────────────

    fun format(amount: Double, useThousandSep: Boolean, numberFormat: NumberFormat): String {
        val abs = Math.abs(amount)
        if (!useThousandSep) return abs.toLong().toString()
        return when (numberFormat) {
            NumberFormat.DOT   -> dotFormatter.format(abs)
            NumberFormat.COMMA -> commaFormatter.format(abs)
            NumberFormat.SPACE -> dotFormatter.format(abs).replace(".", "\u00A0") // non-breaking space
        }
    }

    fun formatWithSign(amount: Double, useThousandSep: Boolean, numberFormat: NumberFormat): String = when {
        amount > 0 -> "+${format(amount, useThousandSep, numberFormat)}"
        amount < 0 -> "-${format(amount, useThousandSep, numberFormat)}"
        else       -> "0"
    }

    fun formatBalance(amount: Double, useThousandSep: Boolean, numberFormat: NumberFormat): String =
        "${format(amount, useThousandSep, numberFormat)} vnđ"

    // ── API mặc định (fallback DOT + useThousandSep=true) ────────────────────

    fun format(amount: Double): String = format(amount, true, NumberFormat.DOT)

    fun formatWithSign(amount: Double): String = formatWithSign(amount, true, NumberFormat.DOT)

    fun formatBalance(amount: Double): String = formatBalance(amount, true, NumberFormat.DOT)
}
