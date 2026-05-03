package com.example.mymoney.domain.repository

import com.example.mymoney.domain.model.WalletModel
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getWallets(userId: String): Flow<List<WalletModel>>
    fun getTotalBalance(userId: String): Flow<Double>
    suspend fun getWalletById(walletId: Long): WalletModel?
    suspend fun getDefaultWallet(userId: String): WalletModel?
    suspend fun addWallet(wallet: WalletModel): Long
    suspend fun updateWallet(wallet: WalletModel)
    suspend fun updateWalletBalance(walletId: Long, newBalance: Double)
    suspend fun updateSortOrders(orders: List<Pair<Long, Int>>)
    suspend fun deleteWallet(id: Long)
}
