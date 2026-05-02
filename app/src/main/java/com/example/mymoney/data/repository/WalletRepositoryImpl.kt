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

    override suspend fun getDefaultWallet(userId: String): WalletModel? =
        walletDao.getDefaultWallet(userId)?.toModel()

    override suspend fun addWallet(wallet: WalletModel): Long =
        walletDao.insert(wallet.toEntity())

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

    override suspend fun deleteWallet(id: Long) =
        walletDao.softDelete(id)

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

