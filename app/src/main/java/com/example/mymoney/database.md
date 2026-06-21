# 📊 Phân tích Hệ thống — MyMoney App

> Tài liệu kỹ thuật chi tiết phục vụ báo cáo và vận hành.  
> Kiến trúc: **Clean Architecture** · **MVVM** · **Jetpack Compose** · **Room** · **Supabase**

---

## 1. Tổng quan Kiến trúc Hệ thống

Ứng dụng được xây dựng theo mô hình **Clean Architecture**, đảm bảo tính độc lập, dễ test và mở rộng.

*   **UI Layer**: Jetpack Compose (Khai báo giao diện).
*   **Presentation Layer**: ViewModel & Contract (Quản lý State và Event).
*   **Domain Layer**: Use Cases (Nghiệp vụ cốt lõi - Pure Kotlin).
*   **Data Layer**: Repository, DAO, DTO (Lưu trữ và Truy xuất dữ liệu).

---

## 2. Quy trình Phân tích Chức năng (4 Bước Chuẩn)

Khi cần tìm hiểu hoặc giải trình một chức năng, hãy đi theo luồng sau:
1.  **UI (Giao diện):** Người dùng thấy gì và tương tác với nút nào?
2.  **ViewModel (Điều khiển):** Logic giao diện xử lý sự kiện đó ra sao?
3.  **Domain (Nghiệp vụ):** Quy tắc kinh doanh nào được áp dụng (Use Case)?
4.  **Data (Dữ liệu):** Dữ liệu được lưu xuống DB hoặc lấy lên như thế nào?

---

## 3. Chức năng chính: THÊM GIAO DỊCH (Add Transaction)

Giúp người dùng ghi lại các khoản thu chi bằng ngôn ngữ tự nhiên (AI) hoặc chụp ảnh.

### 📝 Các file liên quan:
*   **UI**: `ui/addtransaction/AddTransactionScreen.kt`
*   **ViewModel**: `presentation/viewmodel/addtransaction/AddTransactionViewModel.kt`
*   **Domain**: `AddTransactionUseCase`, `ParseTransactionMessageUseCase`.
*   **Data**: `TransactionDao`, `CategoryDao`, `WalletRepository`.

### 🚀 Luồng hoạt động (4 Bước):
1.  **UI**: Người dùng nhập "Ăn sáng 30k" vào thanh Chat hoặc nhấn chụp hóa đơn.
2.  **ViewModel**: Hàm `handleSubmit()` gọi `processUserMessage()`. Nó hiển thị ngay một "bubble" chat của user (Optimistic UI) và hiện icon đang xử lý.
3.  **Domain**: Gọi `ParseTransactionMessageUseCase`. Use Case này gửi text tới AI (Groq) để bóc tách thành: `Ghi chú: Ăn sáng, Số tiền: 30000, Loại: Chi phí, Danh mục: Ăn uống`.
4.  **Data**: 
    *   Hệ thống lấy ID ví hiện tại (`selectedWalletId`).
    *   Gọi `AddTransactionUseCase` để lưu vào bảng `transactions` (Room).
    *   **Đặc biệt**: Đồng thời gọi `walletRepository.updateWalletBalance` để trừ tiền ngay lập tức trong ví.

---

## 4. Chức năng chính: MÀN HÌNH CHÍNH (Home Dashboard)

Hiển thị tổng quan tài chính, số dư các ví và lịch sử giao dịch gần đây.

### 📝 Các file liên quan:
*   **UI**: `ui/home/HomeScreen.kt`
*   **ViewModel**: `presentation/viewmodel/home/HomeViewModel.kt`
*   **Domain**: `GetTransactionsByPeriodUseCase`, `GetPeriodSummaryUseCase`.
*   **Data**: `TransactionDao`, `WalletDao`.

### 🚀 Luồng hoạt động (4 Bước):
1.  **UI**: Khi mở app, `HomeScreen` hiển thị Skeleton Loading. User có thể vuốt chọn giữa các ví (Wallets).
2.  **ViewModel**: Sử dụng `combine` và `flatMapLatest` để quan sát đồng thời: Ví đang chọn, Khoảng thời gian (Ngày/Tuần/Tháng) và Cài đặt định dạng số.
3.  **Domain**: `getTransactionsByPeriod` trả về một `Flow` danh sách giao dịch. `GetPeriodSummaryUseCase` tính tổng Thu/Chi.
4.  **Data**: Room trả về dữ liệu thời gian thực. Bất kỳ khi nào user thêm giao dịch mới (ở bước 3), `HomeScreen` sẽ tự động cập nhật số dư mà không cần load lại trang nhờ sức mạnh của `Flow`.

---

## 5. Chức năng chính: TÌM KIẾM (Search)

Tìm kiếm giao dịch theo từ khóa, danh mục hoặc thời gian.

### 📝 Các file liên quan:
*   **UI**: `ui/search/SearchScreen.kt`
*   **ViewModel**: `SearchViewModel.kt`
*   **Domain**: `GetTransactionsUseCase`.
*   **Data**: `TransactionDao`.

### 🚀 Luồng hoạt động (4 Bước):
1.  **UI**: User gõ "Tiền điện" và chọn lọc theo "Tháng".
2.  **ViewModel**: Hàm `onEvent(SearchEvent.onQueryChange)` nhận text. Sử dụng `delay(300)` (Debounce) để không search liên tục khi user đang gõ.
3.  **Domain**: Lấy toàn bộ danh sách giao dịch của user qua `GetTransactionsUseCase`.
4.  **Data**: Thực hiện hàm `filter` ngay trên bộ nhớ (In-memory filtering) để tốc độ phản hồi cực nhanh, không cần query lại database nhiều lần.

---

## 6. Chức năng chính: AI CHAT & TRỢ LÝ (AI Assistant)

### 🚀 Quy trình nghiệp vụ:
1.  **Voice to Text**: Người dùng nhấn giữ mic -> Gọi `transcribeVoiceUseCase` (Whisper API) để chuyển giọng nói thành văn bản.
2.  **AI Parsing**: Văn bản được gửi tới Llama 3 (Groq) kèm theo **System Prompt** cực kỳ chi tiết để đảm bảo kết quả trả về là JSON chuẩn.
3.  **Smart Mapping**: Nếu AI trả về danh mục "Phở", app tự động map vào danh mục "Ăn uống" trong máy.
4.  **Security**: Mọi đoạn chat được lưu local tại `chat_messages` và tự động xóa sau 48h để bảo vệ quyền riêng tư.

---

## 7. Các chức năng Quản lý khác

| Chức năng | File xử lý chính | Logic đặc biệt |
| :--- | :--- | :--- |
| **Ngân sách (Budget)** | `BudgetViewModel.kt` | So sánh `amount` (Hạn mức) với `spent` (Thực tế) để báo động đỏ khi vượt 80%. |
| **Tiết kiệm (Saving)** | `SavingViewModel.kt` | Tính `%` tiến độ. Khi nạp tiền, tạo đồng thời 1 Record tiết kiệm và 1 giao dịch Chi phí. |
| **Thống kê (Stats)** | `StatisticsViewModel.kt` | Nhóm dữ liệu (`groupBy`) theo danh mục để vẽ biểu đồ tròn (Pie Chart). |
| **Đồng bộ (Sync)** | `SupabaseSyncRepository.kt`| Sử dụng `Worker` để đẩy dữ liệu lên Cloud khi có mạng (Offline-first). |

---

## 8. Thiết kế Dữ liệu (Database Schema)

### 🗄️ Bảng `transactions` (Cốt lõi)
| Cột | Ý nghĩa |
| :--- | :--- |
| `id` | Khóa chính |
| `note` | Nội dung giao dịch |
| `amount` | Số tiền (Double) |
| `type` | `income` (Thu) / `expense` (Chi) |
| `category` | Tên danh mục |
| `wallet_id` | Liên kết với ví nào |
| `timestamp` | Thời gian (Epoch) |

---
*Tài liệu này giúp bạn trả lời trôi chảy mọi câu hỏi về cấu trúc và logic của MyMoney.*
