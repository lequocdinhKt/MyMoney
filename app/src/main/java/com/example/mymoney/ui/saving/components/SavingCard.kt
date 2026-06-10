package com.example.mymoney.ui.saving.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymoney.domain.model.SavingGoalModel
import com.example.mymoney.domain.model.SavingType
import com.example.mymoney.domain.usecase.MoneyFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.style.TextAlign
import com.example.mymoney.presentation.viewmodel.saving.saving.SavingGoalItem

@Composable
fun SavingSection(
    savings: List<SavingGoalItem>,
    onDeleteSaving: (Long) -> Unit,
    onDetailSaving: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    )  {
        itemsIndexed(savings, key = { _, item -> item.goal.id }) { _, item ->
            SavingCard(
                goal = item.goal,
                currentAmount = item.currentAmount,
                onDouble = { onDetailSaving(item.goal.id) },
                onDelete = { onDeleteSaving(item.goal.id) }
            )
        }
    }
}

@Composable
fun SavingCard(
    goal: SavingGoalModel,
    currentAmount: Double,
    onDelete: () -> Unit,
    onDouble: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetAmount = goal.targetAmount
    val progress = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val isCompleted = currentAmount >= targetAmount

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .pointerInput(onDouble) {
                detectTapGestures (
                    onDoubleTap = { onDouble() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Đã tiết kiệm ${MoneyFormatter.format(currentAmount)} / ${MoneyFormatter.format(targetAmount)} ${goal.currency}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (goal.savingType == SavingType.ONE_TIME) {
                    OneTimeDetails(goal.targetDate)
                } else {
                    RecurringDetails(goal.savingType)
                }
            }

            if (isCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Chúc mừng! Bạn đã đạt được mục tiêu tiết kiệm của mình!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OneTimeDetails(targetDateMillis: Long?) {
    if (targetDateMillis == null) return
    val targetDate = Instant.ofEpochMilli(targetDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    val daysRemaining = ChronoUnit.DAYS.between(today, targetDate)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                modifier = Modifier.weight(1f),
                label = "Ngày mục tiêu",
                value = targetDate.format(formatter)
            )

            VerticalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 8.dp)
            )

            StatItem(
                modifier = Modifier.weight(1f),
                label = "Số ngày đến hạn",
                value = when {
                    daysRemaining > 0 -> "$daysRemaining ngày"
                    daysRemaining == 0L -> "Hôm nay"
                    else -> "Quá hạn"
                }
            )
        }
    }
}

@Composable
private fun RecurringDetails(type: SavingType) {
    val today = LocalDate.now()
    val periodLabel = if (type == SavingType.WEEKLY) "Mỗi tuần" else "Mỗi tháng"

    val timeInfo = if (type == SavingType.MONTHLY) {
        "Tháng ${today.monthValue}"
    } else {
        val start = today.withDayOfMonth(1)
        val end = start.plusDays(6)
        val fmt = DateTimeFormatter.ofPattern("dd/MM")
        "${start.format(fmt)} - ${end.format(fmt)}"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            StatItem(
                modifier = Modifier.weight(1f),
                label = "Chu kỳ",
                value = periodLabel
            )

            VerticalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 8.dp)
            )

            StatItem(
                modifier = Modifier.weight(1f),
                label = "Hiện tại",
                value = timeInfo
            )
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
private fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Box(
        modifier = modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(color)
    )
}

@Preview(
    name = "SavingSection Preview",
    showBackground = true,
    widthDp = 400,
    showSystemUi = true
)
@Composable
fun SavingSectionPreview() {

    val fakeData = listOf(
        SavingGoalItem(
            goal = SavingGoalModel(
                id = 1L,
                userId = "user_1",
                title = "Mua xe máy",
                targetAmount = 50_000_000.0,
                savingType = SavingType.ONE_TIME,
                targetDate = System.currentTimeMillis() + 5L * 24 * 60 * 60 * 1000
            ),
            currentAmount = 20_000_000.0
        ),
        SavingGoalItem(
            goal = SavingGoalModel(
                id = 2L,
                userId = "user_1",
                title = "Quỹ du lịch",
                targetAmount = 10_000_000.0,
                savingType = SavingType.WEEKLY
            ),
            currentAmount = 3_500_000.0
        ),
        SavingGoalItem(
            goal = SavingGoalModel(
                id = 3L,
                userId = "user_1",
                title = "Quỹ đầu tư",
                targetAmount = 20_000_000.0,
                savingType = SavingType.MONTHLY
            ),
            currentAmount = 8_000_000.0
        ),
        SavingGoalItem(
            goal = SavingGoalModel(
                id = 4L,
                userId = "user_1",
                title = "Laptop mới",
                targetAmount = 25_000_000.0,
                savingType = SavingType.ONE_TIME,
                targetDate = System.currentTimeMillis()
            ),
            currentAmount = 25_000_000.0
        )
    )

    MaterialTheme {
        SavingSection(
            savings = fakeData,
            onDeleteSaving = {},
            onDetailSaving = {}
        )
    }
}
