package com.example.mymoney.data.repository

import com.example.mymoney.data.local.dao.TransactionDao
import com.example.mymoney.data.local.entity.TransactionEntity
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation của [TransactionRepository] — thuộc Data layer.
 *
 * Sử dụng [TransactionDao] để truy cập Room database.
 * Ánh xạ Entity ↔ Domain model bằng mapper functions.
 *
 * @param dao DAO để truy vấn bảng transactions
 */
class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    /**
     * Lấy tất cả giao dịch — map Entity → Domain model.
     * Flow tự động phát giá trị mới khi dữ liệu Room thay đổi.
     */
    override fun getAllTransactions(): Flow<List<TransactionModel>> =
        dao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Lấy giao dịch theo khoảng thời gian — map Entity → Domain model.
     */
    override fun getTransactionsByPeriod(from: Long, to: Long): Flow<List<TransactionModel>> =
        dao.getTransactionsByPeriod(from, to).map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Lấy tổng thu nhập theo khoảng thời gian.
     */
    override fun getTotalIncome(from: Long, to: Long): Flow<Double> =
        dao.getTotalIncome(from, to)

    /**
     * Lấy tổng chi phí theo khoảng thời gian.
     */
    override fun getTotalExpense(from: Long, to: Long): Flow<Double> =
        dao.getTotalExpense(from, to)

    /**
     * Thêm giao dịch — map Domain model → Entity rồi insert.
     */
    override suspend fun addTransaction(transaction: TransactionModel) {
        dao.insertTransaction(TransactionEntity.fromDomain(transaction))
    }

    /**
     * Xoá giao dịch theo id.
     */
    override suspend fun deleteTransaction(id: Long) {
        dao.deleteTransactionById(id)
    }
}
