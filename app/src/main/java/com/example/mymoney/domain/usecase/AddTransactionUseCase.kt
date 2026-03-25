package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.repository.TransactionRepository

/**
 * Use Case: Thêm một giao dịch mới.
 * Có thể mở rộng thêm validation (kiểm tra số tiền > 0, ghi chú không rỗng, v.v.)
 *
 * @param repository Repository interface (injected từ Data layer)
 */
class AddTransactionUseCase(
    private val repository: TransactionRepository
) {
    /**
     * Thêm giao dịch vào cơ sở dữ liệu.
     *
     * @param transaction Giao dịch cần thêm
     * @throws IllegalArgumentException nếu ghi chú rỗng
     */
    suspend operator fun invoke(transaction: TransactionModel) {
        require(transaction.note.isNotBlank()) { "Ghi chú giao dịch không được để trống" }
        repository.addTransaction(transaction)
    }
}
