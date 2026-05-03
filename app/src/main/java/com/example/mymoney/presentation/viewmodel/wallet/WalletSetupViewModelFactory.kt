package com.example.mymoney.presentation.viewmodel.wallet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.TransactionRepositoryImpl
import com.example.mymoney.data.repository.WalletRepositoryImpl
import com.example.mymoney.domain.usecase.AddTransactionUseCase

/**
 * Factory tạo [WalletSetupViewModel] với dependencies thủ công (không Hilt).
 *
 * @param context   ApplicationContext
 * @param userId    ID người dùng hiện tại
 * @param walletId  null = thêm mới, khác null = chỉnh sửa ví có id này
 */
class WalletSetupViewModelFactory(
    private val context: Context,
    private val userId: String,
    private val walletId: Long? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(WalletSetupViewModel::class.java))
        val db   = AppDatabase.getInstance(context.applicationContext)
        val repo = WalletRepositoryImpl(db.walletDao())
        return WalletSetupViewModel(
            walletRepository     = repo,
            addTransactionUseCase = AddTransactionUseCase(TransactionRepositoryImpl(db.transactionDao())),
            userId               = userId,
            walletId             = walletId
        ) as T
    }
}

