package com.example.mymoney.domain.model

/**
 * Domain model cho một giao dịch tài chính.
 * Pure Kotlin – không phụ thuộc Android hay Room.
 *
 * @param id        Mã định danh duy nhất (auto-generate bởi Room)
 * @param note      Ghi chú / mô tả giao dịch (VD: "Bữa tối 100k, mua sắm 400k")
 * @param amount    Số tiền giao dịch (đơn vị: VNĐ). Dương = thu, Âm = chi
 * @param type      Loại giao dịch: "income" hoặc "expense"
 * @param category  Danh mục giao dịch (VD: "Ăn uống", "Mua sắm")
 * @param timestamp Thời điểm tạo giao dịch (epoch millis)
 */
data class TransactionModel(
    val id: Long = 0L,
    val note: String,
    val amount: Double,
    val type: String = "expense",          // "income" | "expense"
    val category: String = "Khác",
    val timestamp: Long = System.currentTimeMillis()
)
