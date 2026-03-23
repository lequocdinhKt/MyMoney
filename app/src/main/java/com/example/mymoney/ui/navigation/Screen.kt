package com.example.mymoney.ui.navigation

/**
 * Sealed class định nghĩa tất cả các route trong ứng dụng.
 * Dùng sealed class thay vì raw string để tránh lỗi chính tả và dễ refactor.
 */
sealed class Screen(val route: String) {

    // ── Onboarding ──
    /** Màn hình onboarding thống nhất (thay thế Start1, Start2, Start3) */
    data object Onboarding : Screen("onboarding")

    // ── Màn hình chính ──
    /** Màn hình chính sau khi hoàn thành onboarding */
    data object Main : Screen("main")
}
