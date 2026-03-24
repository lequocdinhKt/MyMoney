package com.example.mymoney.ui.other

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
import com.example.mymoney.presentation.viewmodel.other.OtherViewModel
import com.example.mymoney.presentation.viewmodel.other.other.OtherUiState
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Khác – tab cuối cùng trong Bottom Navigation.
 * UI stateless: chỉ nhận state từ ViewModel, không chứa logic nghiệp vụ.
 */
@Composable
fun OtherScreen(
    modifier: Modifier = Modifier,
    viewModel: OtherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    OtherContent(
        uiState = uiState,
        modifier = modifier
    )
}

/**
 * Nội dung hiển thị của màn hình Khác.
 * Composable thuần tuý – không phụ thuộc ViewModel, dễ test và preview.
 */
@Composable
private fun OtherContent(
    uiState: OtherUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Màn hình Khác",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OtherScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        OtherContent(uiState = OtherUiState())
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OtherScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        OtherContent(uiState = OtherUiState())
    }
}
