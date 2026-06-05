package com.example.mymoney.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mymoney.data.local.dao.BudgetDao
import com.example.mymoney.data.local.dao.CategoryDao
import com.example.mymoney.data.local.dao.ChatMessageDao
import com.example.mymoney.data.local.dao.RecurringTransactionDao
import com.example.mymoney.data.local.dao.TransactionDao
import com.example.mymoney.data.local.dao.WalletDao
import com.example.mymoney.data.local.entity.BudgetEntity
import com.example.mymoney.data.local.entity.CategoryEntity
import com.example.mymoney.data.local.entity.ChatMessageEntity
import com.example.mymoney.data.local.entity.RecurringTransactionEntity
import com.example.mymoney.data.local.entity.TransactionEntity
import com.example.mymoney.data.local.entity.WalletEntity

@Database(
    entities = [
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        ChatMessageEntity::class,
        RecurringTransactionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        private const val DB_NAME = "mymoney.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)   // dev phase
                    .build().also { INSTANCE = it }
            }
    }
}

