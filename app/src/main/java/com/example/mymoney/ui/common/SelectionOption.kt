package com.example.mymoney.ui.common

data class SelectionOption<T>(
    val title: String,
    val value: T,
    val image: Int? = null
)

enum class SelectionLayout {
    LIST,
    GRID
}