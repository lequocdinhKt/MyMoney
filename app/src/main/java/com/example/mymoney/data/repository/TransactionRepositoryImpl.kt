package com.example.mymoney.data.repository

import com.example.mymoney.data.local.dao.TransactionDao
import com.example.mymoney.data.local.entity.SyncStatus
import com.example.mymoney.data.local.entity.TransactionEntity
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository có thể dùng như Singleton — không giữ userId trong constructor.
 * userId được truyền qua từng phương thức để an toàn khi đổi tài khoản.
 */
class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(userId: String): Flow<List<TransactionModel>> =
        transactionDao.observeTransactions(userId).map { list -> list.map { it.toModel() } }

    override fun getTransactionsByPeriod(userId: String, from: Long, to: Long): Flow<List<TransactionModel>> =
        transactionDao.observeByDateRange(userId, from, to).map { list -> list.map { it.toModel() } }

    override fun getTransactionsByWalletAndPeriod(userId: String, walletId: Long, from: Long, to: Long): Flow<List<TransactionModel>> =
        transactionDao.observeByWalletAndDateRange(userId, walletId, from, to).map { list -> list.map { it.toModel() } }

    override fun getTotalIncome(userId: String, from: Long, to: Long): Flow<Double> =
        transactionDao.observeByDateRange(userId, from, to).map { list ->
            list.filter { it.type == "income" }.sumOf { it.amount }
        }

    override fun getTotalExpense(userId: String, from: Long, to: Long): Flow<Double> =
        transactionDao.observeByDateRange(userId, from, to).map { list ->
            list.filter { it.type == "expense" }.sumOf { it.amount }
        }

    override fun getTotalIncomeByWallet(userId: String, walletId: Long, from: Long, to: Long): Flow<Double> =
        transactionDao.observeByWalletAndDateRange(userId, walletId, from, to).map { list ->
            list.filter { it.type == "income" }.sumOf { it.amount }
        }

    override fun getTotalExpenseByWallet(userId: String, walletId: Long, from: Long, to: Long): Flow<Double> =
        transactionDao.observeByWalletAndDateRange(userId, walletId, from, to).map { list ->
            list.filter { it.type == "expense" }.sumOf { it.amount }
        }

    override suspend fun addTransaction(transaction: TransactionModel) {
        transactionDao.insert(transaction.toEntity())
    }

    override suspend fun getTransactionById(id: Long): TransactionModel? =
        transactionDao.getTransactionById(id)?.toModel()

    override suspend fun deleteTransaction(id: Long) =
        transactionDao.softDelete(id)

    // ── Mappers ──

    private fun TransactionEntity.toModel() = TransactionModel(
        id           = id,
        userId       = userId,
        note         = note,
        amount       = amount,
        type         = type,
        category     = categoryName,
        categoryId   = categoryId,
        walletId     = walletId,
        aiGenerated  = aiGenerated,
        timestamp    = transactionDate,
        supabaseId   = supabaseId,
        imagePath    = imagePath
    )

    private fun TransactionModel.toEntity(): TransactionEntity {
        val now = System.currentTimeMillis()
        return TransactionEntity(
            id              = id,
            supabaseId      = supabaseId,
            userId          = userId,
            walletId        = walletId,
            categoryId      = categoryId,
            categoryName    = category,
            amount          = amount,
            type            = type,
            note            = note,
            transactionDate = timestamp,
            aiGenerated     = aiGenerated,
            createdAt       = if (id == 0L) now else timestamp,
            updatedAt       = now,
            isDeleted       = false,
            syncStatus      = SyncStatus.PENDING_INSERT,
            imagePath       = imagePath
        )
    }
}

