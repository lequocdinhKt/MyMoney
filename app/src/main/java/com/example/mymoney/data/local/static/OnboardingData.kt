package com.example.mymoney.data.local.static

import androidx.annotation.DrawableRes
import com.example.mymoney.R

// ────────────────────────────────────────────────────────────
// Data model cho mỗi trang onboarding
// ────────────────────────────────────────────────────────────

/**
 * Dữ liệu tĩnh của một trang onboarding.
 *
 * Tiêu đề (AnnotatedString) được build tại OnboardingScreen
 * vì cần @Composable scope để đọc màu từ MaterialTheme.
 *
 * @param titleKey  Khoá định danh tiêu đề — dùng trong buildPageTitle()
 * @param description  Mô tả ngắn bên dưới tiêu đề
 * @param imageRes  Resource ID hình minh hoạ
 * @param imageDescription  Mô tả ảnh cho accessibility (contentDescription)
 */
data class OnboardingPageData(
    val titleKey: String,
    val description: String,
    @param:DrawableRes val imageRes: Int,
    val imageDescription: String,
)

// ────────────────────────────────────────────────────────────
// Danh sách tất cả trang — thêm/bớt trang chỉ cần sửa tại đây
// Số trang tự động = onboardingPages.size, không hardcode
// ────────────────────────────────────────────────────────────
val onboardingPages: List<OnboardingPageData> = listOf(
    OnboardingPageData(
        titleKey = "page_0",
        description = "Bạn quên theo dõi chi phí, hóa đơn ngày càng nhiều " +
                "và bạn không bao giờ biết chắc tiền của mình thực sự đi về đâu.",
        imageRes = R.drawable.img_batdau1,
        imageDescription = "Hình minh hoạ người suy nghĩ với các dấu chấm hỏi",
    ),
    OnboardingPageData(
        titleKey = "page_1",
        description = "Theo dõi chi phí trong vài giây, xem tiền của bạn " +
                "được chi vào đâu và kiểm soát ngân sách mà không bị căng thẳng.",
        imageRes = R.drawable.img_batdau2,
        imageDescription = "Hình minh hoạ người vui vẻ với tiền bay xung quanh",
    ),
    OnboardingPageData(
        titleKey = "page_2",
        description = "Đồng hành cùng bạn trên hành trình tự do tài chính",
        imageRes = R.drawable.ic_logomymoney,
        imageDescription = "Hình minh hoạ heo đất tiết kiệm với đồng xu",
    ),
)
