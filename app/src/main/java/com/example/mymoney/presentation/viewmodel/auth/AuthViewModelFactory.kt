package com.example.mymoney.presentation.viewmodel.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.repository.AuthRepositoryImpl

/**
 * Factory inject dependency chain:
 *   AuthRepositoryImpl + SettingPreferences → AuthViewModel
 *
 * AuthViewModel không cần AppDatabase nên chain đơn giản hơn.
 *
 * @param context  ApplicationContext (hoặc Activity context)
 */
class AuthViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            "Unknown ViewModel: ${modelClass.name}"
        }
        return AuthViewModel(
            authRepository     = AuthRepositoryImpl(),
            settingPreferences = SettingPreferences(context.applicationContext)
        ) as T
    }
}

