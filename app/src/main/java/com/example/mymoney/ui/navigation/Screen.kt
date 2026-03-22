package com.example.mymoney.ui.navigation

/**
 * Sealed class định nghĩa tất cả các route trong ứng dụng.
 * Dùng sealed class thay vì raw string để tránh lỗi chính tả và dễ refactor.
 */
sealed class Screen(val route: String) {

    // ── Onboarding ──
    /** Màn hình giới thiệu 1: "Tiền biến mất không rõ lý do?" */
    data object Start1 : Screen("start_1")

    /** Màn hình giới thiệu 2: "Kiểm Soát Tiền Của Bạn Ngay Hôm Nay" */
    data object Start2 : Screen("start_2")

    /** Màn hình giới thiệu 3: "Chào mừng bạn đến với myMoney" */
    data object Start3 : Screen("start_3")

    // ── Màn hình chính ──
    /** Màn hình chính sau khi hoàn thành onboarding */
    data object Main : Screen("main")
}
