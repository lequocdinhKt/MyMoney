package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case: Lấy danh sách tất cả giao dịch.
 * Đóng gói 1 thao tác nghiệp vụ duy nhất — dễ test bằng fake repository.
 *
 * @param repository Repository interface (injected từ Data layer)
 */
class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    /**
     * Trả về Flow chứa danh sách giao dịch sắp xếp theo thời gian mới nhất.
     * Mỗi khi dữ liệu Room thay đổi, Flow sẽ tự động phát giá trị mới.
     */
    operator fun invoke(): Flow<List<TransactionModel>> =
        repository.getAllTransactions()
}
