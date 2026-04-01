package com.example.mymoney.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
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

        // Key lưu dấu phân cách hàng nghìn đã bật chưa
        private val KEY_IS_THOUSAND_SEPARATOR_ENABLED =
            booleanPreferencesKey("IS_THOUSAND_SEPERATOR_ENABLED")
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
     * Flow phát ra true nếu dấu phân cách hàng nghìn được bật, false nếu chưa
     * Mặc định là true
     */
    val isThousandSeparatorEnabled: Flow<Boolean> = context.dataStore.data
        .map {preferences ->
            preferences[KEY_IS_THOUSAND_SEPARATOR_ENABLED] ?: true
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

    suspend fun setThousandSeparatorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_THOUSAND_SEPARATOR_ENABLED] = enabled
        }
    }
}
