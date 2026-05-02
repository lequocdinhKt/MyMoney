package com.example.mymoney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mymoney.data.local.entity.SyncStatus
import com.example.mymoney.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets WHERE user_id = :userId AND is_deleted = 0 ORDER BY is_default DESC, name ASC")
    fun observeWallets(userId: String): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id AND is_deleted = 0")
    suspend fun getWalletById(id: Long): WalletEntity?

    @Query("SELECT * FROM wallets WHERE user_id = :userId AND is_default = 1 AND is_deleted = 0 LIMIT 1")
    suspend fun getDefaultWallet(userId: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallet: WalletEntity): Long

    @Update
    suspend fun update(wallet: WalletEntity)

    /** Soft-delete: đánh dấu is_deleted = 1, sync_status = PENDING_DELETE */
    @Query("UPDATE wallets SET is_deleted = 1, sync_status = ${SyncStatus.PENDING_DELETE}, updated_at = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    /** Trả về tất cả wallets của user (kể cả đã sync) */
    @Query("SELECT * FROM wallets WHERE user_id = :userId AND is_deleted = 0")
    suspend fun getWalletsByUser(userId: String): List<WalletEntity>

    /** Trả về tất cả wallets chưa được sync lên Supabase */
    @Query("SELECT * FROM wallets WHERE user_id = :userId AND sync_status != ${SyncStatus.SYNCED}")
    suspend fun getPendingSync(userId: String): List<WalletEntity>

    /** Đánh dấu đã sync xong sau khi upsert Supabase thành công */
    @Query("UPDATE wallets SET sync_status = ${SyncStatus.SYNCED}, supabase_id = :supabaseId WHERE id = :localId")
    suspend fun markSynced(localId: Long, supabaseId: String)
}


