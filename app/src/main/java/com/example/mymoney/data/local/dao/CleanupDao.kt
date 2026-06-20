package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CleanupDao {

    @Transaction
    suspend fun hardDeleteAllDeleted(
        userId: String,
        transactionDao: TransactionDao,
        budgetDao: BudgetDao,
        savingRecordDao: SavingRecordDao,
        walletDao: WalletDao,
        categoryDao: CategoryDao,
        savingDao: SavingDao
    ) {
        // Thứ tự xóa: Con trước, Cha sau (do FK RESTRICT)
        transactionDao.hardDeleteDeletedItems(userId)
        budgetDao.hardDeleteDeletedItems(userId)
        savingRecordDao.hardDeleteDeletedItems(userId)

        walletDao.hardDeleteDeletedItems(userId)
        categoryDao.hardDeleteDeletedItems(userId)
        savingDao.hardDeleteDeletedItems(userId)
    }
}
