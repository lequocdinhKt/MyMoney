package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.WalletModel
import com.example.mymoney.domain.repository.WalletRepository

/**
 * Đảm bảo user luôn có ít nhất 1 ví mặc định.
 * Gọi sau khi đăng nhập hoặc trước khi thêm giao dịch.
 * @return WalletModel của ví mặc định (tạo mới hoặc ví cũ đã tồn tại)
 */
class EnsureDefaultWalletUseCase(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(userId: String): WalletModel {
        // Nếu đã có ví mặc định → trả về luôn
        val existing = walletRepository.getDefaultWallet(userId)
        if (existing != null) return existing

        // Chưa có → tạo ví mặc định
        val newWallet = WalletModel(
            userId    = userId,
            name      = "Ví chính",
            balance   = 0.0,
            icon      = "wallet",
            color     = "#0088F0",
            isDefault = true
        )
        val id = walletRepository.addWallet(newWallet)
        return newWallet.copy(id = id)
    }
}
