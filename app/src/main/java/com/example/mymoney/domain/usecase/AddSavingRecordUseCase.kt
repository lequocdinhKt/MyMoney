package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.SavingRecordModel
import com.example.mymoney.domain.repository.SavingRecordRepository
import com.example.mymoney.domain.repository.WalletRepository


class AddSavingRecordUseCase (
    private val walletRepository: WalletRepository,
    private val savingRecordRepository: SavingRecordRepository
) {
    suspend operator fun invoke(
        userId: String,
        goalId: Long,
        walletId: Long,
        amount: Double,
        note: String?
    ) {
        require(amount > 0) {
            "Số tiền phải lớn hơn 0"
        }

        val wallet = walletRepository.getWalletById(walletId)
            ?: throw IllegalArgumentException("Không tìm thấy ví")

        require(wallet.balance >= amount) {
            "Số dư ví không đủ"
        }

        // Trừ tiền ví
        walletRepository.updateWalletBalance(
            walletId = wallet.id,
            newBalance = wallet.balance - amount
        )

        // Tạo bản ghi tiết kiệm
        savingRecordRepository.addRecord(
            SavingRecordModel(
                userId = userId,
                savingGoalId = goalId,
                walletId = walletId,
                walletName = wallet.name,
                amount = amount,
                note = note,
                recordDate = System.currentTimeMillis()
            )
        )
    }
}