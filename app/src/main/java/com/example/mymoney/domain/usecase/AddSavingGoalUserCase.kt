package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.SavingGoalModel
import com.example.mymoney.domain.model.SavingType
import com.example.mymoney.domain.repository.SavingRepository

class AddSavingGoalUserCase (
    private val repository: SavingRepository
) {
    suspend operator fun invoke(savingGoal: SavingGoalModel) {
        require(savingGoal.name.isNotBlank()) {"Tiêu đề không được để trống"}
        require(savingGoal.targetAmount > 0) {"Mục tiêu phải lớn hơn 0"}
        if(savingGoal.savingType == SavingType.ONE_TIME) {
            require(savingGoal.targetDate != null) {
                "Mục tiêu một lần phải có ngày mục tiêu"
            }
        }
        repository.addSavingGoal(savingGoal)
    }
}