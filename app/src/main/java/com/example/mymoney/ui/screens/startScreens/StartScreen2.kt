package com.example.mymoney.ui.screens.startScreens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.example.mymoney.R
import com.example.mymoney.ui.screens.startScreens.component.OnboardingPageLayout
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình onboarding thứ hai.
 * Nội dung: "Kiểm Soát Tiền Của Bạn Ngay Hôm Nay" — giới thiệu giải pháp ứng dụng mang lại.
 *
 * @param onNextClick Callback khi nhấn nút tiếp theo để chuyển sang StartScreen3
 * @param modifier Modifier tuỳ chỉnh từ bên ngoài
 */
@Composable
fun StartScreen2(
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = buildAnnotatedString {
        append("Kiểm Soát Tiền Của Bạn\nNgay Hôm Nay")
    }

    OnboardingPageLayout(
        title = title,
        description = "Theo dõi chi phí trong vài giây, xem tiền của bạn " +
                "được chỉ vào đâu và kiểm soát ngân sách mà không bị căng thẳng.",
        imageRes = R.drawable.img_batdau2,
        imageDescription = "Hình minh hoạ người vui vẻ với tiền bay xung quanh",
        currentPage = 1,
        totalPages = 3,
        onNextClick = onNextClick,
        modifier = modifier,
    )
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StartScreen2LightPreview() {
    MyMoneyTheme(darkTheme = false) {
        StartScreen2(onNextClick = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StartScreen2DarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        StartScreen2(onNextClick = {})
    }
}
