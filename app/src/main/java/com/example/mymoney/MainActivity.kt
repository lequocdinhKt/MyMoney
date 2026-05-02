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
import com.example.mymoney.worker.ChatCleanupWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Lên lịch xóa chat_messages cũ hơn 48h, chạy mỗi 12h
        ChatCleanupWorker.schedule(this)

        setContent {
                MyMoneyTheme {
                    val prefs = SettingPreferences(this)

                    // Đọc trạng thái onboarding từ DataStore
                    // Flow<Boolean> — emit false nếu chưa set (lần đầu cài app)
                    // initial = null để phân biệt "DataStore chưa emit" vs "đã emit false"
                    val isOnboardingCompleted by prefs
                        .isOnboardingCompleted
                        .collectAsState(initial = null)

                    // Đọc userId từ DataStore — nếu có nghĩa là đã đăng nhập trước đó
                    // "loading" = sentinel trước khi DataStore emit lần đầu
                    val currentUserId by prefs
                        .currentUserId
                        .collectAsState(initial = "loading")

                    // rememberNavController phải đặt ở đây (ngoài khối if)
                    // để không bị tạo lại mỗi khi DataStore emit giá trị mới
                    val navController = rememberNavController()

                // Chờ DataStore emit xong mới render NavHost.
                // isOnboardingCompleted == null  → Flow<Boolean> chưa emit lần nào
                // currentUserId == "loading"     → Flow<String?> chưa emit lần nào
                // Sau khi cả hai emit (dù chỉ vài ms), điều kiện này trở thành false.
                if (isOnboardingCompleted == null || currentUserId == "loading") {
                    return@MyMoneyTheme
                }

                val startDestination: String = when {
                    // Chưa xem onboarding (false từ DataStore) → hiện Onboarding
                    isOnboardingCompleted == false -> Screen.Onboarding.route
                    // Đã đăng nhập (userId != null và != "loading") → vào thẳng Main
                    currentUserId != null -> Screen.Main.route
                    // Đã xem onboarding nhưng chưa đăng nhập → hiện Sign In
                    else -> Screen.SignIn.route
                }

                AppNavigation(
                    navController = navController,
                    startDestination = startDestination,
                    userId = currentUserId ?: "",
                    onOnboardingFinished = {}
                )
            }
        }
    }
}
