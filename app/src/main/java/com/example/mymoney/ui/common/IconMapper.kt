package com.example.mymoney.ui.common

import com.example.mymoney.R

fun mapEmojiToDrawable(icon: String): Int {
    return when(icon) {
        "🍜" -> R.drawable.ic_category_food_noodle
        "🛍" -> R.drawable.ic_category_shopping_bag
        "🏠" -> R.drawable.ic_category_mansion
        "🎮" -> R.drawable.ic_category_joystick
        "💊" -> R.drawable.ic_category_pills
        "📚" -> R.drawable.ic_category_graduation_cap
        "🚗" -> R.drawable.ic_category_car
        else -> R.drawable.ic_category_food_noodle
    }
}