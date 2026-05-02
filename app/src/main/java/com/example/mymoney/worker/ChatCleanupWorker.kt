package com.example.mymoney.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.mymoney.data.local.db.AppDatabase
import java.util.concurrent.TimeUnit

/**
 * WorkManager job chạy mỗi 12 giờ để xóa chat_messages cũ hơn 48 giờ.
 *
 * Cách đăng ký (gọi 1 lần trong Application.onCreate hoặc MainActivity):
 *   ChatCleanupWorker.schedule(context)
 */
class ChatCleanupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val threshold = System.currentTimeMillis() - RETENTION_MS
        AppDatabase.getInstance(applicationContext)
            .chatMessageDao()
            .deleteOlderThan(threshold)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "chat_cleanup_periodic"
        private val RETENTION_MS = TimeUnit.HOURS.toMillis(48)

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ChatCleanupWorker>(
                repeatInterval = 12,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  // không reset nếu đã lên lịch
                request
            )
        }
    }
}

