package com.example.mymoney.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mymoney.ui.components.CustomBottomBar
import com.example.mymoney.ui.main.components.CustomTopAppBar
import com.example.mymoney.ui.main.components.MainDrawerOverlay
import com.example.mymoney.ui.main.components.MainNavHost
import com.example.mymoney.ui.navigation.BottomTab
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình chính của ứng dụng – chứa Bottom Navigation và nội dung các tab.
 *
 * Cấu trúc:
 *   Box
 *     ├── Scaffold → CustomTopAppBar + CustomBottomBar + MainNavHost
 *     └── MainDrawerOverlay → hiện khi isDrawerOpen = true
 *
 * Các thành phần con:
 *   - [MainNavHost]       : NavHost 4 tab (ui/main/components/MainNavHost.kt)
 *   - [MainDrawerOverlay] : Drawer tự viết thay ModalNavigationDrawer (ui/main/components/MainDrawerOverlay.kt)
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onAddTransactionClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTab = BottomTab.fromRoute(currentRoute)

    var isDrawerOpen by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                currentTab?.title?.let { title ->
                    CustomTopAppBar(
                        title = title,
                        onSettingsClick = { isDrawerOpen = true },
                        onSearchClick = { /* TODO */ },
                        onCalendarClick = { /* TODO */ }
                    )
                }
            },
            bottomBar = {
                CustomBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { tab ->
                        tabNavController.navigate(tab.route) {
                            popUpTo(tabNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddClick = { onAddTransactionClick() }
                )
            }
        ) { innerPadding ->
            MainNavHost(
                navController = tabNavController,
                innerPadding = innerPadding
            )
        }

        MainDrawerOverlay(
            isOpen = isDrawerOpen,
            onClose = { isDrawerOpen = false },
            onSignOut = onSignOut
        )
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) { MainScreen() }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) { MainScreen() }
}
