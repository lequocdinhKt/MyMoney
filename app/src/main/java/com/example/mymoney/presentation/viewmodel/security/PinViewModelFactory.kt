package com.example.mymoney.presentation.viewmodel.security

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.repository.AuthRepositoryImpl

class PinViewModelFactory(
    private val context: Context,
    private val isSetupFlow: Boolean = false
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PinViewModel::class.java)) {
            "Unknown ViewModel: ${modelClass.name}"
        }
        return PinViewModel(
            settingPreferences = SettingPreferences(context.applicationContext),
            authRepository = AuthRepositoryImpl(),
            isSetupFlow = isSetupFlow
        ) as T
    }
}
