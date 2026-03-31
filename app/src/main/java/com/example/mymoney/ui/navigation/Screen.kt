package com.example.mymoney.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class định nghĩa tất cả các route trong ứng dụng.
 * Dùng sealed class thay vì raw string để tránh lỗi chính tả và dễ refactor.
 */
sealed class Screen(val route: String) {

    // ── Onboarding ──
    /** Màn hình onboarding thống nhất (thay thế Start1, Start2, Start3) */
    data object Onboarding : Screen("onboarding")

    // ── Auth ──
    /** Màn hình đăng nhập */
    data object SignIn : Screen("sign_in")

    /** Màn hình đăng ký */
    data object SignUp : Screen("sign_up")

    // ── Màn hình chính (shell chứa bottom bar + nội dung tab) ──
    data object Main : Screen("main")

    // ── Màn hình thêm giao dịch (mở từ FAB trong Bottom Bar) ──
    data object AddTransaction : Screen("add_transaction")
}

/**
 * Sealed class quản lý các tab trong Bottom Navigation.
 * Mỗi tab có route, label hiển thị, icon và title (cho Top Bar).
 *
 * @param route Đường dẫn điều hướng của tab
 * @param label Tên hiển thị bên dưới icon
 * @param icon Icon Material hiển thị trên tab
 * @param title Tiêu đề hiển thị trên Top Bar — null nếu tab không cần Top Bar
 */
sealed class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val title: String? = null
) {
    // ── Cụm bên trái (trước chỗ lõm) ──
    data object Home : BottomTab(
        route = "tab_home",
        label = "Trang chủ",
        icon = Icons.Filled.Home,
        title = "Trang chủ"
    )

    data object Budget : BottomTab(
        route = "tab_budget",
        label = "Ngân sách",
        icon = Icons.Filled.AccountBalanceWallet,
        title = "Ngân sách"
    )

    // ── Cụm bên phải (sau chỗ lõm) ──
    data object Saving : BottomTab(
        route = "tab_saving",
        label = "Tiết kiệm",
        icon = Icons.Filled.Savings,
        title = "Tiết kiệm"
    )

    /** Màn hình "Khác" – title = null → Top Bar sẽ bị ẩn */
    data object Other : BottomTab(
        route = "tab_other",
        label = "Khác",
        icon = Icons.Filled.MoreHoriz,
        title = null
    )

    companion object {
        /** Danh sách tất cả các tab — dùng để render Bottom Bar */
        val all = listOf(Home, Budget, Saving, Other)

        /**
         * Tìm BottomTab tương ứng với route hiện tại.
         * Dùng trong MainScreen để xác định tab đang hiển thị → truyền title cho Top Bar.
         */
        fun fromRoute(route: String?): BottomTab? =
            all.firstOrNull { it.route == route }
    }
}

