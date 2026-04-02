package com.example.mymoney.data.repository

import com.example.mymoney.data.local.dao.WalletDao
import com.example.mymoney.data.local.entity.WalletEntity
import com.example.mymoney.domain.model.WalletModel
import com.example.mymoney.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WalletRepositoryImpl(
    private val dao: WalletDao
) : WalletRepository {

    override fun getWallets(userId: String): Flow<List<WalletModel>> =
        dao.getWallets(userId).map { it.map { e -> e.toDomain() } }

    override fun getTotalBalance(userId: String): Flow<Double> =
        dao.getTotalBalance(userId)

    override suspend fun getDefaultWallet(userId: String): WalletModel? =
        dao.getDefaultWallet(userId)?.toDomain()

    override suspend fun addWallet(wallet: WalletModel): Long =
        dao.insertWallet(WalletEntity.fromDomain(wallet))

    override suspend fun updateWalletBalance(walletId: Long, newBalance: Double) =
        dao.updateBalance(walletId, newBalance)

    override suspend fun deleteWallet(id: Long) =
        dao.deleteWalletById(id)
}
