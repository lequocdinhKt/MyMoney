package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.SavingRecordModel
import com.example.mymoney.domain.repository.SavingRecordRepository
import com.example.mymoney.domain.repository.SavingRepository


class AddSavingRecordUseCase (
    private val recordRepository: SavingRecordRepository
) {
    suspend operator fun invoke(record: SavingRecordModel) {
        require(record.amount > 0) {
            "Số tiền phải lớn hơn 0"
        }
        recordRepository.addRecord(record)
    }
}