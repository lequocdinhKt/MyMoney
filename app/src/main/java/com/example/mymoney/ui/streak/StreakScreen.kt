package com.example.mymoney.ui.streak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.presentation.viewmodel.streak.DaySummary
import com.example.mymoney.presentation.viewmodel.streak.StreakUiState
import com.example.mymoney.presentation.viewmodel.streak.StreakViewModel
import com.example.mymoney.presentation.viewmodel.streak.StreakViewModelFactory
import com.example.mymoney.ui.theme.MyMoneyTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── Màu sắc cho streak ────────────���──────────────────────────────────────────
private val StreakOrange  = Color(0xFFFF9500)
private val StreakOrangeBg = Color(0xFFFFF3E0)
private val IncomeGreen  = Color(0xFF4CAF50)
private val ExpenseRed   = Color(0xFFF44336)

@Composable
fun StreakScreen(
    factory: StreakViewModelFactory,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: StreakViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    StreakContent(
        state = state,
        onBackClick = onBackClick,
        onDaySelected = vm::onDaySelected,
        onDayDismissed = vm::onDayDismissed,
        onPreviousMonth = vm::onPreviousMonth,
        onNextMonth = vm::onNextMonth,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StreakContent(
    state: StreakUiState,
    onBackClick: () -> Unit,
    onDaySelected: (LocalDate) -> Unit,
    onDayDismissed: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chuỗi ngày",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = StreakOrange) }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ─── Stats row ───────────────────────────────────────────────
                StreakStatsRow(
                    currentStreak = state.currentStreak,
                    longestStreak = state.longestStreak,
                    todayCount    = state.todayTransactionCount
                )

                // ─── Calendar ────────────────────────────────────────────────
                StreakCalendar(
                    displayMonth  = state.displayMonth,
                    daySummaryMap = state.daySummaryMap,
                    selectedDate  = state.selectedDate,
                    onPrevious    = onPreviousMonth,
                    onNext        = onNextMonth,
                    onDayClick    = onDaySelected
                )
            }
        }

        // ─── Bottom sheet chi tiết ngày ─────────────────────────────────────
        if (state.selectedDate != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = onDayDismissed,
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                DayDetailSheet(
                    date    = state.selectedDate,
                    summary = state.selectedDaySummary
                )
            }
        }
    }
}

// ─── Stats cards ──────────────────────────────────────────────────────────────

@Composable
private fun StreakStatsRow(
    currentStreak: Int,
    longestStreak: Int,
    todayCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.Asset("fire.lottie")
                )
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever
                )
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(36.dp)
                )
            },
            value = "$currentStreak ngày",
            label = "Chuỗi hiện tại"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = {
                Text("", fontSize = 24.sp)
            },
            value = "$longestStreak ngày",
            label = "Chuỗi dài nhất"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = {
                Text("", fontSize = 22.sp)
            },
            value = "$todayCount GD",
            label = "Hôm nay"
        )
    }
}

@Composable
private fun StatCard(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Calendar ─────────────────────────────────────────────────────────────────

@Composable
private fun StreakCalendar(
    displayMonth: YearMonth,
    daySummaryMap: Map<LocalDate, DaySummary>,
    selectedDate: LocalDate?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Tháng + điều hướng ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
                }
                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("vi"))
                Text(
                    text = displayMonth.atDay(1).format(formatter).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau")
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Header thứ ──
            val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
            Row(Modifier.fillMaxWidth()) {
                dayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Lưới ngày ──
            val today = LocalDate.now()
            val firstDay = displayMonth.atDay(1)
            // Monday=0 … Sunday=6  (ISO Monday is 1, so offset = dayOfWeek.value - 1)
            val startOffset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
            val daysInMonth = displayMonth.lengthOfMonth()
            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 7) {
                        val index = row * 7 + col
                        val dayNumber = index - startOffset + 1
                        if (dayNumber < 1 || dayNumber > daysInMonth) {
                            Box(Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val date = displayMonth.atDay(dayNumber)
                            val summary = daySummaryMap[date]
                            val isToday = date == today
                            val isSelected = date == selectedDate
                            val hasData = summary != null
                            val isFuture = date.isAfter(today)

                            DayCell(
                                modifier = Modifier.weight(1f),
                                day = dayNumber,
                                hasTransactions = hasData,
                                isToday = isToday,
                                isSelected = isSelected,
                                isFuture = isFuture,
                                net = summary?.net,
                                onClick = { if (!isFuture) onDayClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    hasTransactions: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    net: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected   -> StreakOrange
        isToday      -> StreakOrange.copy(alpha = 0.15f)
        hasTransactions -> StreakOrangeBg
        else         -> Color.Transparent
    }
    val textColor = when {
        isSelected   -> Color.White
        isFuture     -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        hasTransactions -> StreakOrange
        else         -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }
    val borderMod = if (isToday && !isSelected)
        Modifier.border(2.dp, StreakOrange, CircleShape)
    else Modifier

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .then(borderMod)
            .background(bgColor, CircleShape)
            .clickable(enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday || hasTransactions) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                fontSize = 13.sp
            )
            // Dot indicator nếu có giao dịch và không selected
            if (hasTransactions && !isSelected && net != null) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (net >= 0) IncomeGreen else ExpenseRed)
                )
            }
        }
    }
}

// ─── Day Detail Bottom Sheet ───────────────────────────────────────────────────

@Composable
private fun DayDetailSheet(
    date: LocalDate,
    summary: DaySummary?
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.forLanguageTag("vi"))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // ── Tiêu đề ngày ──
        Text(
            text = date.format(formatter).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (summary == null || summary.transactions.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Không có giao dịch nào",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // ── Tổng hợp thu / chi ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryChip(
                    modifier = Modifier.weight(1f),
                    label = "Thu nhập",
                    amount = summary.income,
                    color = IncomeGreen
                )
                SummaryChip(
                    modifier = Modifier.weight(1f),
                    label = "Chi tiêu",
                    amount = summary.expense,
                    color = ExpenseRed
                )
            }

            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ── Danh sách giao dịch ──
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(summary.transactions, key = { it.id }) { tx ->
                    TransactionRow(tx)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryChip(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        Spacer(Modifier.height(4.dp))
        Text(
            text = formatAmount(amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun TransactionRow(tx: TransactionModel) {
    val isIncome = tx.type == "income"
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val sign = if (isIncome) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
            contentDescription = null,
            tint = amountColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.note.ifBlank { tx.category },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            if (tx.category.isNotBlank() && tx.note.isNotBlank()) {
                Text(
                    text = tx.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = "$sign${formatAmount(tx.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 1_000_000) {
        val millions = amount / 1_000_000
        if (millions == millions.toLong().toDouble()) "${millions.toLong()}tr" else "%.1ftr".format(millions)
    } else if (amount >= 1_000) {
        val thousands = amount / 1_000
        if (thousands == thousands.toLong().toDouble()) "${thousands.toLong()}k" else "%.0fk".format(thousands)
    } else {
        "%.0f₫".format(amount)
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StreakScreenPreview() {
    val today = LocalDate.now()
    val fakeSummaries = buildMap {
        for (i in 0..13) {
            val date = today.minusDays(i.toLong())
            put(
                date, DaySummary(
                    date = date,
                    transactions = emptyList(),
                    income = if (i % 3 == 0) 200_000.0 else 0.0,
                    expense = 150_000.0
                )
            )
        }
    }
    MyMoneyTheme(darkTheme = false) {
        StreakContent(
            state = StreakUiState(
                isLoading = false,
                currentStreak = 14,
                longestStreak = 21,
                todayTransactionCount = 3,
                displayMonth = YearMonth.now(),
                daySummaryMap = fakeSummaries
            ),
            onBackClick = {},
            onDaySelected = {},
            onDayDismissed = {},
            onPreviousMonth = {},
            onNextMonth = {}
        )
    }
}









