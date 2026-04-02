package com.example.mymoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.ui.navigation.AppNavigation
import com.example.mymoney.ui.navigation.Screen
import com.example.mymoney.ui.theme.MyMoneyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
                MyMoneyTheme {
                    val prefs = SettingPreferences(this)

                    // Đọc trạng thái onboarding từ DataStore
                    val isOnboardingCompleted by prefs
                        .isOnboardingCompleted
                        .collectAsState(initial = null)

                    // Đọc userId từ DataStore — nếu có nghĩa là đã đăng nhập trước đó
                    val currentUserId by prefs
                        .currentUserId
                        .collectAsState(initial = "loading") // "loading" = đang đọc DataStore

                    // rememberNavController phải đặt ở đây (ngoài khối if)
                    // để không bị tạo lại mỗi khi DataStore emit giá trị mới
                    val navController = rememberNavController()

                // Chờ DataStore đọc xong trước khi render NavHost (tránh flash màn hình sai)
                // null/"loading" = đang tải → chưa render gì
                if (isOnboardingCompleted == null || currentUserId == "loading") {
                    return@MyMoneyTheme // chờ DataStore
                }

                val startDestination: String = when {
                    // Chưa xem onboarding → hiện Onboarding
                    isOnboardingCompleted == false -> Screen.Onboarding.route
                    // Đã đăng nhập (userId != null) → vào thẳng Main
                    currentUserId != null -> Screen.Main.route
                    // Đã xem onboarding nhưng chưa đăng nhập → hiện Sign In
                    else -> Screen.SignIn.route
                }

                AppNavigation(
                    navController = navController,
                    startDestination = startDestination,
                    userId = currentUserId ?: "",
                    // Callback không cần làm gì thêm — DataStore đã được lưu trong ViewModel
                    onOnboardingFinished = {}
                )
            }
        }
    }
}
