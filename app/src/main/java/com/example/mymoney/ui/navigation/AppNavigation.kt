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
import com.example.mymoney.ui.search.SearchScreen
import com.example.mymoney.ui.security.PinEntryScreen
import com.example.mymoney.ui.security.PinSetupScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.mymoney.ui.wallet.WalletSetupScreen
import com.example.mymoney.ui.budget.BudgetFormScreen
import com.example.mymoney.ui.streak.StreakScreen
import com.example.mymoney.ui.saving.AddSavingRecordScreen
import com.example.mymoney.ui.saving.SavingDetailScreen
import com.example.mymoney.ui.saving.SavingFormScreen
import com.example.mymoney.ui.recurring.RecurringScreen
import com.example.mymoney.presentation.viewmodel.streak.StreakViewModelFactory
import com.example.mymoney.ui.other.AboutUsScreen
import com.example.mymoney.ui.other.ReportBugScreen
import com.example.mymoney.ui.other.SupportUsScreen
import com.example.mymoney.ui.statistics.StatisticsScreen

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
        // Hiệu ứng chuyển cảnh mặc định: Fade nhanh
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) },
        popEnterTransition = { fadeIn(animationSpec = tween(250)) },
        popExitTransition = { fadeOut(animationSpec = tween(250)) }
    ) {
        // ── Màn hình onboarding thống nhất (gồm 3 trang nội bộ) ──
        composable(
            route = Screen.Onboarding.route,
        ) {
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
        composable(
            route = Screen.SignIn.route,
        ) {
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
        composable(
            route = Screen.SignUp.route,
        ) {
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
composable(
    route = Screen.Main.route,
) {
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
        onNavigateToBudgetManual = { budgetId ->
            navController.navigate(Screen.BudgetManual.createRoute(userId, budgetId))
        },
        onNavigateToDetailSaving = { goalId ->
            navController.navigate(Screen.SavingDetail.createRoute(goalId))
        },
        onNavigateToAddSavingForm = {
            navController.navigate(Screen.SavingForm.createRoute(userId))
        },
        onWalletColorChanged = onWalletColorChanged,
        onSearchClick = {
            navController.navigate("search")
        },
        onStatisticsClick = {
            navController.navigate(Screen.Statistics.route)
        },
        onNavigateToStreak = {
            navController.navigate(Screen.Streak.route)
        },
        onSignOut = {
            navController.navigate(Screen.SignIn.route) {
                popUpTo(0) { inclusive = true }
            }
        },
        onNavigateToPinSetup = {
            navController.navigate(Screen.PinSetup.route)
        },
        onNavigateToAboutUs = {
            navController.navigate(Screen.AboutUs.route)
        },
        onNavigateToReportBug = {
            navController.navigate(Screen.ReportBug.route)
        },
        onNavigateToSupportUs = {
            navController.navigate(Screen.SupportUs.route)
        }
    )
}
        // ── Màn hình tìm kiếm ──
        composable("search") {
            val context = LocalContext.current
            val db = AppDatabase.getInstance(context)
            val repo = TransactionRepositoryImpl(db.transactionDao())

            val factory = SearchViewModelFactory(
                GetTransactionsUseCase(repo),
                userId
            )

            SearchScreen(
                factory = factory,
                onBackClick = {
                    if (navController.currentBackStackEntry?.destination?.route == "search") {
                        navController.popBackStack()
                    }
                }
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
            val lifecycleOwner = LocalLifecycleOwner.current
            AIChatScreen(
                onNavigateBack = {
                    if (navController.currentBackStackEntry?.destination?.route == Screen.AddTransaction.route) {
                        navController.popBackStack()
                    }
                },
                onNavigateToRecurring = { wId -> navController.navigate(Screen.Recurring.createRoute(wId)) },
                onNavigateToCamera = { wId -> navController.navigate(Screen.CameraCapture.createRoute(wId)) },
                registerCameraResultListener = { callback ->
                    val saved = navController.currentBackStackEntry?.savedStateHandle
                    // Observe LiveData and forward to provided callback
                    saved?.getLiveData<String>("camera_ocr_result")?.observe(lifecycleOwner) { text ->
                        if (text != null) {
                            callback(text)
                            // remove after consumed
                            saved.remove<String>("camera_ocr_result")
                        }
                    }
                },
                walletId = walletId
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
                    if (navController.currentBackStackEntry?.destination?.route == Screen.CameraCapture.route) {
                        navController.popBackStack()
                    }
                },
                onPhotoTaken = { _ ->
                    // Giữ nguyên camera screen sau khi chụp — không pop back
                },
                onOcrResult = { text ->
                    // Forward OCR result back to previous screen via savedStateHandle
                    navController.previousBackStackEntry?.savedStateHandle?.set("camera_ocr_result", text)
                    if (navController.currentBackStackEntry?.destination?.route == Screen.CameraCapture.route) {
                        navController.popBackStack()
                    }
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
            route = Screen.BudgetManual.route,
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
                    if (navController.currentBackStackEntry?.destination?.route == Screen.BudgetManual.route) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ── Màn hình Chuỗi ngày ──
        composable(route = Screen.Streak.route) {
            val context = LocalContext.current
            val db = AppDatabase.getInstance(context)
            val repo = TransactionRepositoryImpl(db.transactionDao())
            val factory = StreakViewModelFactory(repo, userId)
            StreakScreen(
                factory = factory,
                onBackClick = {
                    if (navController.currentBackStackEntry?.destination?.route == Screen.Streak.route) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ── Màn hình giao dịch định kỳ ──
        composable(
            route = Screen.Recurring.route,
            arguments = listOf(
                navArgument("walletId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val walletId = backStackEntry.arguments?.getLong("walletId") ?: 0L
            RecurringScreen(
                walletId = walletId,
                onNavigateBack = {
                    if (navController.currentBackStackEntry?.destination?.route == Screen.Recurring.route) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ── Màn hình thống kê ──
        composable(route = Screen.Statistics.route) {
            StatisticsScreen(
                userId = userId,
                onBackClick = {
                    if (navController.currentBackStackEntry?.destination?.route == Screen.Statistics.route) {
                        navController.popBackStack()
                    }
                },
                onNavigateToBudget = {
                    navController.navigate(Screen.BudgetManual.createRoute(userId))
                }
            )
        }

        // ── Security ──
        composable(route = Screen.PinSetup.route) {
            PinSetupScreen(
                onNavigateBack = {
                    if (navController.currentBackStackEntry?.destination?.route == Screen.PinSetup.route) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(route = Screen.PinEntry.route) {
            PinEntryScreen(
                onSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.PinEntry.route) { inclusive = true }
                    }
                }
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
                onNavigateBack = {
                    if (navController.currentBackStackEntry?.destination?.route == Screen.SavingDetail.route) {
                        navController.popBackStack()
                    }
                },
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
                onNavigateBack = {
                    if (navController.currentBackStackEntry?.destination?.route == Screen.AddSavingRecord.route) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ── Màn hình Form tiết kiệm ──
        composable(
            route = Screen.SavingForm.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("userId") ?: userId
            SavingFormScreen(
                userId = uid,
                onNavigateBack = {
                    if (navController.currentBackStackEntry?.destination?.route == Screen.SavingForm.route) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ── Khác ──
        composable(route = Screen.AboutUs.route) {
            AboutUsScreen(onBackClick = {
                if (navController.currentBackStackEntry?.destination?.route == Screen.AboutUs.route) {
                    navController.popBackStack()
                }
            })
        }

        composable(route = Screen.ReportBug.route) {
            ReportBugScreen(onBackClick = {
                if (navController.currentBackStackEntry?.destination?.route == Screen.ReportBug.route) {
                    navController.popBackStack()
                }
            })
        }

        composable(route = Screen.SupportUs.route) {
            SupportUsScreen(onBackClick = {
                if (navController.currentBackStackEntry?.destination?.route == Screen.SupportUs.route) {
                    navController.popBackStack()
                }
            })
        }
    }
}
