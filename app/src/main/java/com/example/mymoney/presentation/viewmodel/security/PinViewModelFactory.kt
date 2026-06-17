package com.example.mymoney.presentation.viewmodel.security

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.datastore.SettingPreferences

class PinViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PinViewModel::class.java)) {
            "Unknown ViewModel: ${modelClass.name}"
        }
        return PinViewModel(
            settingPreferences = SettingPreferences(context.applicationContext)
        ) as T
    }
}
