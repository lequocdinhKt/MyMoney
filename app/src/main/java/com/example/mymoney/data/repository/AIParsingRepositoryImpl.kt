package com.example.mymoney.data.repository

import com.example.mymoney.data.remote.GroqService
import com.example.mymoney.domain.model.AIParsedResult
import com.example.mymoney.domain.model.AIParsedSaving
import com.example.mymoney.domain.model.AIParsedTransaction
import com.example.mymoney.domain.repository.AIParsingRepository
import java.io.File

class AIParsingRepositoryImpl : AIParsingRepository {

    override suspend fun parseMessage(message: String, customRules: String?): AIParsedResult {
        val serviceResult = GroqService.chatWithParsing(message, customRules)
        return AIParsedResult(
            displayText = serviceResult.displayText,
            transactions = serviceResult.transactions.map {
                AIParsedTransaction(
                    note = it.note,
                    amount = it.amount,
                    type = it.type,
                    category = it.category
                )
            },
            savings = serviceResult.savings.map {
                AIParsedSaving(
                    goalTitle = it.goalTitle,
                    amount = it.amount,
                    note = it.note
                )
            }
        )
    }

    override suspend fun transcribeVoice(audioFile: File): String {
        return GroqService.transcribeAudio(audioFile)
    }
}
