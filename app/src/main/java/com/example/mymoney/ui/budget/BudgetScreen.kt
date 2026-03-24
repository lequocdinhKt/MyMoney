package com.example.mymoney.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.budget.BudgetViewModel
import com.example.mymoney.presentation.viewmodel.budget.budget.BudgetUiState
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Ngân sách – tab thứ 2 trong Bottom Navigation.
 * UI stateless: chỉ nhận state từ ViewModel, không chứa logic nghiệp vụ.
 */
@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BudgetContent(
        uiState = uiState,
        modifier = modifier
    )
}

/**
 * Nội dung hiển thị của màn hình Ngân sách.
 * Composable thuần tuý – không phụ thuộc ViewModel, dễ test và preview.
 */
@Composable
private fun BudgetContent(
    uiState: BudgetUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Màn hình Ngân Sách",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BudgetScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        BudgetContent(uiState = BudgetUiState())
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BudgetScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        BudgetContent(uiState = BudgetUiState())
    }
}
