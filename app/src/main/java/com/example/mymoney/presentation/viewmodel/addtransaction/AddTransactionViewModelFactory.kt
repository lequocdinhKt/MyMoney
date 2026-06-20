package com.example.mymoney.presentation.viewmodel.addtransaction

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.AIParsingRepositoryImpl
import com.example.mymoney.data.repository.ChatRepositoryImpl
import com.example.mymoney.data.repository.SavingRecordRepositoryImpl
import com.example.mymoney.data.repository.SavingRepositoryImpl
import com.example.mymoney.data.repository.TransactionRepositoryImpl
import com.example.mymoney.data.repository.WalletRepositoryImpl
import com.example.mymoney.domain.usecase.AddSavingRecordUseCase
import com.example.mymoney.domain.usecase.AddTransactionUseCase
import com.example.mymoney.domain.usecase.EnsureDefaultWalletUseCase
import com.example.mymoney.domain.usecase.GetTransactionsUseCase
import com.example.mymoney.domain.usecase.ParseTransactionMessageUseCase
import com.example.mymoney.domain.usecase.TranscribeVoiceUseCase

/**
 * Factory inject toàn bộ dependency chain:
 *   AppDatabase → DAO → RepositoryImpl → UseCase → AddTransactionViewModel
 *
 * @param context    ApplicationContext (hoặc Activity context — sẽ lấy applicationContext)
 * @param walletId   ID ví đang được chọn trên HomeScreen (0L = fallback về ví mặc định)
 */
class AddTransactionViewModelFactory(
    private val context: Context,
    private val walletId: Long = 0L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
            "Unknown ViewModel: ${modelClass.name}"
        }
        val appCtx    = context.applicationContext
        val db        = AppDatabase.getInstance(appCtx)
        val txRepo    = TransactionRepositoryImpl(db.transactionDao())
        val walletRepo = WalletRepositoryImpl(db.walletDao())
        val savingRepo = SavingRepositoryImpl(db.savingDao())
        val savingRecordRepo = SavingRecordRepositoryImpl(db.savingRecordDao())
        val aiParsingRepo = AIParsingRepositoryImpl()

        return AddTransactionViewModel(
            getTransactionsUseCase  = GetTransactionsUseCase(txRepo),
            addTransactionUseCase   = AddTransactionUseCase(txRepo),
            walletRepository        = walletRepo,
            ensureDefaultWallet     = EnsureDefaultWalletUseCase(walletRepo),
            chatRepository          = ChatRepositoryImpl(db.chatMessageDao()),
            savingRepository        = savingRepo,
            addSavingRecordUseCase  = AddSavingRecordUseCase(walletRepo, savingRecordRepo),
            parseTransactionMessageUseCase = ParseTransactionMessageUseCase(aiParsingRepo),
            transcribeVoiceUseCase = TranscribeVoiceUseCase(aiParsingRepo),
            db                      = db,
            settingPreferences      = SettingPreferences(appCtx),
            categoryDao             = db.categoryDao(),
            selectedWalletId        = walletId,
            appContext               = appCtx
        ) as T
    }
}

