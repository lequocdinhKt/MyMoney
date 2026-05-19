package com.example.mymoney.presentation.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.domain.repository.AuthRepository

/**
 * Factory tạo [ProfileViewModel] với dependencies thủ công (không Hilt).
 *
 * @param authRepository     Repository xử lý xác thực người dùng
 * @param settingPreferences DataStore lưu thông tin cài đặt/local user
 */
class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val settingPreferences: SettingPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository, settingPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
