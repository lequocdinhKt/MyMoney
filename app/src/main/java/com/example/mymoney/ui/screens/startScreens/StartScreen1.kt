package com.example.mymoney.ui.screens.startScreens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.example.mymoney.R
import com.example.mymoney.ui.screens.startScreens.component.OnboardingPageLayout
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình onboarding đầu tiên.
 * Nội dung: "Tiền biến mất không rõ lý do?" — giới thiệu vấn đề người dùng gặp phải.
 *
 * @param onNextClick Callback khi nhấn nút tiếp theo để chuyển sang StartScreen2
 * @param modifier Modifier tuỳ chỉnh từ bên ngoài
 */
@Composable
fun StartScreen1(
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tiêu đề in đậm
    val title: AnnotatedString = buildAnnotatedString {
        append("Tiền biến mất không rõ lý do?")
    }

    OnboardingPageLayout(
        title = title,
        description = "Bạn quên theo dõi chi phí, hóa đơn ngày càng nhiều " +
                "và bạn không bao giờ biết chắc tiền của mình thực sự đi về đâu.",
        imageRes = R.drawable.img_batdau1,
        imageDescription = "Hình minh hoạ người suy nghĩ với các dấu chấm hỏi",
        currentPage = 0,
        totalPages = 3,
        onNextClick = onNextClick,
        modifier = modifier
    )
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StartScreen1LightPreview() {
    MyMoneyTheme(darkTheme = false) {
        StartScreen1(onNextClick = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StartScreen1DarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        StartScreen1(onNextClick = {})
    }
}
