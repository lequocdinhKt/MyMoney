package com.example.mymoney.domain.repository

import com.example.mymoney.domain.model.AIParsedResult
import java.io.File

interface AIParsingRepository {
    suspend fun parseMessage(message: String, customRules: String?): AIParsedResult
    suspend fun transcribeVoice(audioFile: File): String
}
