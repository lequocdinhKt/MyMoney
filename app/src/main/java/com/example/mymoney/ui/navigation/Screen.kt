package com.example.mymoney.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class định nghĩa tất cả các route trong ứng dụng.
 */
sealed class Screen(val route: String) {

    // ── Onboarding ──
    data object Onboarding : Screen("onboarding")

    // ── Auth ──
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")

    // ── Màn hình chính ──
    data object Main : Screen("main")

    // ── Giao dịch & Camera ──
    data object AddTransaction : Screen("add_transaction/{walletId}") {
        fun createRoute(walletId: Long = 0L) = "add_transaction/$walletId"
    }

    data object CameraCapture : Screen("camera_capture/{walletId}") {
        fun createRoute(walletId: Long = 0L) = "camera_capture/$walletId"
    }

    // ── Thiết lập ví ──
    data object WalletSetup : Screen("wallet_setup/{userId}/{walletId}") {
        fun createRoute(userId: String, walletId: Long = -1L) =
            "wallet_setup/$userId/$walletId"
    }

    // ── Thiết lập ngân sách ──
    data object BudgetForm : Screen("budget_form/{userId}/{budgetId}") {
        fun createRoute(userId: String, budgetId: Long = -1L) =
            "budget_form/$userId/$budgetId"
    }

    // ── Màn hình hồ sơ ──
    data object Profile : Screen("profile")

    // ── Tiết kiệm ──
    data object SavingForm : Screen("saving_form/{userId}") {
        fun createRoute(userId: String) = "saving_form/$userId"
    }

    data object SavingDetail : Screen("saving_detail/{goalId}") {
        fun createRoute(goalId: Long) = "saving_detail/$goalId"
    }

    // ── Màn hình thêm bản ghi tiết kiệm ──
    data object AddSavingRecord : Screen("add_saving_record/{userId}/{goalId}") {
        fun createRoute(userId: String, goalId: Long) = "add_saving_record/$userId/$goalId"
    }
}

/**
 * Sealed class quản lý các tab trong Bottom Navigation.
 */
sealed class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val title: String? = null
) {
    data object Home : BottomTab("tab_home", "Trang chủ", Icons.Filled.Home, "Trang chủ")
    data object Budget : BottomTab("tab_budget", "Ngân sách", Icons.Filled.AccountBalanceWallet, "Ngân sách")
    data object Saving : BottomTab("tab_saving", "Tiết kiệm", Icons.Filled.Savings, "Tiết kiệm")
    data object Other : BottomTab("tab_other", "Khác", Icons.Filled.MoreHoriz, null)

    companion object {
        val all = listOf(Home, Budget, Saving, Other)
        fun fromRoute(route: String?): BottomTab? = all.firstOrNull { it.route == route }
    }
}
