package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.repository.SavingRecordRepository
import com.example.mymoney.domain.repository.WalletRepository

class DeleteSavingRecordUseCase(
    private val savingRecordRepository: SavingRecordRepository,
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(recordId: Long) {
        val record = savingRecordRepository.getRecordById(recordId)
            ?: throw IllegalArgumentException("Không tìm thấy bản ghi tiết kiệm")

        // Hoàn lại tiền vào ví
        val wallet = walletRepository.getWalletById(record.walletId)
        if (wallet != null) {
            walletRepository.updateWalletBalance(
                walletId = wallet.id,
                newBalance = wallet.balance + record.amount
            )
        }

        // Xóa bản ghi (soft delete)
        savingRecordRepository.deleteRecord(recordId)
    }
}