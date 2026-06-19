package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.repository.SavingRecordRepository
import com.example.mymoney.domain.repository.SavingRepository
import com.example.mymoney.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first

class DeleteSavingGoalUseCase (
    private val goalRepository: SavingRepository,
    private val recordRepository: SavingRecordRepository,
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(goalId: Long) {
        // 1. Lấy tất cả các bản ghi tiết kiệm của mục tiêu này trước khi xóa
        // Sử dụng .first() để lấy danh sách hiện tại từ Flow
        val records = recordRepository.getRecordsByGoalId(goalId).first()

        // 2. Hoàn lại tiền vào các ví tương ứng
        records.forEach { record ->
            val wallet = walletRepository.getWalletById(record.walletId)
            if (wallet != null) {
                val newBalance = wallet.balance + record.amount
                walletRepository.updateWalletBalance(wallet.id, newBalance)
            }
        }

        // 3. Xóa tất cả bản ghi tiết kiệm liên quan đến mục tiêu này
        recordRepository.deleteRecordsByGoalId(goalId)
        // 4. Cuối cùng mới xóa mục tiêu tiết kiệm
        goalRepository.deleteSavingGoal(goalId)
    }
}