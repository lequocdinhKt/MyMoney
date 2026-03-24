package com.example.mymoney.ui.screens.home

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
 * Màn hình Trang chủ — tab đầu tiên trong Bottom Navigation.
 * UI stateless: chỉ nhận state từ ViewModel, không chứa logic nghiệp vụ.
 *
 * @param viewModel ViewModel cung cấp trạng thái cho màn hình
 * @param modifier Modifier tùy chỉnh từ bên ngoài
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    // Lắng nghe trạng thái từ ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Gọi phần UI thuần — tách biệt để dễ Preview
    HomeContent(
        uiState = uiState,
        modifier = modifier
    )
}

/**
 * Nội dung hiển thị của màn hình Trang chủ.
 * Composable thuần túy — không phụ thuộc ViewModel, dễ test và preview.
 */
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder — sẽ thay thế bằng nội dung thực tế
        Text(
            text = "Màn hình Trang Chủ",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        HomeContent(uiState = HomeUiState())
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        HomeContent(uiState = HomeUiState())
    }
}
