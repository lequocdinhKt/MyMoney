package com.example.mymoney.ui.screens.main

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mymoney.ui.components.CustomBottomBar
import com.example.mymoney.ui.navigation.BottomTab
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình chính của ứng dụng — chứa Bottom Navigation và nội dung các tab.
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // Tắt màu nền mặc định của Scaffold — để content tự quản lý
        containerColor = MaterialTheme.colorScheme.background,
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
                TabPlaceholder(title = "Trang chủ")
            }

            // ── Tab: Ngân sách ──
            composable(BottomTab.Budget.route) {
                TabPlaceholder(title = "Ngân sách")
            }

            // ── Tab: Tiết kiệm ──
            composable(BottomTab.Saving.route) {
                TabPlaceholder(title = "Tiết kiệm")
            }

            // ── Tab: Khác ──
            composable(BottomTab.Other.route) {
                TabPlaceholder(title = "Khác")
            }
        }
    }
}

/**
 * Placeholder tạm thời cho nội dung từng tab.
 * Sẽ thay thế bằng screen thực tế trong quá trình phát triển.
 *
 * @param title Tên tab hiển thị ở giữa màn hình
 */
@Composable
private fun TabPlaceholder(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
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
