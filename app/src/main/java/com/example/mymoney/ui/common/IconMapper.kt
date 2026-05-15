package com.example.mymoney.ui.common

import com.example.mymoney.R

fun mapEmojiToDrawable(icon: String): Int {
    return when(icon) {
        "🍜" -> R.drawable.ic_category_expense_noodle
        "🛍" -> R.drawable.ic_category_expense_shopping_bag
        "🏠" -> R.drawable.ic_category_expense_mansion
        "🎮" -> R.drawable.ic_category_expense_joystick
        "💊" -> R.drawable.ic_category_expense_pills
        "📚" -> R.drawable.ic_category_expense_education
        "📄" -> R.drawable.ic_category_expense_bill
        "📦" -> R.drawable.ic_category_expense_box
        "🚗" -> R.drawable.ic_category_expense_car
        "💰" -> R.drawable.ic_category_income_money
        "🎁" -> R.drawable.ic_category_income_bonus
        "📈" -> R.drawable.ic_category_income_investment
        "💼" -> R.drawable.ic_category_income_bag
        else -> R.drawable.ic_category_expense_noodle
    }
}