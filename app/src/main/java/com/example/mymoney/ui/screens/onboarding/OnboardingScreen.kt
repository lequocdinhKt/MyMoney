package com.example.mymoney.ui.screens.onboarding

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.data.local.static.onboardingPages
import com.example.mymoney.ui.screens.onboarding.component.OnboardingPageLayout
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình onboarding duy nhất.
 *
 * State ([OnboardingUiState.currentPage]) đến từ [OnboardingViewModel].
 * Navigation side-effect đến từ [OnboardingViewModel.navEvent] (SharedFlow).
 * UI hoàn toàn stateless — không chứa logic nghiệp vụ, không phụ thuộc NavController.
 *
 * @param onFinished Callback gọi khi người dùng hoàn thành tất cả trang onboarding
 * @param modifier   Modifier tuỳ chỉnh từ bên ngoài
 * @param viewModel  ViewModel quản lý state onboarding (tự tạo factory nếu không truyền)
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel(
        // Truyền factory để inject SettingPreferences mà không cần Hilt/Koin
        factory = OnboardingViewModel.factory(LocalContext.current)
    ),
) {
    // Quan sát UI state từ ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect navigation side-effect — LaunchedEffect(Unit) chạy 1 lần, lắng nghe liên tục
    // Khi ViewModel phát NavigateToMain → gọi callback onFinished, không tự navigate
    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is OnboardingNavEvent.NavigateToMain -> onFinished()
            }
        }
    }

    // Lấy dữ liệu trang hiện tại từ danh sách — không hardcode số trang
    val page = onboardingPages[uiState.currentPage]

    OnboardingPageLayout(
        title = buildPageTitle(titleKey = page.titleKey),
        description = page.description,
        imageRes = page.imageRes,
        imageDescription = page.imageDescription,
        currentPage = uiState.currentPage,
        totalPages = onboardingPages.size,
        onNextClick = { viewModel.onEvent(OnboardingEvent.OnNextClicked) },
        modifier = modifier,
    )
}

// ────────────────────────────────────────────────────────────
// Hàm build AnnotatedString tiêu đề theo titleKey
// Tách riêng để OnboardingScreen không bị lẫn logic styling
// ────────────────────────────────────────────────────────────

/**
 * Chuyển [titleKey] thành [androidx.compose.ui.text.AnnotatedString] tương ứng.
 * Tách ra khỏi OnboardingPageLayout để layout không phụ thuộc vào nội dung cụ thể.
 */
@Composable
private fun buildPageTitle(titleKey: String) = when (titleKey) {
    "page_0" -> buildAnnotatedString {
        append("Tiền biến mất không rõ lý do?")
    }
    "page_1" -> buildAnnotatedString {
        append("Kiểm Soát Tiền Của Bạn\nNgay Hôm Nay")
    }
    // page_2 và mọi trang tương lai đều dùng nhánh else
    else -> {
        // Lấy màu primary để tô "myMoney" — phải gọi trong @Composable scope
        val primaryColor = MaterialTheme.colorScheme.primary
        buildAnnotatedString {
            append("Chào mừng bạn đến với\n")
            withStyle(style = SpanStyle(color = primaryColor)) {
                append("myMoney")
            }
        }
    }
}

// ── Previews — hiển thị từng trang riêng biệt ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPage0Preview() {
    MyMoneyTheme(darkTheme = false) {
        val page = onboardingPages[0]
        OnboardingPageLayout(
            title = buildAnnotatedString { append("Tiền biến mất không rõ lý do?") },
            description = page.description,
            imageRes = page.imageRes,
            imageDescription = page.imageDescription,
            currentPage = 0,
            totalPages = onboardingPages.size,
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPage1Preview() {
    MyMoneyTheme(darkTheme = false) {
        val page = onboardingPages[1]
        OnboardingPageLayout(
            title = buildAnnotatedString { append("Kiểm Soát Tiền Của Bạn\nNgay Hôm Nay") },
            description = page.description,
            imageRes = page.imageRes,
            imageDescription = page.imageDescription,
            currentPage = 1,
            totalPages = onboardingPages.size,
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPage2Preview() {
    MyMoneyTheme(darkTheme = true) {
        val page = onboardingPages[2]
        OnboardingPageLayout(
            title = buildAnnotatedString { append("Chào mừng bạn đến với\nmyMoney") },
            description = page.description,
            imageRes = page.imageRes,
            imageDescription = page.imageDescription,
            currentPage = 2,
            totalPages = onboardingPages.size,
            onNextClick = {},
        )
    }
}
