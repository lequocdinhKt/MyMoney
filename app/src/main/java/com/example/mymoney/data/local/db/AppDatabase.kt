package com.example.mymoney.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mymoney.data.local.dao.CategoryDao
import com.example.mymoney.data.local.dao.ChatMessageDao
import com.example.mymoney.data.local.dao.TransactionDao
import com.example.mymoney.data.local.dao.WalletDao
import com.example.mymoney.data.local.entity.CategoryEntity
import com.example.mymoney.data.local.entity.ChatMessageEntity
import com.example.mymoney.data.local.entity.TransactionEntity
import com.example.mymoney.data.local.entity.WalletEntity

/**
 * Room Database chính của ứng dụng MyMoney.
 *
 * Version history:
 *   v1 → v2: Thêm bảng wallets, categories, chat_messages
 */
@Database(
    entities = [
        TransactionEntity::class,
        WalletEntity::class,
        CategoryEntity::class,
        ChatMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO để truy vấn bảng transactions */
    abstract fun transactionDao(): TransactionDao

    /** DAO để truy vấn bảng wallets */
    abstract fun walletDao(): WalletDao

    /** DAO để truy vấn bảng categories */
    abstract fun categoryDao(): CategoryDao

    /** DAO để truy vấn bảng chat_messages */
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Migration v1 → v2: thêm bảng wallets, categories, chat_messages */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS wallets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        balance REAL NOT NULL DEFAULT 0.0,
                        icon TEXT NOT NULL DEFAULT 'wallet',
                        color TEXT NOT NULL DEFAULT '#0088F0',
                        isDefault INTEGER NOT NULL DEFAULT 0,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        supabaseId TEXT
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        color TEXT NOT NULL,
                        type TEXT NOT NULL,
                        isSystem INTEGER NOT NULL DEFAULT 0,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        supabaseId TEXT
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        content TEXT NOT NULL,
                        sender TEXT NOT NULL,
                        sessionId TEXT NOT NULL,
                        transactionId INTEGER,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_messages_sessionId_timestamp ON chat_messages (sessionId, timestamp)")
            }
        }

        /**
         * Lấy Singleton instance của database.
         * Thread-safe — dùng synchronized block.
         *
         * Cách dùng:
         * ```kotlin
         * val db = AppDatabase.getInstance(applicationContext)
         * val dao = db.transactionDao()
         * ```
         *
         * @param context Application context (KHÔNG dùng Activity context để tránh memory leak)
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mymoney_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
