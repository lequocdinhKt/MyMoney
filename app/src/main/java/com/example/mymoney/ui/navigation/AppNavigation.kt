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
import com.example.mymoney.ui.budget.BudgetFormScreen
import com.example.mymoney.ui.profile.ProfileScreen
import com.example.mymoney.ui.saving.AddSavingRecordScreen
import com.example.mymoney.ui.saving.SavingDetailScreen
import com.example.mymoney.ui.saving.SavingFormScreen

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
        onNavigateToBudgetForm = { budgetId ->
            navController.navigate(Screen.BudgetForm.createRoute(userId, budgetId))
        },

        onNavigateToAddSavingForm = {
            navController.navigate(Screen.SavingForm.createRoute(userId))
        },
        onNavigateToDetailSaving = { goalId ->
            navController.navigate(Screen.SavingDetail.createRoute(goalId))
        },
        onWalletColorChanged = onWalletColorChanged,
        onSearchClick = {
            navController.navigate("search")
        },
        onNavigateToProfile = {
            navController.navigate(Screen.Profile.route)
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

        // ── Màn hình thiết lập ngân sách ──
        composable(
            route = Screen.BudgetForm.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("budgetId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("userId") ?: userId
            val bId = backStackEntry.arguments?.getLong("budgetId") ?: -1L
            BudgetFormScreen(
                userId = uid,
                budgetId = if (bId == -1L) null else bId,
                onNavigateBack = {
                    if (navController.currentBackStackEntry?.destination?.route == Screen.BudgetForm.route) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ── Màn hình thay đổi thông tin người dùng ──
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSignIn = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Màn hình thiết lập tiết kiệm ──
        composable(
            route = Screen.SavingForm.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("userId") ?: userId
            SavingFormScreen(
                userId = uid,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Màn hình chi tiết tiết kiệm ──
        composable(
            route = Screen.SavingDetail.route,
            arguments = listOf(navArgument("goalId") { type = NavType.LongType })
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
            SavingDetailScreen(
                goalId = goalId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddRecord = { gId ->
                    navController.navigate(Screen.AddSavingRecord.createRoute(userId, gId))
                }
            )
        }

        // ── Màn hình thêm bản ghi tiết kiệm ──
        composable(
            route = Screen.AddSavingRecord.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("goalId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("userId") ?: userId
            val gId = backStackEntry.arguments?.getLong("goalId") ?: 0L
            AddSavingRecordScreen(
                userId = uid,
                goalId = gId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}