package com.example.mymoney.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mymoney.data.local.dao.TransactionDao
import com.example.mymoney.data.local.entity.TransactionEntity

/**
 * Room Database chính của ứng dụng MyMoney.
 *
 * Hiện tại chỉ chứa bảng [TransactionEntity].
 * Khi thêm bảng mới → cập nhật entities array + tăng version.
 *
 * Sử dụng Singleton pattern để đảm bảo chỉ có 1 instance duy nhất.
 */
@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO để truy vấn bảng transactions */
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

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
                    // Khi chưa cấu hình migration → huỷ và tạo lại DB khi schema thay đổi
                    // TODO: Thay bằng addMigrations() khi có dữ liệu thực
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
