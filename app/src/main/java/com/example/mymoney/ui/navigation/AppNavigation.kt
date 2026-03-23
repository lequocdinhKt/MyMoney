package com.example.mymoney.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.ui.screens.main.MainScreen
import com.example.mymoney.ui.screens.onboarding.OnboardingScreen

/**
 * Navigation graph chính của ứng dụng.
 *
 * startDestination được quyết định dựa vào DataStore:
 *   - IS_ONBOARDING_COMPLETED = true  → vào thẳng MainScreen
 *   - IS_ONBOARDING_COMPLETED = false → hiện OnboardingScreen
 *
 * Tránh nhấp nháy màn hình: đọc Flow trực tiếp trước khi NavHost render.
 *
 * @param navController Controller điều hướng từ MainActivity
 * @param modifier Modifier tuỳ chỉnh
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Đọc trạng thái onboarding từ DataStore — null = đang tải, chưa render NavHost
    val isOnboardingCompleted by SettingPreferences(context)
        .isOnboardingCompleted
        .collectAsState(initial = null)

    // Chờ DataStore đọc xong (tránh flash màn hình sai)
    val startDestination = when (isOnboardingCompleted) {
        true  -> Screen.Main.route         // đã xem → vào thẳng Main
        false -> Screen.Onboarding.route   // chưa xem → hiện Onboarding
        null  -> return                    // đang tải → chưa render gì
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // Tắt toàn bộ animation khi chuyển màn hình
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // ── Màn hình onboarding thống nhất (gồm 3 trang nội bộ) ──
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        // ── Màn hình chính ──
        composable(route = Screen.Main.route) {
            MainScreen()
        }
    }
}
