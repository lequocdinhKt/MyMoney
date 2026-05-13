package com.example.mymoney.ui.common

import androidx.compose.ui.graphics.vector.ImageVector

data class SelectionOption<T>(
    val title: String,
    val value: T,
    val image: ImageVector? = null
)

enum class SelectionLayout {
    LIST,
    GRID
}