package com.example.mymoney.ui.screens.startScreens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.example.mymoney.R
import com.example.mymoney.ui.screens.startScreens.component.OnboardingPageLayout
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình onboarding thứ ba (cuối cùng).
 * Nội dung: "Chào mừng bạn đến với myMoney" — chào đón người dùng sử dụng ứng dụng.
 *
 * @param onNextClick Callback khi nhấn nút tiếp theo để vào màn hình chính
 * @param modifier Modifier tuỳ chỉnh từ bên ngoài
 */
@Composable
fun StartScreen3(
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lấy màu primary từ theme để tô "myMoney" màu xanh
    val primaryColor = MaterialTheme.colorScheme.primary

    val title = buildAnnotatedString {
        append("Chào mừng bạn đến với\n")
        // "myMoney" được tô màu primary (xanh dương)
        withStyle(style = SpanStyle(color = primaryColor)) {
            append("myMoney")
        }
    }

    OnboardingPageLayout(
        title = title,
        description = "Đồng hành cùng bạn trên hành trình tự do tài chính",
        imageRes = R.drawable.ic_logomymoney,
        imageDescription = "Hình minh hoạ heo đất tiết kiệm với đồng xu",
        currentPage = 2,
        totalPages = 3,
        onNextClick = onNextClick,
        modifier = modifier
    )
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StartScreen3LightPreview() {
    MyMoneyTheme(darkTheme = false) {
        StartScreen3(onNextClick = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StartScreen3DarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        StartScreen3(onNextClick = {})
    }
}
