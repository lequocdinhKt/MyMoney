package com.example.mymoney.domain.model

data class AIParsedResult(
    val displayText: String,
    val transactions: List<AIParsedTransaction> = emptyList(),
    val savings: List<AIParsedSaving> = emptyList()
)

data class AIParsedTransaction(
    val note: String,
    val amount: Double,
    val type: String,
    val category: String
)

data class AIParsedSaving(
    val goalTitle: String,
    val amount: Double,
    val note: String
)
