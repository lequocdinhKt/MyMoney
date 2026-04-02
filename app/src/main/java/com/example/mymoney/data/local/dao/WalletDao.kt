package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mymoney.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    /** Lấy tất cả ví của user, ví mặc định lên đầu */
    @Query("SELECT * FROM wallets WHERE userId = :userId AND isArchived = 0 ORDER BY isDefault DESC, createdAt ASC")
    fun getWallets(userId: String): Flow<List<WalletEntity>>

    /** Lấy ví mặc định của user */
    @Query("SELECT * FROM wallets WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultWallet(userId: String): WalletEntity?

    /** Lấy ví theo id */
    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getWalletById(id: Long): WalletEntity?

    /** Tổng số dư tất cả ví của user */
    @Query("SELECT COALESCE(SUM(balance), 0.0) FROM wallets WHERE userId = :userId AND isArchived = 0")
    fun getTotalBalance(userId: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(entity: WalletEntity): Long

    @Update
    suspend fun updateWallet(entity: WalletEntity)

    /** Cập nhật số dư ví */
    @Query("UPDATE wallets SET balance = :newBalance, updatedAt = :now WHERE id = :walletId")
    suspend fun updateBalance(walletId: Long, newBalance: Double, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteWalletById(id: Long)
}
