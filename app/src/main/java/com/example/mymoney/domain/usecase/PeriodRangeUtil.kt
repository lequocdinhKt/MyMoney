package com.example.mymoney.domain.usecase

import com.example.mymoney.presentation.viewmodel.home.home.TimePeriod
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Tính khoảng thời gian [from, to) epoch millis từ [TimePeriod].
 * Dùng ZoneId.systemDefault() để tính theo múi giờ thiết bị.
 */
object PeriodRangeUtil {

    data class Range(val from: Long, val to: Long)

    fun getRangeFor(period: TimePeriod, referenceDate: LocalDate = LocalDate.now()): Range {
        val zone = ZoneId.systemDefault()
        return when (period) {
            TimePeriod.DAY -> {
                val from = referenceDate.atStartOfDay(zone).toInstant().toEpochMilli()
                val to   = referenceDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                Range(from, to)
            }
            TimePeriod.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val startOfWeek = referenceDate.with(weekFields.dayOfWeek(), 1)
                val from = startOfWeek.atStartOfDay(zone).toInstant().toEpochMilli()
                val to   = startOfWeek.plusWeeks(1).atStartOfDay(zone).toInstant().toEpochMilli()
                Range(from, to)
            }
            TimePeriod.MONTH -> {
                val startOfMonth = referenceDate.withDayOfMonth(1)
                val from = startOfMonth.atStartOfDay(zone).toInstant().toEpochMilli()
                val to   = startOfMonth.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
                Range(from, to)
            }
            TimePeriod.YEAR -> {
                val startOfYear = referenceDate.withDayOfYear(1)
                val from = startOfYear.atStartOfDay(zone).toInstant().toEpochMilli()
                val to   = startOfYear.plusYears(1).atStartOfDay(zone).toInstant().toEpochMilli()
                Range(from, to)
            }
            TimePeriod.CUSTOM -> {
                // CUSTOM dùng toàn bộ — sẽ mở rộng sau với date picker
                val from = 0L
                val to   = Long.MAX_VALUE
                Range(from, to)
            }
        }
    }

    /** Tạo nhãn hiển thị phía trên danh sách giao dịch */
    fun getLabelFor(period: TimePeriod, referenceDate: LocalDate = LocalDate.now()): String {
        val months = listOf(
            "tháng 1","tháng 2","tháng 3","tháng 4","tháng 5","tháng 6",
            "tháng 7","tháng 8","tháng 9","tháng 10","tháng 11","tháng 12"
        )
        return when (period) {
            TimePeriod.DAY   -> "Hôm nay, ${referenceDate.dayOfMonth} ${months[referenceDate.monthValue - 1]}"
            TimePeriod.WEEK  -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val start = referenceDate.with(weekFields.dayOfWeek(), 1)
                val end   = start.plusDays(6)
                "${start.dayOfMonth}/${start.monthValue} – ${end.dayOfMonth}/${end.monthValue}/${end.year}"
            }
            TimePeriod.MONTH -> "${months[referenceDate.monthValue - 1].replaceFirstChar { it.uppercase() }}, ${referenceDate.year}"
            TimePeriod.YEAR  -> "Năm ${referenceDate.year}"
            TimePeriod.CUSTOM -> "Tất cả giao dịch"
        }
    }

    /**
     * Nhãn hiển thị cho khoảng ngày tùy chỉnh.
     * Ví dụ: "01/04/2026 – 30/04/2026"
     */
    fun getCustomLabel(fromMs: Long, toMs: Long): String {
        val zone    = ZoneId.systemDefault()
        val from    = Instant.ofEpochMilli(fromMs).atZone(zone).toLocalDate()
        // toMs là exclusive (start of next day), nên trừ 1 ngày để hiển thị đúng
        val to      = Instant.ofEpochMilli(toMs).atZone(zone).toLocalDate().minusDays(1)
        fun LocalDate.fmt() = "${dayOfMonth.toString().padStart(2,'0')}/${monthValue.toString().padStart(2,'0')}/$year"
        return "${from.fmt()} – ${to.fmt()}"
    }
}
