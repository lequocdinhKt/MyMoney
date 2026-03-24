package com.example.mymoney.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mymoney.ui.main.MainScreen
import com.example.mymoney.ui.onboarding.OnboardingScreen

/**
 * Navigation graph chính của ứng dụng.
 *
 * Trách nhiệm của file này:
 *   - Định nghĩa NavHost và các route
 *   - KHÔNG tự đọc DataStore — startDestination được truyền từ MainActivity
 *
 * Luồng onboarding:
 *   MainActivity đọc DataStore → truyền startDestination vào AppNavigation
 *   OnboardingScreen hoàn thành → gọi onOnboardingFinished → AppNavigation navigate sang Main
 *
 * @param navController      Controller điều hướng từ MainActivity
 * @param startDestination   Route khởi đầu — do MainActivity quyết định dựa vào DataStore
 * @param onOnboardingFinished Callback được gọi khi OnboardingScreen hoàn thành
 * @param modifier           Modifier tuỳ chỉnh
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            OnboardingScreen(
                // Khi onboarding hoàn thành, điều hướng sang Main và xoá Onboarding khỏi back stack
                onFinished = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                    onOnboardingFinished()
                }
            )
        }

        // ── Màn hình chính ──
        composable(route = Screen.Main.route) {
            MainScreen()
        }
    }
}
