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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.TransactionRepositoryImpl
import com.example.mymoney.domain.usecase.GetTransactionsUseCase
import com.example.mymoney.presentation.viewmodel.search.SearchViewModelFactory
import com.example.mymoney.ui.addtransaction.AIChatScreen
import com.example.mymoney.ui.auth.SignInScreen
import com.example.mymoney.ui.auth.SignUpScreen
import com.example.mymoney.ui.camera.CameraCaptureScreen
import com.example.mymoney.ui.main.MainScreen
import com.example.mymoney.ui.onboarding.OnboardingScreen
import com.example.mymoney.ui.wallet.WalletSetupScreen
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
    onWalletColorChanged: (colorHex: String) -> Unit = {},
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
        onAddTransactionClick = { walletId ->
            navController.navigate(Screen.AddTransaction.createRoute(walletId))
        },
        onCameraClick = { walletId ->
            navController.navigate(Screen.CameraCapture.createRoute(walletId))
        },
        onNavigateToAddWallet = {
            navController.navigate(Screen.WalletSetup.createRoute(userId))
        },
        onNavigateToEditWallet = { walletId ->
            navController.navigate(Screen.WalletSetup.createRoute(userId, walletId))
        },
        onWalletColorChanged = onWalletColorChanged,
        onSearchClick = {
            navController.navigate("search")
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
                GetTransactionsUseCase(repo),
                userId
            )

            SearchScreen(
                factory = factory,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Màn hình chat AI thêm giao dịch ──
        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("walletId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val walletId = backStackEntry.arguments?.getLong("walletId") ?: 0L
            AIChatScreen(
                walletId = walletId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Màn hình chụp ảnh (Locket-style) ──
        composable(
            route = Screen.CameraCapture.route,
            arguments = listOf(
                navArgument("walletId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val walletId = backStackEntry.arguments?.getLong("walletId") ?: 0L
            CameraCaptureScreen(
                walletId = walletId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPhotoTaken = { photoUri ->
                    // Giữ nguyên camera screen sau khi chụp — không pop back
                }
            )
        }

        // ── Màn hình thiết lập ví ──
        composable(
            route     = Screen.WalletSetup.route,
            arguments = listOf(
                navArgument("userId")   { type = NavType.StringType },
                navArgument("walletId") { type = NavType.LongType   }
            )
        ) { backStackEntry ->
            val uid  = backStackEntry.arguments?.getString("userId") ?: userId
            val wId  = backStackEntry.arguments?.getLong("walletId") ?: -1L
            WalletSetupScreen(
                userId         = uid,
                walletId       = if (wId == -1L) null else wId,
                onNavigateBack = {
                    // Guard: chỉ pop khi destination hiện tại vẫn còn là WalletSetup.
                    // Nếu người dùng nhấn back 2 lần liên tiếp nhanh, lần thứ 2 sẽ
                    // không làm gì thêm vì route lúc đó đã không còn là WalletSetup.
                    if (navController.currentBackStackEntry?.destination?.route
                            == Screen.WalletSetup.route) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
