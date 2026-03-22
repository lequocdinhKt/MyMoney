package com.example.mymoney.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình chính của ứng dụng — hiển thị sau khi hoàn thành onboarding.
 * Hiện tại là placeholder, sẽ được thay thế bằng nội dung thực tế sau.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Màn hình chính",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        MainScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        MainScreen()
    }
}
