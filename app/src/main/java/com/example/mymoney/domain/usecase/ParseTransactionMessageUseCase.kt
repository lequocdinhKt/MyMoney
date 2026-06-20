package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.AIParsedResult
import com.example.mymoney.domain.repository.AIParsingRepository

class ParseTransactionMessageUseCase(
    private val repository: AIParsingRepository
) {
    suspend operator fun invoke(message: String, customRules: String?): AIParsedResult {
        return repository.parseMessage(message, customRules)
    }
}
