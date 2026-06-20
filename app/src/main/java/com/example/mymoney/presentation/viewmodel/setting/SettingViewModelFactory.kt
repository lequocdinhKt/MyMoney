package com.example.mymoney.presentation.viewmodel.setting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.AuthRepositoryImpl

/**
 * Factory inject toàn bộ dependency chain:
 *   AppDatabase → DAO → RepositoryImpl → SettingViewModel
 *
 * @param context  ApplicationContext (hoặc Activity context)
 * @param userId   ID người dùng hiện tại — dùng để filter Room queries theo user
 */
class SettingViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            "Unknown ViewModel: ${modelClass.name}"
        }
        val appCtx = context.applicationContext
        val db     = AppDatabase.getInstance(appCtx)

        return SettingViewModel(
            settingPreferences      = SettingPreferences(appCtx),
            authRepository          = AuthRepositoryImpl(),
            db                      = db
        ) as T
    }
}

