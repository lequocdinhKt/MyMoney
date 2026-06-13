package com.example.mymoney.ui.saving

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.mymoney.presentation.viewmodel.saving.saving_detail.SavingDetailEvent
import com.example.mymoney.presentation.viewmodel.saving.saving_detail.SavingDetailUiState
import com.example.mymoney.presentation.viewmodel.saving.saving_detail.SavingDetailViewModel
import com.example.mymoney.presentation.viewmodel.saving.saving_detail.SavingDetailViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.example.mymoney.domain.model.SavingGoalModel
import com.example.mymoney.ui.components.EmptyStateComposable

@Composable
fun SavingDetailScreen(
    goalId: Long,
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: SavingDetailViewModel = viewModel(
        factory = SavingDetailViewModelFactory(context, goalId)
    )

    val uiState by viewModel.uiState.collectAsState()

    SavingDetailContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun SavingDetailContent(
    uiState: SavingDetailUiState,
    onEvent: (SavingDetailEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val detail = uiState.detail
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                }

                Text(
                    text = "Chi tiết mục tiêu tiết kiệm",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.error)
                    }
                }

                detail != null -> {
                    SavingProgressSection(
                        detail = detail
                    )

                    Spacer(Modifier.height(12.dp))

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

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Lịch sử tiết kiệm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                    )

                    EmptyStateComposable("Chưa có hồ sơ nào. Thêm mới ngay nào")
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                // Navigate Add Record
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Text(text = "Thêm")
        }
    }
}

@Composable
private fun SavingProgressSection(
    detail: SavingGoalDetailModel
) {
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
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { detail.progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 10.dp
            )

            Text(
                text = "${(detail.progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge
            )
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
                    detail.currentCycleSaved
            )

            StatisticItem(
                title = "Còn lại",
                value = detail.remainingAmount
            )

            StatisticItem(
                title = "Mục tiêu",
                value = detail.goal.targetAmount
            )
        }
    }
}

@Composable
private fun StatisticItem(
    title: String,
    value: Double
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = formatMoney(value),
            fontWeight = FontWeight.Bold
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
        onEvent = {},
        onNavigateBack = {}
    )
}