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
 * @param context ApplicationContext
 */
class SettingPreferences(private val context: Context) {
    private companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_USER_ID              = stringPreferencesKey("user_id")
        val KEY_USERNAME             = stringPreferencesKey("username")
        val KEY_THOUSAND_SEPARATOR   = booleanPreferencesKey("thousand_separator_enabled")
        val KEY_THEME_MODE           = stringPreferencesKey("THEME_MODE")
        val KEY_NUMBER_FORMAT        = stringPreferencesKey("number_format")
    }
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_ONBOARDING_COMPLETED] ?: false }
    val currentUserId: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[KEY_USER_ID] }
    val currentUsername: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[KEY_USERNAME] }
    /** Mặc định bật phân tách hàng nghìn */
    val isThousandSeparatorEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_THOUSAND_SEPARATOR] ?: true }
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { prefs ->
            val name = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
            try { ThemeMode.valueOf(name) } catch (_: Exception) { ThemeMode.SYSTEM }
        }
    /** Định dạng số, mặc định DOT (1.000.000 kiểu VN) */
    val numberFormat: Flow<NumberFormat> = context.dataStore.data
        .map { prefs ->
            val name = prefs[KEY_NUMBER_FORMAT] ?: NumberFormat.DOT.name
            try { NumberFormat.valueOf(name) } catch (_: Exception) { NumberFormat.DOT }
        }
    suspend fun saveOnboardingCompleted() {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = true }
    }
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { it[KEY_USER_ID] = userId }
    }
    suspend fun saveUsername(username: String) {
        context.dataStore.edit { it[KEY_USERNAME] = username }
    }
    suspend fun setThousandSeparatorEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_THOUSAND_SEPARATOR] = enabled }
    }
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }
    suspend fun setNumberFormat(format: NumberFormat) {
        context.dataStore.edit { it[KEY_NUMBER_FORMAT] = format.name }
    }
    suspend fun clearUserId() {
        context.dataStore.edit { it.remove(KEY_USER_ID) }
    }
    suspend fun clearUsername() {
        context.dataStore.edit { it.remove(KEY_USERNAME) }
    }
}
