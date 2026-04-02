package com.example.mymoney.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mymoney.domain.model.WalletModel

/**
 * Room Entity cho bảng "wallets".
 * Mỗi user có thể có nhiều ví tiền.
 */
@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val name: String,
    val balance: Double = 0.0,
    val icon: String = "wallet",
    val color: String = "#0088F0",
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val supabaseId: String? = null
) {
    fun toDomain(): WalletModel = WalletModel(
        id = id,
        userId = userId,
        name = name,
        balance = balance,
        icon = icon,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt,
        supabaseId = supabaseId
    )

    companion object {
        fun fromDomain(model: WalletModel): WalletEntity = WalletEntity(
            id = model.id,
            userId = model.userId,
            name = model.name,
            balance = model.balance,
            icon = model.icon,
            color = model.color,
            isDefault = model.isDefault,
            isArchived = model.isArchived,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt,
            supabaseId = model.supabaseId
        )
    }
}
