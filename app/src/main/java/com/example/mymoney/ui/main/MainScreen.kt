package com.example.mymoney.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.TransactionRepositoryImpl
import com.example.mymoney.data.repository.WalletRepositoryImpl
import com.example.mymoney.domain.usecase.GetPeriodSummaryUseCase
import com.example.mymoney.domain.usecase.GetTotalBalanceUseCase
import com.example.mymoney.domain.usecase.GetTransactionsByPeriodUseCase
import com.example.mymoney.domain.usecase.GetTransactionsUseCase
import com.example.mymoney.presentation.viewmodel.home.HomeViewModelFactory
import com.example.mymoney.presentation.viewmodel.search.SearchViewModelFactory
import com.example.mymoney.ui.components.CustomBottomBar
import com.example.mymoney.ui.main.components.CustomTopAppBar
import com.example.mymoney.ui.main.components.MainDrawerOverlay
import com.example.mymoney.ui.main.components.MainNavHost
import com.example.mymoney.ui.main.components.drawerBlur
import com.example.mymoney.ui.navigation.BottomTab
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình chính của ứng dụng – chứa Bottom Navigation và nội dung các tab.
 *
 * Cấu trúc Z-layer (xếp chồng theo trục Z):
 *   Box
 *     ├── [Layer 1] Scaffold + MainNavHost   ← Main Content, blur dần khi drawer mở
 *     └── MainDrawerOverlay
 *           ├── [Layer 2] Scrim              ← Tối dần: alpha = X × 0.5
 *           └── [Layer 3] Drawer Panel       ← Trượt từ trái: offsetX = -300dp × (1 - X)
 *
 * Biến X (drawerProgress) ∈ [0f, 1f] — chia sẻ giữa MainScreen và MainDrawerOverlay
 * để Layer 1 (blur), Layer 2 (scrim), Layer 3 (drawer) luôn đồng bộ.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    userId: String = "",
    onAddTransactionClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current

    // Build dependencies — sẽ thay bằng DI framework sau
    val homeViewModelFactory = remember(userId) {
        val db = AppDatabase.getInstance(context)
        val transactionRepo = TransactionRepositoryImpl(db.transactionDao())
        val walletRepo      = WalletRepositoryImpl(db.walletDao())
        HomeViewModelFactory(
            getTransactionsByPeriod = GetTransactionsByPeriodUseCase(transactionRepo),
            getPeriodSummary        = GetPeriodSummaryUseCase(transactionRepo),
            getTotalBalance         = GetTotalBalanceUseCase(walletRepo),
            userId                  = userId
        )
    }

    val searchViewModelFactory = remember {
        val db = AppDatabase.getInstance(context)
        val repo = TransactionRepositoryImpl(db.transactionDao())

        SearchViewModelFactory(
            getTransactionsUseCase = GetTransactionsUseCase(repo)
        )
    }

    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // Fallback về Home khi currentTab null (frame đầu tiên trước khi NavHost emit route)
    // → TopBar luôn hiển thị ngay lập tức, không bị flash trắng
    val currentTab = BottomTab.fromRoute(currentRoute) ?: BottomTab.Home

    var isDrawerOpen by rememberSaveable { mutableStateOf(false) }

    // ── drawerProgress: biến X chia sẻ giữa Layer 1 (blur) và Overlay (scrim + drawer) ──
    // Layer 1 cần X để tính blurRadius = X × 18px
    val drawerProgress = remember { Animatable(0f) }
    LaunchedEffect(isDrawerOpen) {
        drawerProgress.animateTo(
            targetValue = if (isDrawerOpen) 1f else 0f,
            animationSpec = tween(durationMillis = 320)
        )
    }

    Box(modifier = modifier.fillMaxSize()) {

        // ─────────────────────────────────────────────────────────────
        // LAYER 1 — Main Content
        // drawerBlur: blurRadius = drawerProgress × 18px
        //   drawerProgress=0 → blur=0px  (sắc nét)
        //   drawerProgress=1 → blur=18px (mờ kiểu iOS)
        // ─────────────────────────────────────────────────────────────
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .drawerBlur(drawerProgress.value),   // ← iOS Backdrop Blur
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if(currentRoute != "search") {
                    CustomTopAppBar(
                        title = currentTab.title ?: currentTab.label,
                        onSettingsClick = { isDrawerOpen = true },
                        showback = currentRoute == "search",
                        onBackClick = { tabNavController.popBackStack() },
                        onSearchClick = { tabNavController.navigate("search") {
                            launchSingleTop = true
                        } },
                        onCalendarClick = { /* TODO */ }
                    )
                }
            },
            bottomBar = {
                if (currentRoute != "search"){
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
            }
        ) { innerPadding ->
            MainNavHost(
                navController = tabNavController,
                innerPadding = innerPadding,
                homeViewModelFactory = homeViewModelFactory,
                searchViewModelFactory = searchViewModelFactory
            )
        }

        // Layer 2 + Layer 3 nằm trong MainDrawerOverlay
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
