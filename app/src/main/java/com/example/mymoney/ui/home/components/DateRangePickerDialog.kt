package com.example.mymoney.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mymoney.ui.theme.MyMoneyTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val DateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Ba mode điều hướng của picker header */
private enum class CalendarMode { DAY, MONTH, YEAR }

/**
 * Dialog chọn khoảng ngày tùy chỉnh.
 *
 * Điều hướng header (pill):
 *  - DAY mode   → click pill "MM/YYYY"    → chuyển MONTH mode
 *  - MONTH mode → click pill "YYYY"       → chuyển YEAR mode
 *  - YEAR mode  → click pill "YYYY–YYYY"  → quay về DAY mode
 *
 * Logic chọn ngày:
 *  1. Tap 1 → set startDate
 *  2. Tap 2 → set endDate (tự swap nếu < start, từ chối nếu > 365 ngày)
 *  3. Cả hai đã chọn → tap lại để reset và chọn mới
 *  4. Nút "Xóa" → clear cả hai
 */
@Composable
fun DateRangePickerDialog(
    onConfirm: (fromMs: Long, toMs: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var startDate    by remember { mutableStateOf<LocalDate?>(null) }
    var endDate      by remember { mutableStateOf<LocalDate?>(null) }
    var displayMonth by remember { mutableStateOf(YearMonth.now()) }
    var errorMsg     by remember { mutableStateOf<String?>(null) }

    // ── Navigation mode state ──
    var calendarMode       by remember { mutableStateOf(CalendarMode.DAY) }
    var yearInMonthPicker  by remember { mutableIntStateOf(YearMonth.now().year) }
    var yearRangeStart     by remember { mutableIntStateOf(LocalDate.now().year - 11) }

    val today = LocalDate.now()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // ── 1. Header: Từ ngày / Đến ngày ──
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DateLabel(label = "Từ ngày",  date = startDate)
                    DateLabel(label = "Đến ngày", date = endDate, alignEnd = true)
                }

                if (errorMsg != null) {
                    Text(
                        text     = errorMsg!!,
                        color    = MaterialTheme.colorScheme.error,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // ── 2. Navigation header (mode-aware) ──
                PickerHeader(
                    calendarMode      = calendarMode,
                    displayMonth      = displayMonth,
                    yearInMonthPicker = yearInMonthPicker,
                    yearRangeStart    = yearRangeStart,
                    onPillClick       = {
                        when (calendarMode) {
                            CalendarMode.DAY -> {
                                yearInMonthPicker = displayMonth.year   // sync trước khi mở month picker
                                calendarMode = CalendarMode.MONTH
                            }
                            CalendarMode.MONTH -> calendarMode = CalendarMode.YEAR
                            CalendarMode.YEAR  -> calendarMode = CalendarMode.DAY
                        }
                    },
                    onPrev = {
                        when (calendarMode) {
                            CalendarMode.DAY   -> displayMonth = displayMonth.minusMonths(1)
                            CalendarMode.MONTH -> yearInMonthPicker--
                            CalendarMode.YEAR  -> yearRangeStart -= 12
                        }
                    },
                    onNext = {
                        when (calendarMode) {
                            CalendarMode.DAY   -> displayMonth = displayMonth.plusMonths(1)
                            CalendarMode.MONTH -> yearInMonthPicker++
                            CalendarMode.YEAR  -> yearRangeStart += 12
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                // ── 3. Calendar content (theo mode) ──
                when (calendarMode) {

                    CalendarMode.DAY -> {
                        // Tiêu đề thứ trong tuần
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "CN").forEach { h ->
                                Text(
                                    text       = h,
                                    modifier   = Modifier.weight(1f),
                                    textAlign  = TextAlign.Center,
                                    style      = MaterialTheme.typography.labelSmall,
                                    color      = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))

                        // Calendar grid
                        val firstDayOffset = displayMonth.atDay(1).dayOfWeek.value - 1
                        val daysInMonth    = displayMonth.lengthOfMonth()
                        val rowCount       = ((firstDayOffset + daysInMonth) + 6) / 7

                        repeat(rowCount) { row ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                repeat(7) { col ->
                                    val dayNumber = row * 7 + col - firstDayOffset + 1
                                    if (dayNumber in 1..daysInMonth) {
                                        val date = displayMonth.atDay(dayNumber)
                                        DayCell(
                                            day         = dayNumber,
                                            date        = date,
                                            startDate   = startDate,
                                            endDate     = endDate,
                                            today       = today,
                                            modifier    = Modifier.weight(1f),
                                            onDateClick = { clicked ->
                                                errorMsg = null
                                                when {
                                                    startDate == null -> startDate = clicked
                                                    endDate == null -> {
                                                        val from = minOf(clicked, startDate!!)
                                                        val to   = maxOf(clicked, startDate!!)
                                                        if (ChronoUnit.DAYS.between(from, to) > 365) {
                                                            errorMsg = "Giới hạn tối đa là 1 năm (365 ngày)"
                                                        } else {
                                                            startDate = from
                                                            endDate   = to
                                                        }
                                                    }
                                                    else -> { startDate = clicked; endDate = null }
                                                }
                                            }
                                        )
                                    } else {
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }

                    CalendarMode.MONTH -> {
                        MonthPickerGrid(
                            year         = yearInMonthPicker,
                            displayMonth = displayMonth,
                            today        = today,
                            onMonthSelected = { month ->
                                displayMonth = YearMonth.of(yearInMonthPicker, month)
                                calendarMode = CalendarMode.DAY
                            }
                        )
                    }

                    CalendarMode.YEAR -> {
                        YearPickerGrid(
                            yearRangeStart = yearRangeStart,
                            displayYear    = displayMonth.year,
                            today          = today,
                            onYearSelected = { year ->
                                yearInMonthPicker = year
                                calendarMode = CalendarMode.MONTH
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── 4. Quick select (luôn hiển thị) ──
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(7, 15, 30).forEach { days ->
                        OutlinedButton(
                            onClick = {
                                errorMsg  = null
                                startDate = today.minusDays(days.toLong() - 1)
                                endDate   = today
                                calendarMode = CalendarMode.DAY
                            },
                            modifier       = Modifier.weight(1f),
                            shape          = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            colors         = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                        ) {
                            Text("$days Ngày", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── 5. Xóa + Chọn ngày ──
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick  = { startDate = null; endDate = null; errorMsg = null },
                        modifier = Modifier.height(48.dp),
                        shape    = RoundedCornerShape(8.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Delete,
                            contentDescription = "Xóa",
                            modifier           = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick  = {
                            val s = startDate ?: return@Button
                            val e = endDate   ?: s
                            val zone   = ZoneId.systemDefault()
                            val fromMs = s.atStartOfDay(zone).toInstant().toEpochMilli()
                            val toMs   = e.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                            onConfirm(fromMs, toMs)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape    = RoundedCornerShape(8.dp),
                        enabled  = startDate != null,
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor   = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text       = "Chọn ngày",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ── Sub-composables ──

@Composable
private fun DateLabel(
    label:    String,
    date:     LocalDate?,
    alignEnd: Boolean = false
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            text       = date?.format(DateFmt) ?: "--/--/----",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Header điều hướng: pill (clickable để đổi mode) + mũi tên trái/phải.
 * - DAY mode   : pill hiện "MM/YYYY" + icon lịch
 * - MONTH mode : pill hiện "YYYY" + icon lịch
 * - YEAR mode  : pill hiện "YYYY–YYYY" (không icon)
 */
@Composable
private fun PickerHeader(
    calendarMode:      CalendarMode,
    displayMonth:      YearMonth,
    yearInMonthPicker: Int,
    yearRangeStart:    Int,
    onPillClick:       () -> Unit,
    onPrev:            () -> Unit,
    onNext:            () -> Unit
) {
    val primary  = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val pillText = when (calendarMode) {
        CalendarMode.DAY   -> "${displayMonth.monthValue.toString().padStart(2, '0')}/${displayMonth.year}"
        CalendarMode.MONTH -> yearInMonthPicker.toString()
        CalendarMode.YEAR  -> "$yearRangeStart–${yearRangeStart + 11}"
    }
    val showIcon = calendarMode != CalendarMode.YEAR

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Pill
        Row(
            modifier = Modifier
                .background(primary, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick           = onPillClick
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector        = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint               = onPrimary,
                    modifier           = Modifier.size(14.dp)
                )
            }
            Text(
                text       = pillText,
                color      = onPrimary,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Navigation arrows
        Row {
            IconButton(onClick = onPrev) {
                Icon(Icons.Default.ChevronLeft,  contentDescription = "Trước", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Sau",   tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** Grid 4 hàng × 3 cột chọn tháng trong năm [year] */
@Composable
private fun MonthPickerGrid(
    year:            Int,
    displayMonth:    YearMonth,
    today:           LocalDate,
    onMonthSelected: (Int) -> Unit
) {
    val primary   = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in 0 until 4) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 3) {
                    val month      = row * 3 + col + 1
                    val isSelected = displayMonth.year == year && displayMonth.monthValue == month
                    val isFuture   = year > today.year || (year == today.year && month > today.monthValue)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected)
                                    Modifier.background(primary, RoundedCornerShape(20.dp))
                                else Modifier
                            )
                            .then(
                                if (!isFuture)
                                    Modifier.clickable { onMonthSelected(month) }
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "Tháng $month",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color      = when {
                                isSelected -> onPrimary
                                isFuture   -> onSurface.copy(alpha = 0.35f)
                                else       -> onSurface
                            }
                        )
                    }
                }
            }
            if (row < 3) Spacer(Modifier.height(4.dp))
        }
    }
}

/** Grid 4 hàng × 3 cột chọn năm, mỗi trang 12 năm từ [yearRangeStart] */
@Composable
private fun YearPickerGrid(
    yearRangeStart: Int,
    displayYear:    Int,
    today:          LocalDate,
    onYearSelected: (Int) -> Unit
) {
    val primary   = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in 0 until 4) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 3) {
                    val year          = yearRangeStart + row * 3 + col
                    val isSelected    = year == displayYear
                    val isCurrentYear = year == today.year
                    val isFuture      = year > today.year

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected)
                                    Modifier.background(primary, RoundedCornerShape(20.dp))
                                else Modifier
                            )
                            .then(
                                if (!isFuture)
                                    Modifier.clickable { onYearSelected(year) }
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = year.toString(),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = when {
                                isSelected || isCurrentYear -> FontWeight.Bold
                                else -> FontWeight.Normal
                            },
                            color = when {
                                isSelected    -> onPrimary
                                isFuture      -> onSurface.copy(alpha = 0.35f)
                                isCurrentYear -> onSurface
                                else          -> onSurface.copy(alpha = 0.55f)
                            }
                        )
                    }
                }
            }
            if (row < 3) Spacer(Modifier.height(4.dp))
        }
    }
}

/** Ô ngày trong lưới lịch tháng */
@Composable
private fun DayCell(
    day:         Int,
    date:        LocalDate,
    startDate:   LocalDate?,
    endDate:     LocalDate?,
    today:       LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier:    Modifier = Modifier
) {
    val primary    = MaterialTheme.colorScheme.primary
    val onPrimary  = MaterialTheme.colorScheme.onPrimary
    val onSurface  = MaterialTheme.colorScheme.onSurface
    val rangeColor = primary.copy(alpha = 0.15f)

    val isFuture   = date > today          // ngày chưa tới → không cho chọn
    val isStart    = date == startDate
    val isEnd      = date == endDate
    val hasRange   = startDate != null && endDate != null && startDate != endDate
    val isInRange  = hasRange && startDate != null && endDate != null && startDate < date && date < endDate
    val isSelected = isStart || isEnd
    val isToday    = date == today

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .drawBehind {
                if (!isFuture) {
                    when {
                        isInRange -> drawRect(rangeColor, Offset.Zero, size)
                        isStart && hasRange -> drawRect(
                            rangeColor, Offset(size.width / 2f, 0f), Size(size.width / 2f, size.height)
                        )
                        isEnd && hasRange -> drawRect(
                            rangeColor, Offset.Zero, Size(size.width / 2f, size.height)
                        )
                    }
                }
            }
            .then(
                if (!isFuture)
                    Modifier.clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick           = { onDateClick(date) }
                    )
                else Modifier   // ngày tương lai: không có clickable
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isFuture) {
            when {
                isSelected -> Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(primary))
                isToday    -> Box(modifier = Modifier.size(34.dp).clip(CircleShape).border(1.5.dp, primary, CircleShape))
            }
        }
        Text(
            text       = day.toString(),
            textAlign  = TextAlign.Center,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected && !isFuture) FontWeight.Bold else FontWeight.Normal,
            color      = when {
                isFuture   -> onSurface.copy(alpha = 0.30f)   // mờ, không tương tác
                isSelected -> onPrimary
                isToday    -> primary
                else       -> onSurface
            }
        )
    }
}

// ── Preview ──

@Preview(showBackground = true)
@Composable
private fun DateRangePickerPreview() {
    MyMoneyTheme(darkTheme = false) {
        DateRangePickerDialog(onConfirm = { _, _ -> }, onDismiss = {})
    }
}

