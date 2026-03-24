package com.example.mymoney.ui.screens.saving

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
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Tiết kiệm — tab thứ 3 trong Bottom Navigation.
 * UI stateless: chỉ nhận state từ ViewModel, không chứa logic nghiệp vụ.
 */
@Composable
fun SavingScreen(
    modifier: Modifier = Modifier,
    viewModel: SavingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SavingContent(
        uiState = uiState,
        modifier = modifier
    )
}

/**
 * Nội dung hiển thị của màn hình Tiết kiệm.
 * Composable thuần túy — không phụ thuộc ViewModel, dễ test và preview.
 */
@Composable
private fun SavingContent(
    uiState: SavingUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Màn hình Tiết Kiệm",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SavingScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        SavingContent(uiState = SavingUiState())
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SavingScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        SavingContent(uiState = SavingUiState())
    }
}
