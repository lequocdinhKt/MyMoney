package com.example.mymoney.worker

    import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.mymoney.data.local.db.AppDatabase
import java.util.concurrent.TimeUnit

/**
 * WorkManager job định kỳ xóa chat_messages cũ hơn 48 giờ.
 *
 * Tần suất lý tưởng: 12 giờ/lần — tuy nhiên WorkManager KHÔNG đảm bảo đúng giờ.
 * Doze mode, battery optimization và hệ điều hành có thể trì hoãn thêm 1–2 giờ.
 * Đây là hành vi bình thường và chấp nhận được cho tác vụ dọn dẹp.
 *
 * Constraints:
 *   - BATTERY_NOT_LOW: tránh chạy khi máy sắp hết pin → không gây lag cho user.
 *   - Không yêu cầu network — chỉ thao tác Room local.
 *
 * Đăng ký: gọi [schedule] 1 lần trong Application.onCreate hoặc MainActivity.
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
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)   // không chạy khi pin yếu
                .build()

            val request = PeriodicWorkRequestBuilder<ChatCleanupWorker>(
                repeatInterval         = 12,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  // không reset nếu đã lên lịch
                request
            )
        }
    }
}

