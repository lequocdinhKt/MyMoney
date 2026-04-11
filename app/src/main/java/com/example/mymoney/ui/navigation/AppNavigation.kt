package com.example.mymoney.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.TransactionRepositoryImpl
import com.example.mymoney.domain.usecase.GetTransactionsUseCase
import com.example.mymoney.presentation.viewmodel.search.SearchViewModelFactory
import com.example.mymoney.ui.addtransaction.AIChatScreen
import com.example.mymoney.ui.auth.SignInScreen
import com.example.mymoney.ui.auth.SignUpScreen
import com.example.mymoney.ui.main.MainScreen
import com.example.mymoney.ui.onboarding.OnboardingScreen
import com.example.mymoney.ui.search.SearchScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

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
    modifier: Modifier = Modifier,
    userId: String = "",
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
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                    onOnboardingFinished()
                }
            )
        }

        // ── Màn hình đăng nhập ──
        composable(route = Screen.SignIn.route) {
            SignInScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
            )
        }

        // ── Màn hình đăng ký ──
        composable(route = Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.popBackStack()
                },
            )
        }

        // ── Màn hình chính ──
        composable(route = Screen.Main.route) {
            MainScreen(
                userId = userId,
                onSearchClick = {
                    navController.navigate("search")
                },
                onAddTransactionClick = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onSignOut = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        // ── Màn hình tìm kiếm ──
        composable(
            "search",
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it }, // từ dưới lên
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { -it / 4 }, // đi xuống
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInVertically(
                    initialOffsetY = { -it / 4 },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(
                        400,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        ) {
            val context = LocalContext.current
            val db = AppDatabase.getInstance(context)
            val repo = TransactionRepositoryImpl(db.transactionDao())

            val factory = SearchViewModelFactory(
                GetTransactionsUseCase(repo)
            )

            SearchScreen(
                factory = factory,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Màn hình chat AI thêm giao dịch ──
        composable(route = Screen.AddTransaction.route) {
            AIChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
