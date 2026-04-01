package com.example.mymoney.ui.main.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mymoney.ui.budget.BudgetScreen
import com.example.mymoney.ui.home.HomeScreen
import com.example.mymoney.ui.navigation.BottomTab
import com.example.mymoney.ui.other.OtherScreen
import com.example.mymoney.ui.saving.SavingScreen

/**
 * NavHost nội bộ cho các tab trong MainScreen.
 *
 * Tách riêng để MainScreen.kt không bị phình to khi thêm tab mới.
 * Mỗi khi cần thêm/sửa tab, chỉ cần chỉnh file này.
 *
 * @param navController  NavController riêng của MainScreen (không phải navController gốc)
 * @param innerPadding   PaddingValues từ Scaffold để tránh bị che bởi TopBar/BottomBar
 */
@Composable
fun MainNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = BottomTab.Home.route,
        modifier = Modifier.padding(innerPadding),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(BottomTab.Home.route)   { HomeScreen() }
        composable(BottomTab.Budget.route) { BudgetScreen() }
        composable(BottomTab.Saving.route) { SavingScreen() }
        composable(BottomTab.Other.route)  { OtherScreen() }
    }
}
