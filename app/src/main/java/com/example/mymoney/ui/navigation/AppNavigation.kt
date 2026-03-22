package com.example.mymoney.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mymoney.ui.screens.main.MainScreen
import com.example.mymoney.ui.screens.startScreens.OnboardingEvent
import com.example.mymoney.ui.screens.startScreens.OnboardingViewModel
import com.example.mymoney.ui.screens.startScreens.StartScreen1
import com.example.mymoney.ui.screens.startScreens.StartScreen2
import com.example.mymoney.ui.screens.startScreens.StartScreen3

/**
 * Navigation graph chính của ứng dụng.
 *
 * Luồng: Start1 → Start2 → Start3 → Main
 *
 * Mỗi màn hình onboarding có ViewModel riêng (scoped theo NavBackStackEntry).
 * Navigation được điều khiển qua Event + State (UDF), không gọi trực tiếp trong UI.
 *
 * @param navController Controller điều hướng từ MainActivity
 * @param modifier Modifier tuỳ chỉnh
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Start1.route,
        modifier = modifier,
        // Tắt toàn bộ animation khi chuyển màn hình
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // ── Màn hình onboarding 1 ──
        composable(route = Screen.Start1.route) {
            // ViewModel scoped theo NavBackStackEntry → mỗi trang có instance riêng
            val viewModel: OnboardingViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Quan sát cờ navigation → điều hướng sang trang 2
            LaunchedEffect(uiState.shouldNavigateNext) {
                if (uiState.shouldNavigateNext) {
                    navController.navigate(Screen.Start2.route)
                    viewModel.onEvent(OnboardingEvent.OnNavigationHandled)
                }
            }

            // UI gửi event khi nhấn nút "Tiếp theo"
            StartScreen1(
                onNextClick = { viewModel.onEvent(OnboardingEvent.OnNextClicked) }
            )
        }

        // ── Màn hình onboarding 2 ──
        composable(route = Screen.Start2.route) {
            val viewModel: OnboardingViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Quan sát cờ navigation → điều hướng sang trang 3
            LaunchedEffect(uiState.shouldNavigateNext) {
                if (uiState.shouldNavigateNext) {
                    navController.navigate(Screen.Start3.route)
                    viewModel.onEvent(OnboardingEvent.OnNavigationHandled)
                }
            }

            StartScreen2(
                onNextClick = { viewModel.onEvent(OnboardingEvent.OnNextClicked) }
            )
        }

        // ── Màn hình onboarding 3 ──
        composable(route = Screen.Start3.route) {
            val viewModel: OnboardingViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Quan sát cờ navigation → điều hướng sang màn hình chính
            // popUpTo xoá toàn bộ onboarding khỏi back stack để không quay lại được
            LaunchedEffect(uiState.shouldNavigateNext) {
                if (uiState.shouldNavigateNext) {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Start1.route) { inclusive = true }
                    }
                    viewModel.onEvent(OnboardingEvent.OnNavigationHandled)
                }
            }

            StartScreen3(
                onNextClick = { viewModel.onEvent(OnboardingEvent.OnNextClicked) }
            )
        }

        // ── Màn hình chính ──
        composable(route = Screen.Main.route) {
            MainScreen()
        }
    }
}
