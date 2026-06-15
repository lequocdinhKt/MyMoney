package com.example.mymoney.ui.saving

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.domain.model.SavingGoalDetailModel
import com.example.mymoney.domain.model.SavingType
import com.example.mymoney.presentation.viewmodel.saving.saving_detail.SavingDetailUiState
import com.example.mymoney.presentation.viewmodel.saving.saving_detail.SavingDetailViewModel
import com.example.mymoney.presentation.viewmodel.saving.saving_detail.SavingDetailViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import com.example.mymoney.domain.model.SavingGoalModel
import com.example.mymoney.ui.components.EmptyStateComposable
import com.example.mymoney.ui.theme.MyMoneyTheme
import com.example.mymoney.ui.theme.SuccessGreen

@Composable
fun SavingDetailScreen(
    goalId: Long,
    onNavigateBack: () -> Unit = {},
    onNavigateToAddRecord: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: SavingDetailViewModel = viewModel(
        factory = SavingDetailViewModelFactory(context, goalId)
    )

    val uiState by viewModel.uiState.collectAsState()

    SavingDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToAddRecord = onNavigateToAddRecord
    )
}

@Composable
private fun SavingDetailContent(
    uiState: SavingDetailUiState,
    onNavigateBack: () -> Unit,
    onNavigateToAddRecord: (Long) -> Unit
) {
    val detail = uiState.detail
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── HEADER ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "Chi tiết mục tiêu tiết kiệm",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            // ── LOADING / ERROR ──
            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                uiState.error != null -> {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(uiState.error)
                        }
                    }
                }

                detail != null -> {
                    // ── PROGRESS ──
                    item {
                        SavingProgressSection(detail)
                    }

                    item { Spacer(Modifier.height(12.dp)) }

                    // ── INFO CARD ──
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                if (detail.goal.savingType == SavingType.ONE_TIME) {
                                    InfoRow(
                                        "Ngày bắt đầu",
                                        formatDate(detail.goal.createdAt)
                                    )
                                    InfoRow(
                                        "Ngày mục tiêu",
                                        formatDate(detail.goal.targetDate)
                                    )
                                    InfoRow(
                                        "Số ngày còn lại",
                                        "${detail.daysRemaining ?: 0} ngày"
                                    )
                                } else {
                                    InfoRow(
                                        "Tổng đã tiết kiệm",
                                        formatMoney(detail.totalSavedAllTime)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(12.dp)) }

                    // ── TITLE ──
                    item {
                        Text(
                            "Lịch sử tiết kiệm",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item { Spacer(Modifier.height(28.dp)) }

                    // ── RECORD LIST ──
                    if (detail.records.isEmpty()) {
                        item {
                            EmptyStateComposable("Chưa có hồ sơ nào. Thêm mới ngay nào")
                        }
                    } else {
                        // TODO: Hiển thị lịch sử tiết kiệm
                    }
                }
            }
        }
        if (detail != null) {
            // FAB
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddRecord(detail.goal.id) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(text = "Thêm")
            }
        }
    }
}

@Composable
private fun SavingProgressSection(
    detail: SavingGoalDetailModel
) {
    val isCompleted = detail.progress >= 1f
    val progressColor =
        if (isCompleted) SuccessGreen
        else MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card {
            Text(
                text = when (detail.goal.savingType) {
                    SavingType.ONE_TIME -> "Một lần"
                    SavingType.WEEKLY -> "Định kỳ - Hàng tuần"
                    SavingType.MONTHLY -> "Định kỳ - Hàng tháng"
                },
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            )
        }

        Spacer(Modifier.height(6.dp))

        if (detail.goal.savingType != SavingType.ONE_TIME) {
            Text(
                text = "${formatDate(detail.currentCycleStart)} - ${
                    formatDate(detail.currentCycleEnd)
                }",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { detail.progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 10.dp,
                color = progressColor
            )

            Text(
                text = "${(detail.progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                color = progressColor
            )
        }

        if (isCompleted) {
            Spacer(Modifier.height(18.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SuccessGreen.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "Hoàn thành",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = SuccessGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticItem(
                title = "Đã tiết kiệm",
                value = if (detail.goal.savingType == SavingType.ONE_TIME)
                    detail.totalSavedAllTime
                else
                    detail.currentCycleSaved,
                valueColor = SuccessGreen
            )

            StatisticItem(
                title = "Còn lại",
                value = detail.remainingAmount,
                valueColor = MaterialTheme.colorScheme.primary
            )

            StatisticItem(
                title = "Mục tiêu",
                value = detail.goal.targetAmount,
                valueColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatisticItem(
    title: String,
    value: Double,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = formatMoney(value),
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)

        Text(
            text = value,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatMoney(
    amount: Double
): String {
    return "%,.0f VNĐ".format(amount)
}

private fun formatDate(
    millis: Long?
): String {
    if (millis == null) return "-"
    val simpleDateFormat = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    )

    return simpleDateFormat.format(Date(millis))
}

@Preview(showBackground = true)
@Composable
fun SavingDetailPreview() {
    val now = System.currentTimeMillis()
    val detail = SavingGoalDetailModel(
        goal = SavingGoalModel(
            id = 1,
            userId = "1",
            title = "Mua xe",
            targetAmount = 10000000.0,
            createdAt = System.currentTimeMillis(),
            targetDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            savingType = SavingType.MONTHLY
        ),
        records = emptyList(),
        totalSavedAllTime = 3000000.0,
        currentCycleSaved = 500000.0,
        progress = 0.5f,
        remainingAmount = 500000.0,
        currentCycleStart = now,
        currentCycleEnd = now + 30L * 24 * 60 * 60 * 1000
    )

    SavingDetailContent(
        uiState = SavingDetailUiState(detail = detail),
        onNavigateBack = {},
        onNavigateToAddRecord = {}
    )
}

@Preview(showBackground = true, name = "Saving Detail - Completed")
@Composable
fun SavingDetailCompletedPreview() {
    val now = System.currentTimeMillis()

    val detail = SavingGoalDetailModel(
        goal = SavingGoalModel(
            id = 1,
            userId = "1",
            title = "Mua xe",
            targetAmount = 10000000.0,
            createdAt = System.currentTimeMillis(),
            targetDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            savingType = SavingType.MONTHLY
        ),
        records = emptyList(),
        totalSavedAllTime = 10000000.0,
        currentCycleSaved = 10000000.0,
        progress = 1f, // 👈 QUAN TRỌNG: completed
        remainingAmount = 0.0,
        currentCycleStart = now,
        currentCycleEnd = now + 30L * 24 * 60 * 60 * 1000
    )

    MyMoneyTheme {
        SavingDetailContent(
            uiState = SavingDetailUiState(detail = detail),
            onNavigateBack = {},
            onNavigateToAddRecord = {}
        )
    }
}