package com.example.mymoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.ui.navigation.AppNavigation
import com.example.mymoney.ui.navigation.Screen
import com.example.mymoney.ui.theme.MyMoneyTheme
import com.example.mymoney.worker.ChatCleanupWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Lên lịch xóa chat_messages cũ hơn 48h, chạy mỗi 12h
        ChatCleanupWorker.schedule(this)

        setContent {
            // Màu primary động theo ví đang active — survive recomposition
            var primaryHex by rememberSaveable { mutableStateOf("#0088F0") }

            MyMoneyTheme(primaryHex = primaryHex) {
                val prefs = SettingPreferences(this)

                val isOnboardingCompleted by prefs
                    .isOnboardingCompleted
                    .collectAsState(initial = null)

                val currentUserId by prefs
                    .currentUserId
                    .collectAsState(initial = "loading")

                val navController = rememberNavController()

                if (isOnboardingCompleted == null || currentUserId == "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                    return@MyMoneyTheme
                }

                val startDestination: String = when {
                    isOnboardingCompleted == false -> Screen.Onboarding.route
                    currentUserId != null -> Screen.Main.route
                    else -> Screen.SignIn.route
                }

                AppNavigation(
                    navController        = navController,
                    startDestination     = startDestination,
                    userId               = currentUserId ?: "",
                    onOnboardingFinished = {},
                    onWalletColorChanged = { hex -> primaryHex = hex }
                )
            }
        }
    }
}
