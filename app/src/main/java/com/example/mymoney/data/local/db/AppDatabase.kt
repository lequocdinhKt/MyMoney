package com.example.mymoney.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mymoney.data.local.dao.*
import com.example.mymoney.data.local.entity.*

@Database(
    entities = [
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        ChatMessageEntity::class,
        SavingGoalEntity::class,
        SavingRecordEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun savingDao(): SavingDao
    abstract fun savingRecordDao(): SavingRecordDao

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
