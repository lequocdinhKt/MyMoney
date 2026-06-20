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

    @Query("SELECT * FROM wallets WHERE user_id = :userId AND is_deleted = 0 ORDER BY sort_order ASC, is_default DESC, name ASC")
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

    /** Cập nhật sort_order để duy trì thứ tự sau drag & drop */
    @Query("UPDATE wallets SET sort_order = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    /** Đánh dấu đã sync xong sau khi upsert Supabase thành công */
    @Query("UPDATE wallets SET sync_status = ${SyncStatus.SYNCED}, supabase_id = :supabaseId WHERE id = :localId")
    suspend fun markSynced(localId: Long, supabaseId: String)

    @Query("DELETE FROM wallets WHERE user_id = :userId AND is_deleted = 1")
    suspend fun hardDeleteDeletedItems(userId: String)


    /** Khi 1 ví được set mặc định -> bỏ mặc định toàn bộ ví khác */
    @Query("UPDATE wallets SET is_default = 0, updated_at = :now, sync_status = ${SyncStatus.PENDING_UPDATE} WHERE user_id = :userId")
    suspend fun clearDefaultWallets(userId: String, now: Long = System.currentTimeMillis())

    /** Khi xóa ví mặc định → tìm ví khác để set mặc định */
    @Query("SELECT * FROM wallets WHERE user_id = :userId AND is_deleted = 0 AND id != :excludeId ORDER BY sort_order ASC, created_at ASC LIMIT 1")
    suspend fun getAnotherWallet(userId: String, excludeId: Long): WalletEntity?
}


