# DEBUG GUIDE — MyMoney

Tài liệu này liệt kê tất cả các thành phần được thêm/sửa, kèm theo cách kiểm tra.

> **Cập nhật lần 2** (sau code-review): Ba lỗ hổng nghiêm trọng đã được vá — xem mục 9.

---

## 1. Room Database (Local)

### Files added
| File | Mục đích |
|------|----------|
| `data/local/entity/SyncStatus.kt` | Hằng số trạng thái đồng bộ: SYNCED=0, PENDING_INSERT=1, PENDING_UPDATE=2, PENDING_DELETE=3 |
| `data/local/entity/WalletEntity.kt` | Bảng `wallets` — có `supabase_id`, `sync_status`, `is_deleted` |
| `data/local/entity/CategoryEntity.kt` | Bảng `categories` — có `supabase_id`, `sync_status`, `is_deleted` |
| `data/local/entity/TransactionEntity.kt` | Bảng `transactions` — FK đến wallet + category (RESTRICT) |
| `data/local/entity/BudgetEntity.kt` | Bảng `budgets` — unique(user_id, category_id, month, year) |
| `data/local/entity/ChatMessageEntity.kt` | Bảng `chat_messages` — local only, FK đến transaction (SET_NULL) |
| `data/local/dao/WalletDao.kt` | CRUD + `softDelete` + `getPendingSync` + `markSynced` |
| `data/local/dao/CategoryDao.kt` | CRUD + `softDelete` + `observeByUserId` |
| `data/local/dao/TransactionDao.kt` | CRUD + `observeByDateRange` + `sumIncome` + `sumExpense` |
| `data/local/dao/BudgetDao.kt` | CRUD + `softDelete` + `observeBudgets` |
| `data/local/dao/ChatMessageDao.kt` | CRUD + `deleteOlderThan(thresholdMs)` |
| `data/local/db/AppDatabase.kt` | Room singleton, version=1, 5 entities, 5 DAOs |

### Cách kiểm tra
1. Build app → nếu không crash tại `AppDatabase.getInstance()` thì Room schema OK.
2. Dùng **Android Studio → App Inspection → Database Inspector** để xem database `mymoney.db`.
3. Kiểm tra tất cả 5 bảng tồn tại: `wallets`, `categories`, `transactions`, `budgets`, `chat_messages`.
4. Thêm 1 wallet thông qua AddTransactionScreen → kiểm tra bảng `wallets` có record mới với `sync_status=1` (PENDING_INSERT).

### Lỗi thường gặp
- **`IllegalStateException: Room cannot verify the data integrity`** → xóa app data hoặc tăng `version` trong `AppDatabase.kt`.
- **`SQLiteConstraintException`** → vi phạm FK (ví dụ xóa wallet khi còn transaction). Đây là behavior mong muốn vì dùng `RESTRICT`.

---

## 2. WorkManager — Chat Cleanup

### File
`worker/ChatCleanupWorker.kt`

### Logic
- Chạy mỗi **~12 giờ** — WorkManager **KHÔNG đảm bảo đúng giờ**: Doze mode, battery
  optimization và hệ điều hành có thể trì hoãn thêm **1–2 giờ**. Đây là hành vi bình thường.
- Xóa tất cả `chat_messages` có `timestamp < now - 48h`.
- Đăng ký trong `MainActivity.onCreate()` với policy `KEEP`.
- **Constraints** (mới): `setRequiresBatteryNotLow(true)` — worker không chạy khi pin yếu,
  tránh làm lag khi user đang dùng app.

### Cách kiểm tra
1. **Android Studio → App Inspection → Background Task Inspector** → tìm `chat_cleanup_periodic`.
2. Test nhanh (debug build): tạm đổi interval sang 15 phút.
3. Logcat: tìm tag `WorkerResult`.

### Lỗi thường gặp
- **Worker không chạy trên emulator** → Tắt Doze: `adb shell dumpsys deviceidle disable`.

---

## 3. Repository Layer

### Interfaces (domain layer)
| File | Mô tả |
|------|-------|
| `domain/repository/WalletRepository.kt` | (đã có từ trước) |
| `domain/repository/CategoryRepository.kt` | Thêm `seedDefaultCategories(userId)` |
| `domain/repository/TransactionRepository.kt` | (đã có từ trước) |
| `domain/repository/BudgetRepository.kt` | Mới hoàn toàn |
| `domain/repository/ChatRepository.kt` | (đã có từ trước) |

### Implementations (data layer)
| File | Constructor | Ghi chú |
|------|------------|---------|
| `data/repository/WalletRepositoryImpl.kt` | `(walletDao)` | |
| `data/repository/CategoryRepositoryImpl.kt` | `(categoryDao)` | `seedDefaultCategories` idempotent — xem mục 9.1 |
| `data/repository/TransactionRepositoryImpl.kt` | `(transactionDao)` | userId truyền qua method — xem mục 9.2 |
| `data/repository/BudgetRepositoryImpl.kt` | `(budgetDao)` | |
| `data/repository/ChatRepositoryImpl.kt` | `(chatMessageDao)` | |

### Cách kiểm tra seedDefaultCategories
Sau khi login/signup thành công, gọi:
```kotlin
CategoryRepositoryImpl(db.categoryDao()).seedDefaultCategories(userId)
```
→ Database Inspector: bảng `categories` có đúng 12 bản ghi — gọi lần 2 vẫn 12, không tăng.

---

## 4. ViewModelFactory

### Files added
| File | ViewModel được tạo |
|------|-------------------|
| `presentation/viewmodel/auth/AuthViewModelFactory.kt` | `AuthViewModel` |
| `presentation/viewmodel/addtransaction/AddTransactionViewModelFactory.kt` | `AddTransactionViewModel` |
| `presentation/viewmodel/setting/SettingViewModelFactory.kt` | `SettingViewModel` |
| `presentation/viewmodel/budget/BudgetViewModelFactory.kt` | `BudgetViewModel` (MỚI) |
| `presentation/viewmodel/home/HomeViewModelFactory.kt` | `HomeViewModel` (đã có từ trước) |

### Cách dùng BudgetViewModelFactory trong Composable
```kotlin
val context = LocalContext.current
val budgetViewModel: BudgetViewModel = viewModel(
    factory = BudgetViewModelFactory(context, userId)
)
```

### Cách kiểm tra factory hoạt động
1. Mở màn hình tương ứng → quan sát Logcat, không có `ViewModelProvider` exception.
2. Thêm log tạm trong ViewModel `init {}` để xác nhận constructor chạy.

---

## 5. BudgetViewModel — Nâng cấp

### Trước (stub)
```kotlin
class BudgetViewModel : ViewModel() {
    // không có dependency
}
```

### Sau
```kotlin
class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val userId: String
) : ViewModel() {
    val uiState: StateFlow<BudgetUiState> // expose budgets list
    fun onEvent(event: BudgetEvent) // xử lý SaveBudget, DeleteBudget, LoadMonth
}
```

`BudgetUiState` giờ có thêm:
```kotlin
data class BudgetUiState(
    val isLoading: Boolean = true,
    val budgets: List<BudgetModel> = emptyList()
)
```

`BudgetEvent` giờ có 3 subclass:
- `SaveBudget(budget: BudgetModel)`
- `DeleteBudget(id: Long)`
- `LoadMonth(month: Int, year: Int)`

---

## 6. Dependency Graph tổng quan

```
MainActivity
  └── ChatCleanupWorker.schedule()         [WorkManager, pin-safe]

MainScreen(userId)
  └── HomeViewModelFactory(userId)
        ├── TransactionRepositoryImpl(dao)  ← Singleton, không giữ userId
        │     └── getTransactionsByPeriod(userId, from, to)
        └── WalletRepositoryImpl(dao)

AddTransactionScreen
  └── AddTransactionViewModelFactory(ctx, userId)
        ├── TransactionRepositoryImpl(dao)  ← Singleton
        └── ...

BudgetScreen(userId)
  └── BudgetViewModelFactory(ctx, userId)
        └── BudgetRepositoryImpl(dao)

SettingScreen
  └── SettingViewModelFactory(ctx, userId)
        └── TransactionRepositoryImpl(dao)  ← Singleton
              └── getAllTransactions(userId) ← userId lấy từ DataStore bên trong VM
```

---

## 7. Checklist Build

- [ ] `./gradlew assembleDebug` — build thành công không lỗi
- [ ] App không crash khi khởi động
- [ ] Database Inspector thấy 5 bảng Room
- [ ] Background Task Inspector thấy `chat_cleanup_periodic`
- [ ] Gọi `seedDefaultCategories(userId)` 2 lần → bảng `categories` vẫn chỉ có 12 bản ghi
- [ ] Thêm transaction → hiện trong HomeScreen
- [ ] Thêm budget → `BudgetViewModel.uiState.budgets` cập nhật
- [ ] Đổi user (logout → login account khác) → HomeScreen hiển thị đúng dữ liệu user mới

---

## 8. Pending TODOs (chưa implement)

| Việc cần làm | Ưu tiên |
|-------------|---------|
| Gọi `seedDefaultCategories(userId)` sau login/signup thành công | HIGH |
| Tạo lại `SettingPreferences` (DataStore) — bị xóa trong cleanup | HIGH |
| Implement `BudgetScreen` UI sử dụng `BudgetViewModel` | MEDIUM |
| WorkManager sync lên Supabase (`getPendingSync` → upload → `markSynced`) | MEDIUM |
| `MainViewModel.kt` vẫn là stub | LOW |

---

## 9. Lỗ hổng đã vá (Code-Review lần 2)

### 9.1 Seed Categories — Idempotent Guard

**Vấn đề cũ:** Gọi `seedDefaultCategories(userId)` nhiều lần (cài lại app, login thiết bị B)
→ tạo duplicate categories → phá Data Integrity.

**Fix:** `CategoryDao` thêm `countSystemCategories(userId)`. `seedDefaultCategories` kiểm tra
trước khi insert — nếu đã có system category thì skip ngay.

```kotlin
// CategoryRepositoryImpl.kt
override suspend fun seedDefaultCategories(userId: String) {
    if (categoryDao.countSystemCategories(userId) > 0) return  // ← guard
    // ... tạo 12 danh mục mặc định
}
```

**Kiểm tra:** Gọi 2 lần liên tiếp với cùng userId → bảng `categories` vẫn đúng 12 bản ghi.

---

### 9.2 TransactionRepository — Xóa userId khỏi Constructor

**Vấn đề cũ:** `TransactionRepositoryImpl(dao, userId)` giữ userId trong constructor.
Nếu user A logout, user B login → phải tái tạo toàn bộ DI graph → nguy cơ memory leak &
stale state.

**Fix:** Repository trở thành Singleton thực sự — userId truyền qua từng phương thức:

| Before | After |
|--------|-------|
| `TransactionRepositoryImpl(dao, userId)` | `TransactionRepositoryImpl(dao)` |
| `getAllTransactions()` | `getAllTransactions(userId)` |
| `getTransactionsByPeriod(from, to)` | `getTransactionsByPeriod(userId, from, to)` |
| `getTotalIncome(from, to)` | `getTotalIncome(userId, from, to)` |
| `getTotalExpense(from, to)` | `getTotalExpense(userId, from, to)` |

**Files đã cập nhật:**
- ✅ `domain/repository/TransactionRepository.kt` — interface
- ✅ `data/repository/TransactionRepositoryImpl.kt` — impl
- ✅ `domain/usecase/GetTransactionsUseCase.kt`
- ✅ `domain/usecase/GetTransactionsByPeriodUseCase.kt`
- ✅ `domain/usecase/GetPeriodSummaryUseCase.kt`
- ✅ `presentation/viewmodel/home/HomeViewModel.kt`
- ✅ `presentation/viewmodel/setting/SettingViewModel.kt`
- ✅ `presentation/viewmodel/addtransaction/AddTransactionViewModelFactory.kt`
- ✅ `presentation/viewmodel/setting/SettingViewModelFactory.kt`
- ✅ `ui/main/MainScreen.kt`

---

### 9.3 ChatCleanupWorker — Constraints & Timing

**Vấn đề cũ:** Chạy không điều kiện → có thể gây lag khi pin thấp. Không có ghi chú về
độ trễ WorkManager.

**Fix:**
```kotlin
val constraints = Constraints.Builder()
    .setRequiresBatteryNotLow(true)
    .build()
```
Thêm comment rõ ràng: WorkManager có thể trễ **1–2 giờ** do Doze mode — hành vi bình thường.

---

### 9.4 Conflict Resolution Strategy (Planned)

Khi Sync Worker upload lên Supabase, cần nguyên tắc giải quyết xung đột:

> **Last-Write-Wins dựa trên `updated_at`**: bản ghi nào có `updated_at` lớn hơn (mới hơn)
> thì ghi đè.

Quy trình sync cụ thể:
1. Đọc `getPendingSync(userId)` từ Room.
2. Với mỗi record: `upsert` lên Supabase (dùng `supabase_id` làm conflict key).
3. Supabase trả về record đã lưu → so sánh `updated_at`.
4. Nếu Supabase có bản ghi mới hơn → update Room với dữ liệu từ Supabase.
5. Gọi `markSynced(localId, supabaseId)` → `sync_status = SYNCED`.

Trường hợp `is_deleted = 1`:
- Upload DELETE lên Supabase trước.
- Sau khi Supabase xác nhận → hard-delete khỏi Room (hoặc giữ lại, tuỳ quyết định).
