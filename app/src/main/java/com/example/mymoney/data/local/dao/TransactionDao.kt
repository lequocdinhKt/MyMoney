package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mymoney.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho bảng "transactions".
 * Cung cấp các phương thức CRUD — Room tự generate implementation.
 *
 * Quy tắc:
 *   - Flow cho query đọc (tự động phát khi dữ liệu thay đổi)
 *   - suspend cho write operations
 */
@Dao
interface TransactionDao {

    /**
     * Lấy tất cả giao dịch, sắp xếp theo thời gian mới nhất trước (DESC).
     * Trả về Flow — UI tự động cập nhật khi có thay đổi.
     */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Thêm giao dịch mới.
     * REPLACE nếu trùng primary key (trường hợp hiếm khi xảy ra với autoGenerate).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: TransactionEntity)

    /**
     * Xoá giao dịch theo id.
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}
