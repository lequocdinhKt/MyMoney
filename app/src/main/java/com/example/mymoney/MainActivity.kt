package com.example.mymoney

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.remote.SupabaseClient
import com.example.mymoney.ui.navigation.AppNavigation
import com.example.mymoney.ui.navigation.Screen
import com.example.mymoney.ui.theme.MyMoneyTheme
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        // ── TEST: Kiểm tra kết nối Supabase ──
//        lifecycleScope.launch {
//            try {
//                // Thử lấy dữ liệu từ bảng "profiles" (thay tên bảng nếu cần)
//                val result = SupabaseClient.client.postgrest["profiles"]
//                    .select()
//                    .data  // JSON string trả về
//
//                Log.d("SupabaseTest", "✅ Kết nối thành công! Data: $result")
//            } catch (e: Exception) {
//                Log.e("SupabaseTest", "❌ Kết nối thất bại: ${e.message}", e)
//            }
//        }
        setContent {
                MyMoneyTheme {
                    // Đọc trạng thái onboarding từ DataStore tại đây — AppNavigation không tự đọc
                    val isOnboardingCompleted by SettingPreferences(this)
                        .isOnboardingCompleted
                        .collectAsState(initial = null)

                // Chờ DataStore đọc xong trước khi render NavHost (tránh flash màn hình sai)
                // null = đang tải → chưa render gì
                val startDestination: String = when (isOnboardingCompleted) {
                    true  -> Screen.Main.route        // đã xem → vào thẳng Main
                    false -> Screen.Onboarding.route  // chưa xem → hiện Onboarding
                    else  -> return@MyMoneyTheme      // đang tải → chờ DataStore
                }

                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    startDestination = startDestination,
                    // Callback không cần làm gì thêm — DataStore đã được lưu trong ViewModel
                    onOnboardingFinished = {}
                )
            }
        }
    }
}
