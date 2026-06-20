package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.repository.AIParsingRepository
import java.io.File

class TranscribeVoiceUseCase(
    private val repository: AIParsingRepository
) {
    suspend operator fun invoke(audioFile: File): String {
        return repository.transcribeVoice(audioFile)
    }
}
