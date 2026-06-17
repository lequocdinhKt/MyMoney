package com.example.mymoney.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mymoney.presentation.viewmodel.setting.setting.NumberFormat
import com.example.mymoney.presentation.viewmodel.setting.setting.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Singleton DataStore per-Context theo khuyến nghị của Jetpack
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "setting_prefs")

/**
 * Quản lý toàn bộ cài đặt người dùng bằng DataStore Preferences.
 *
 * Dữ liệu được lưu vào bộ nhớ trong app, không mất khi tắt ứng dụng,
 * chỉ bị xóa khi gỡ cài đặt hoặc xóa dữ liệu ứng dụng.
 *
 * @param context ApplicationContext
 */
class SettingPreferences(private val context: Context) {

    // ── Keys ──────────────────────────────────────────────────────────────────
    private companion object {
        val KEY_ONBOARDING_COMPLETED     = booleanPreferencesKey("onboarding_completed")
        val KEY_USER_ID                  = stringPreferencesKey("user_id")
        val KEY_USERNAME                 = stringPreferencesKey("username")
        val KEY_THOUSAND_SEPARATOR       = booleanPreferencesKey("thousand_separator_enabled")
        val KEY_THEME_MODE               = stringPreferencesKey("theme_mode")
        val KEY_NUMBER_FORMAT            = stringPreferencesKey("number_format")
        val KEY_SHOW_COMPLETED           = booleanPreferencesKey("show_completed_enabled")
        val KEY_CHAT_TONE                 = stringPreferencesKey("chat_tone")
        val KEY_AI_CUSTOM_RULES          = stringPreferencesKey("ai_custom_rules")
        val KEY_PIN_CODE                  = stringPreferencesKey("pin_code")
        val KEY_BIOMETRIC_ENABLED         = booleanPreferencesKey("biometric_enabled")
    }

    // ── Read Flows ─────────────────────────────────────────────────────────────

    /** false = chưa xem onboarding (bao gồm lần đầu cài app) */
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_ONBOARDING_COMPLETED] ?: false }

    /** null = chưa đăng nhập */
    val currentUserId: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[KEY_USER_ID] }

    val currentUsername: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[KEY_USERNAME] }

    /** Mặc định bật phân tách hàng nghìn */
    val isThousandSeparatorEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_THOUSAND_SEPARATOR] ?: true }

    /** Lấy ThemeMode hiện tại, mặc định là SYSTEM */
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { prefs ->
            val themeName = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(themeName)
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            }
        }

    val numberFormat: Flow<NumberFormat> = context.dataStore.data
        .map { prefs ->
            val formatName = prefs[KEY_NUMBER_FORMAT] ?: NumberFormat.DOT.name
            try {
                NumberFormat.valueOf(formatName)
            } catch (_: Exception) {
                NumberFormat.DOT
            }
        }

    /** Mặc định hiện mục tiêu đã hoàn thành */
    val isShowCompletedEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_SHOW_COMPLETED] ?: true }

    /** Chat tone: FRIENDLY or STERN, default FRIENDLY */
    val chatTone: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_CHAT_TONE] ?: "FRIENDLY" }

    /** Custom rules for AI parsing */
    val aiCustomRules: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_AI_CUSTOM_RULES] ?: "" }

    /** PIN Code - null nếu chưa thiết lập */
    val pinCode: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[KEY_PIN_CODE] }

    /** Biometric enabled status */
    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_BIOMETRIC_ENABLED] ?: false }

    // ── Write ──────────────────────────────────────────────────────────────────

    suspend fun saveOnboardingCompleted() {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
        }
    }

    suspend fun setThousandSeparatorEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THOUSAND_SEPARATOR] = enabled
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    @Suppress("unused")
    suspend fun setNumberFormat(format: NumberFormat) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NUMBER_FORMAT] = format.name
        }
    }

    suspend fun setShowCompletedEnabled(show_enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_COMPLETED] = show_enabled
        }
    }

    suspend fun setChatTone(toneName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CHAT_TONE] = toneName
        }
    }

    suspend fun setAiCustomRules(rules: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AI_CUSTOM_RULES] = rules
        }
    }

    suspend fun savePinCode(pin: String?) {
        context.dataStore.edit { prefs ->
            if (pin == null) prefs.remove(KEY_PIN_CODE)
            else prefs[KEY_PIN_CODE] = pin
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun clearUserId() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
        }
    }

    suspend fun clearUsername() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USERNAME)
        }
    }
}
