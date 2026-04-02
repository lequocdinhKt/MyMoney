package com.example.mymoney.domain.repository

import com.example.mymoney.domain.model.WalletModel
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getWallets(userId: String): Flow<List<WalletModel>>
    fun getTotalBalance(userId: String): Flow<Double>
    suspend fun getDefaultWallet(userId: String): WalletModel?
    suspend fun addWallet(wallet: WalletModel): Long
    suspend fun updateWalletBalance(walletId: Long, newBalance: Double)
    suspend fun deleteWallet(id: Long)
}
