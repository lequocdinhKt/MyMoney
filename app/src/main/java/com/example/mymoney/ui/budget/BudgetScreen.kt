package com.example.mymoney.ui.budget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.domain.model.BudgetModel
import com.example.mymoney.presentation.viewmodel.budget.BudgetViewModel
import com.example.mymoney.presentation.viewmodel.budget.BudgetViewModelFactory
import com.example.mymoney.presentation.viewmodel.budget.budget.BudgetEvent
import com.example.mymoney.presentation.viewmodel.budget.budget.BudgetUiState
import com.example.mymoney.ui.budget.components.BudgetSection
import com.example.mymoney.ui.components.EmptyStateComposable
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Ngân sách – tab thứ 2 trong Bottom Navigation.
 */
@Composable
fun BudgetScreen(
    userId: String = "",
    onNavigateToBudgetManual: (budgetId: Long) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModelFactory(context, userId)
    )
    val uiState by viewModel.uiState.collectAsState()

    BudgetContent(
        uiState = uiState,
        onNavigateToBudgetManual = onNavigateToBudgetManual,
        onDeleteBudget = { id -> viewModel.onEvent(BudgetEvent.DeleteBudget(id)) }
    )
}

/**
 * Nội dung hiển thị của màn hình Ngân sách.
 */
@Composable
private fun BudgetContent(
    uiState: BudgetUiState,
    onNavigateToBudgetManual: (budgetId: Long) -> Unit = {},
    onDeleteBudget: (budgetId: Long) -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.budgets.isEmpty() -> {
                EmptyStateComposable(
                    "Hiện tại chưa có ngân sách nào được tạo.\n" +
                            "Bắt đầu thêm ngân sách của bạn ngay bây giờ"
                )
            }
            else -> {
                BudgetSection(
                    budgets = uiState.budgets,
                    selectedBudgetId = -1L,
                    onSelectBudget = {},
                    onDeleteClick = onDeleteBudget,
                    onEditBudget = { id -> onNavigateToBudgetManual(id) },
                    onReorderBudget = {}
                )
            }
        }

        // FAB
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // FAB chính
            ExtendedFloatingActionButton(
                onClick = { onNavigateToBudgetManual(-1L) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    text = "Thêm",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

// ── Previews ──
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BudgetScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        BudgetContent(
            uiState = BudgetUiState(),
            onNavigateToBudgetManual = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BudgetScreenWithDataPreview() {
    MyMoneyTheme(darkTheme = false) {
        BudgetContent(
            uiState = BudgetUiState(
                isLoading = false,
                budgets = listOf(
                    BudgetModel(id = 1L, userId = "u1", categoryId = 1L, categoryName = "Ăn uống", amountLimit = 2000000.0, month = 5, year = 2026),
                    BudgetModel(id = 2L, userId = "u1", categoryId = 2L, categoryName = "Di chuyển", amountLimit = 500000.0, month = 5, year = 2026),
                )
            ),
            onNavigateToBudgetManual = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BudgetScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        BudgetContent(
            uiState = BudgetUiState(),
            onNavigateToBudgetManual = {}
        )
    }
}