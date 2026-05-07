package com.example.mymoney.data.repository

import com.example.mymoney.data.local.dao.WalletDao
import com.example.mymoney.data.local.entity.SyncStatus
import com.example.mymoney.data.local.entity.WalletEntity
import com.example.mymoney.domain.model.WalletModel
import com.example.mymoney.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WalletRepositoryImpl(
    private val walletDao: WalletDao
) : WalletRepository {

    override fun getWallets(userId: String): Flow<List<WalletModel>> =
        walletDao.observeWallets(userId).map { list -> list.map { it.toModel() } }

    override fun getTotalBalance(userId: String): Flow<Double> =
        walletDao.observeWallets(userId).map { list -> list.sumOf { it.balance } }

    override suspend fun getWalletById(walletId: Long): WalletModel? =
        walletDao.getWalletById(walletId)?.toModel()

    override suspend fun getDefaultWallet(userId: String): WalletModel? =
        walletDao.getDefaultWallet(userId)?.toModel()

    override suspend fun addWallet(wallet: WalletModel): Long {
        val now = System.currentTimeMillis()

        if (wallet.isDefault) {
            walletDao.clearDefaultWallets(wallet.userId, now)
        }
        return walletDao.insert(wallet.toEntity().copy(updatedAt = now, syncStatus = SyncStatus.PENDING_INSERT))
    }

    override suspend fun updateWallet(wallet: WalletModel) {
        val now = System.currentTimeMillis()

        /** Nếu ví này được set mặc định → bỏ mặc định toàn bộ ví khác **/
        if (wallet.isDefault) {
            walletDao.clearDefaultWallets(wallet.userId, now)

            walletDao.update(
                wallet.toEntity().copy(
                    updatedAt  = now,
                    syncStatus = SyncStatus.PENDING_UPDATE
                )
            )
            return
        }

        /** Nếu ví tắt mặc định -> tìm ví khác để set mặc định **/
        val current = walletDao.getWalletById(wallet.id)
        if (current?.isDefault == true && !wallet.isDefault) {
            // Update ví hiện tại thành false trước
            walletDao.update(
                wallet.toEntity().copy(
                    updatedAt = now,
                    syncStatus = SyncStatus.PENDING_UPDATE
                )
            )

            // Tìm ví khác để auto set mặc định
            val anotherWallet = walletDao.getAnotherWallet(
                userId = wallet.userId,
                excludeId = wallet.id
            )

            if (anotherWallet != null) {
                walletDao.update(
                    anotherWallet.copy(
                        isDefault = true,
                        updatedAt = now,
                        syncStatus = SyncStatus.PENDING_UPDATE
                    )
                )
            }

            return
        }

        walletDao.update(
            wallet.toEntity().copy(
                updatedAt = now,
                syncStatus = SyncStatus.PENDING_UPDATE
            )
        )
    }

    override suspend fun updateWalletBalance(walletId: Long, newBalance: Double) {
        val entity = walletDao.getWalletById(walletId) ?: return
        walletDao.update(
            entity.copy(
                balance = newBalance,
                updatedAt = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_UPDATE
            )
        )
    }

    override suspend fun deleteWallet(id: Long) {
        val wallet = walletDao.getWalletById(id) ?: return
        walletDao.softDelete(id)

        /** Nếu xóa ví mặc định → chọn ví khác làm mặc định**/
        if (wallet.isDefault) {
            val anotherWallet = walletDao.getAnotherWallet(
                userId = wallet.userId,
                excludeId = wallet.id
            )

            if (anotherWallet != null) {
                walletDao.update(
                    anotherWallet.copy(
                        isDefault = true,
                        updatedAt = System.currentTimeMillis(),
                        syncStatus = SyncStatus.PENDING_UPDATE
                    )
                )
            }
        }
    }

    override suspend fun updateSortOrders(orders: List<Pair<Long, Int>>) {
        orders.forEach { (id, sortOrder) -> walletDao.updateSortOrder(id, sortOrder) }
    }

    // ── Mappers ──

    private fun WalletEntity.toModel() = WalletModel(
        id         = id,
        userId     = userId,
        name       = name,
        balance    = balance,
        icon       = icon,
        color      = color,
        isDefault  = isDefault,
        isArchived = isDeleted,
        createdAt  = createdAt,
        updatedAt  = updatedAt,
        supabaseId = supabaseId
    )

    private fun WalletModel.toEntity() = WalletEntity(
        id          = id,
        supabaseId  = supabaseId,
        userId      = userId,
        name        = name,
        balance     = balance,
        icon        = icon,
        color       = color,
        isDefault   = isDefault,
        createdAt   = createdAt,
        updatedAt   = updatedAt,
        isDeleted   = isArchived,
        syncStatus  = SyncStatus.PENDING_INSERT
    )
}

