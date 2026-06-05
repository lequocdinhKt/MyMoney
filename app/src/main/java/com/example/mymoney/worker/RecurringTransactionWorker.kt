package com.example.mymoney.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.local.entity.TransactionEntity
import com.example.mymoney.data.local.entity.SyncStatus
import com.example.mymoney.data.repository.WalletRepositoryImpl
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Worker chạy hàng ngày (lúc 8:00 sáng) để kiểm tra và tạo giao dịch định kỳ đến hạn.
 *
 * Lịch trình: MainActivity (hoặc Application) gọi scheduleRecurringWorker() một lần.
 */
class RecurringTransactionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "RecurringWorker"

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val recurringDao = db.recurringTransactionDao()
        val transactionDao = db.transactionDao()
        val walletRepo = WalletRepositoryImpl(db.walletDao())
        val userId = SettingPreferences(applicationContext).currentUserId.first()
            ?: return Result.success()

        val now = System.currentTimeMillis()
        val dueItems = runCatching { recurringDao.getDueRecurring(userId, now) }
            .getOrElse {
                Log.e(TAG, "getDueRecurring failed: ${it.message}")
                return Result.retry()
            }

        Log.d(TAG, "Processing ${dueItems.size} due recurring transactions")

        for (item in dueItems) {
            try {
                // Kiểm tra số dư nếu là chi tiêu
                val wallet = walletRepo.getWalletById(item.walletId) ?: continue
                if (item.type == "expense" && wallet.balance < item.amount) {
                    Log.w(TAG, "Insufficient balance for recurring '${item.note}', skipping")
                    continue
                }

                // Tạo giao dịch
                val transaction = TransactionEntity(
                    userId          = userId,
                    walletId        = item.walletId,
                    categoryName    = item.categoryName,
                    amount          = item.amount,
                    type            = item.type,
                    note            = "${item.note} (định kỳ)",
                    transactionDate = now,
                    aiGenerated     = false,
                    createdAt       = now,
                    updatedAt       = now,
                    syncStatus      = SyncStatus.PENDING_INSERT
                )
                transactionDao.insert(transaction)

                // Cập nhật số dư ví
                val delta = if (item.type == "income") item.amount else -item.amount
                walletRepo.updateWalletBalance(item.walletId, wallet.balance + delta)

                // Tính nextDueDate tiếp theo
                val nextDue = computeNextDue(now, item.frequency)
                recurringDao.updateNextDueDate(item.id, nextDue)

                Log.d(TAG, "Created recurring TX: '${item.note}' ${item.type} ${item.amount}, nextDue=$nextDue")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing recurring id=${item.id}: ${e.message}")
            }
        }

        return Result.success()
    }

    private fun computeNextDue(fromMs: Long, frequency: String): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = fromMs }
        when (frequency) {
            "daily"   -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "weekly"  -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "monthly" -> cal.add(Calendar.MONTH, 1)
            "yearly"  -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }

    companion object {
        const val WORK_NAME = "recurring_transaction_worker"

        /**
         * Đăng ký lịch chạy định kỳ — gọi từ MainActivity.onCreate() một lần.
         *
         * ```kotlin
         * // Trong MainActivity.onCreate():
         * RecurringTransactionWorker.schedule(this)
         * ```
         */
        fun schedule(context: Context) {
            val request = androidx.work.PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
                1, java.util.concurrent.TimeUnit.DAYS
            )
                .setInitialDelay(
                    computeDelayUntil8AM(),
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .build()

            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        private fun computeDelayUntil8AM(): Long {
            val now = Calendar.getInstance()
            val next8AM = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            return next8AM.timeInMillis - now.timeInMillis
        }
    }
}

