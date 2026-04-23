package com.example.mymoney.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mymoney.presentation.viewmodel.setting.setting.CurrencyMode
import com.example.mymoney.presentation.viewmodel.setting.setting.NumberFormat
import com.example.mymoney.presentation.viewmodel.setting.setting.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ────────────────────────────────────────────────────────────
// DataStore singleton — 1 instance duy nhất cho toàn app
// Khai báo extension property tại top-level của file
// ────────────────────────────────────────────────────────────
private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "app_settings")

/**
 * Lớp quản lý tất cả các preference liên quan đến cài đặt ứng dụng.
 * Dùng DataStore (thay thế SharedPreferences) — hỗ trợ coroutine và Flow.
 *
 * Cách dùng:
 * ```
 * val prefs = SettingPreferences(context)
 * prefs.saveOnboardingCompleted()   // ghi
 * prefs.isOnboardingCompleted       // đọc (Flow)
 * ```
 */
class SettingPreferences(private val context: Context) {

    companion object {
        // Key lưu trạng thái đã xem hết onboarding
        private val KEY_IS_ONBOARDING_COMPLETED =
            booleanPreferencesKey("IS_ONBOARDING_COMPLETED")

        // Key lưu user ID từ Supabase Auth (UUID string)
        private val KEY_SUPABASE_USER_ID =
            stringPreferencesKey("SUPABASE_USER_ID")

        // Key lưu username — lưu khi đăng nhập/đăng ký để đọc offline không cần network
        private val KEY_USERNAME =
            stringPreferencesKey("USERNAME")

        // Key lưu trạng thái bật/tắt dấu phân cách hàng nghìn
        private val KEY_IS_THOUSAND_SEPARATOR_ENABLED =
            booleanPreferencesKey("IS_THOUSAND_SEPARATOR_ENABLED")

        // Key lưu trạng thái giao diện (Sáng/Tối/Hệ thống)
        private val KEY_THEME_MODE =
            stringPreferencesKey("THEME_MODE")

        private val KEY_CURRENCY_MODE =
            stringPreferencesKey("CURRENCY_MODE")

        private val KEY_NUMBER_FORMAT_MODE =
            stringPreferencesKey("NUMBER_FORMAT_MODE")
    }

    // ── Đọc ──

    /**
     * Flow phát ra true nếu người dùng đã hoàn thành onboarding, false nếu chưa.
     * Mặc định = false (lần đầu cài app chưa có giá trị).
     */
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_IS_ONBOARDING_COMPLETED] ?: false
        }

    /**
     * Flow phát ra user ID hiện tại (UUID string) hoặc null nếu chưa đăng nhập.
     */
    val currentUserId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SUPABASE_USER_ID]
        }

    /**
     * Flow phát ra username đã lưu khi đăng nhập/đăng ký.
     * Đọc từ DataStore — không cần network, luôn có ngay khi mở app.
     */
    val currentUsername: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_USERNAME] ?: ""
        }

    /**
     * Flow phát ra true nếu dấu phân cách hàng nghìn đang bật, false nếu tắt.
     * Mặc định = true (bật sẵn cho dễ đọc số tiền).
     */
    val isThousandSeparatorEnabled: Flow<Boolean> = context.dataStore.data
            .map { preferences ->
                preferences[KEY_IS_THOUSAND_SEPARATOR_ENABLED] ?: true
            }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map {preferences ->
            when(preferences[KEY_THEME_MODE]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    val currencyMode: Flow<CurrencyMode> = context.dataStore.data
        .map { preferences ->
            when(preferences[KEY_CURRENCY_MODE]) {
                // ──  ──
                else -> CurrencyMode.VND
            }
        }

    val numberFormat: Flow<NumberFormat> = context.dataStore.data
        .map { preferences ->
            when(preferences[KEY_NUMBER_FORMAT_MODE]) {
                // ── ──
                else -> NumberFormat.COMMA
            }
        }

    // ── Ghi ──

    /**
     * Lưu trạng thái đã hoàn thành onboarding vào DataStore.
     * Gọi suspend nên phải chạy trong coroutine scope (ViewModel dùng viewModelScope).
     */
    suspend fun saveOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_ONBOARDING_COMPLETED] = true
        }
    }

    /**
     * Lưu user ID sau khi đăng nhập/đăng ký thành công.
     * @param userId UUID string từ Supabase auth.uid()
     */
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SUPABASE_USER_ID] = userId
        }
    }

    /**
     * Xoá user ID khi đăng xuất.
     */
    suspend fun clearUserId() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_SUPABASE_USER_ID)
        }
    }

    /**
     * Lưu username vào DataStore ngay khi đăng nhập/đăng ký thành công.
     * Đảm bảo username luôn có sẵn khi mở app lại — không cần gọi Supabase network.
     */
    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = username
        }
    }

    /**
     * Xóa username khi đăng xuất.
     */
    suspend fun clearUsername() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_USERNAME)
        }
    }

    /**
     * Lưu trạng thái bật/tắt dấu phân cách hàng nghìn.
     * @param enabled true = bật, false = tắt
     */
    suspend fun setThousandSeparatorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_THOUSAND_SEPARATOR_ENABLED] = enabled
        }
    }

    /**
     * Lưu trạng thái giao diện app (Sáng/Tối/Hệ thống).
     * @param
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setCurrencyMode(currency: CurrencyMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENCY_MODE] = currency.name
        }
    }

    suspend fun setNumberFormatMode(numerFormat: NumberFormat) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NUMBER_FORMAT_MODE] = numerFormat.name
        }
    }
}
