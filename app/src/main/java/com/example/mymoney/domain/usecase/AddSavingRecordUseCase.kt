package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.SavingRecordModel
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.repository.SavingRecordRepository
import com.example.mymoney.domain.repository.SavingRepository
import com.example.mymoney.domain.repository.TransactionRepository
import com.example.mymoney.domain.repository.WalletRepository


class AddSavingRecordUseCase (
    private val walletRepository: WalletRepository,
    private val savingRecordRepository: SavingRecordRepository,
    private val savingRepository: SavingRepository,
    private val transactionRepository: TransactionRepository
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

        // Lấy thông tin mục tiêu để đặt tiêu đề giao dịch
        val goal = savingRepository.getSavingGoalById(goalId)
        val goalName = goal?.name ?: "Mục tiêu"

        // Tạo bản ghi tiết kiệm
        savingRecordRepository.addRecord(
            SavingRecordModel(
                userId = userId,
                savingGoalId = goalId,
                amount = amount,
                note = note,
                recordDate = System.currentTimeMillis()
            )
        )

        // Tạo bản ghi giao dịch (chi tiêu từ ví)
        transactionRepository.addTransaction(
            TransactionModel(
                userId = userId,
                note = "Tiết kiệm cho $goalName",
                amount = amount,
                type = "expense",
                category = "Tiết kiệm",
                categoryIcon = "🐷",
                walletId = walletId,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}