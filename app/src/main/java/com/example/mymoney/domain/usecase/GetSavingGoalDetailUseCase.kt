package com.example.mymoney.domain.usecase

import com.example.mymoney.domain.model.SavingGoalDetailModel
import com.example.mymoney.domain.model.SavingType
import com.example.mymoney.domain.repository.SavingRecordRepository
import com.example.mymoney.domain.repository.SavingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class GetSavingGoalDetailUseCase(
    private val goalRepository: SavingRepository,
    private val recordRepository: SavingRecordRepository
) {
    operator fun invoke(goalId: Long): Flow<SavingGoalDetailModel?> {
        return combine(
            goalRepository.observeSavingGoalById(goalId),
            recordRepository.getRecordsByGoalId(goalId)
        ) { goal, records ->
            if (goal == null) return@combine null

            val totalSavedAllTime = records.sumOf { it.amount }
            val now = System.currentTimeMillis()
            val daysRemaining = goal.targetDate?.let { ((it - now) / (24 * 60 * 60 * 1000)).coerceAtLeast(0) }
            
            val cycleStart: Long?
            val cycleEnd: Long?
            val currentCycleSaved: Double
            
            when (goal.savingType) {
                SavingType.ONE_TIME -> {
                    cycleStart = null
                    cycleEnd = null
                    currentCycleSaved = totalSavedAllTime
                }
                SavingType.WEEKLY -> {
                    val range = getCurrentWeeklyCycle(goal.createdAt, now)
                    cycleStart = range.first
                    cycleEnd = range.second
                    currentCycleSaved = records.filter { it.recordDate in cycleStart..cycleEnd }.sumOf { it.amount }
                }
                SavingType.MONTHLY -> {
                    val range = getCurrentMonthlyCycle(goal.createdAt, now)
                    cycleStart = range.first
                    cycleEnd = range.second
                    currentCycleSaved = records.filter { it.recordDate in cycleStart..cycleEnd }.sumOf { it.amount }
                }
            }
            
            val progress = if (goal.targetAmount > 0) (currentCycleSaved / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
            val remainingAmount = (goal.targetAmount - currentCycleSaved).coerceAtLeast(0.0)

            SavingGoalDetailModel(
                goal = goal,
                records = records,
                totalSavedAllTime = totalSavedAllTime,
                progress = progress,
                remainingAmount = remainingAmount,
                daysRemaining = daysRemaining,
                currentCycleSaved = currentCycleSaved,
                currentCycleStart = cycleStart,
                currentCycleEnd = cycleEnd
            )
        }
    }

    /**
     * Chu kỳ tuần tính từ ngày tạo mục tiêu (createdAt).
     *
     * Ý tưởng:
     * - Chia timeline thành các block 7 ngày liên tiếp
     * - Xác định "now" đang nằm trong block thứ mấy
     * - Trả về start/end của block đó
     *
     * Ví dụ:
     * createdAt = 01/06
     * now = 20/6
     *
     * Chu kỳ:
     * 01/06 - 07/06
     * 08/06 - 14/06
     * 15/06 - 21/06  ← now nằm trong đây
     */
    private fun getCurrentWeeklyCycle(createdAt: Long, now: Long): Pair<Long, Long> {
        val weekMillis = 7L * 24 * 60 * 60 * 1000 // 7 ngày tính theo milliseconds

        /**
         * Tính xem từ createdAt đến now đã trôi qua bao nhiêu tuần
         *
         * Ví dụ:
         * 15 ngày / 7 = 2 tuần
         */
        val elapsedWeeks = ((now - createdAt) / weekMillis).coerceAtLeast(0)

        /**
         * Tìm điểm bắt đầu của chu kỳ hiện tại
         *
         * createdAt + (số tuần đã qua × 7 ngày)
         *
         * Ví dụ:
         * createdAt = 01/06
         * elapsedWeeks = 2
         * => start = 15/06
         */
        val start = createdAt + elapsedWeeks * weekMillis

        /**
         * End của chu kỳ = start + 7 ngày - 1ms
         *
         * -1ms để không overlap sang chu kỳ sau
         */
        val end = start + weekMillis - 1
        return start to end
    }

    /**
     * Chu kỳ tháng tính từ ngày tạo mục tiêu (createdAt).
     *
     * Ý tưởng:
     * - Không dùng 30 ngày cố định (vì tháng 28/29/30/31 ngày)
     * - Dùng Calendar để cộng theo MONTH thật
     * - Chu kỳ luôn bắt đầu từ ngày tạo ban đầu
     *
     * Ví dụ:
     * createdAt = 15/01
     *
     * Chu kỳ:
     * 15/01 - 14/02
     * 15/02 - 14/03
     * 15/03 - 14/04
     * ...
     */
    private fun getCurrentMonthlyCycle(createdAt: Long, now: Long): Pair<Long, Long> {
        // Convert timestamp -> Calendar để xử lý theo YEAR/MONTH
        val startCal = Calendar.getInstance().apply { timeInMillis = createdAt }
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }

        /**
         * Tính số tháng đã trôi qua giữa createdAt và now
         *
         * Công thức:
         * (chênh lệch năm × 12) + chênh lệch tháng
         *
         * Ví dụ:
         * Jan → Jun = 5 tháng
         */
        var monthsPassed = (nowCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)) * 12 +
                (nowCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH))

        /**
         * Tạo thử mốc start bằng cách cộng monthsPassed tháng vào createdAt
         *
         * Mục đích:
         * - kiểm tra xem có bị vượt quá "now" không
         */
        val candidateStart = Calendar.getInstance().apply {
            timeInMillis = createdAt
            add(Calendar.MONTH, monthsPassed)
        }

        /**
         * Nếu start dự đoán bị vượt quá now → lùi lại 1 tháng
         *
         * (fix case lệch do ngày trong tháng khác nhau)
         */
        if (candidateStart.timeInMillis > now) { monthsPassed-- }

        /**
         * Tính lại start chính xác của chu kỳ hiện tại
         */
        val cycleStart = Calendar.getInstance().apply { 
            timeInMillis = createdAt
            add(Calendar.MONTH, monthsPassed)
        }

        /**
         * End = start + 1 tháng - 1ms
         *
         * - dùng MONTH để đảm bảo đúng số ngày của từng tháng
         */
        val cycleEnd = Calendar.getInstance().apply { 
            timeInMillis = cycleStart.timeInMillis
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return cycleStart.timeInMillis to cycleEnd.timeInMillis
    }
}