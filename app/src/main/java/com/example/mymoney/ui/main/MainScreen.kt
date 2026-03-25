package com.example.mymoney.ui.main

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mymoney.ui.budget.BudgetScreen
import com.example.mymoney.ui.components.CustomBottomBar
import com.example.mymoney.ui.home.HomeScreen
import com.example.mymoney.ui.main.components.CustomTopAppBar
import com.example.mymoney.ui.navigation.BottomTab
import com.example.mymoney.ui.other.OtherScreen
import com.example.mymoney.ui.saving.SavingScreen
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình chính của ứng dụng – chứa Bottom Navigation và nội dung các tab.
 *
 * Cấu trúc:
 *   Scaffold
 *     ├── bottomBar  → CustomBottomBar (subtract.xml + FAB)
 *     └── content    → NavHost nội bộ cho 4 tab
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    // NavController riêng cho các tab bên trong MainScreen
    val tabNavController = rememberNavController()

    // Lấy route hiện tại để highlight tab đang chọn
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Xác định tab hiện tại → lấy title cho Top Bar
    val currentTab = BottomTab.fromRoute(currentRoute)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // Tắt màu nền mặc định của Scaffold – để content tự quản lý
        containerColor = MaterialTheme.colorScheme.background,
        // ── Top Bar: chỉ hiện khi tab có title (OtherScreen → title = null → ẩn) ──
        topBar = {
            currentTab?.title?.let { title ->
                CustomTopAppBar(
                    title = title,
                    onSettingsClick = {
                        // TODO: Mở màn hình cài đặt
                    },
                    onSearchClick = {
                        // TODO: Mở tìm kiếm
                    },
                    onCalendarClick = {
                        // TODO: Mở lịch
                    }
                )
            }
        },
        bottomBar = {
            CustomBottomBar(
                currentRoute = currentRoute,
                onTabSelected = { tab ->
                    // Điều hướng đến tab được chọn, tránh tạo nhiều bản sao trên stack
                    tabNavController.navigate(tab.route) {
                        popUpTo(tabNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddClick = {
                    // TODO: Mở màn hình thêm giao dịch
                }
            )
        }
    ) { innerPadding ->
        // ── NavHost nội bộ: render nội dung tab tương ứng ──
        NavHost(
            navController = tabNavController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(innerPadding),
            // Tắt animation chuyển tab
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            // ── Tab: Trang chủ ──
            composable(BottomTab.Home.route) {
                HomeScreen()
            }

            // ── Tab: Ngân sách ──
            composable(BottomTab.Budget.route) {
                BudgetScreen()
            }

            // ── Tab: Tiết kiệm ──
            composable(BottomTab.Saving.route) {
                SavingScreen()
            }

            // ── Tab: Khác ──
            composable(BottomTab.Other.route) {
                OtherScreen()
            }
        }
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        MainScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        MainScreen()
    }
}
