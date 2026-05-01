# 📊 PHÂN TÍCH CẤU TRÚC DATABASE - MyMoney App

**Ngày phân tích:** 2026-05-01  
**Version Database:**
- Room (Local): v3
- Supabase (Remote): Đang phát triển 🚀

---

## 📋 TỔNG QUAN

Project sử dụng **3 loại storage** cho dữ liệu:

```
┌─────────────────────────────────────────────────────┐
│         MYMONEY APP - DATA ARCHITECTURE             │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────────────────────────────────┐          │
│  │  1. LOCAL DATABASE (Room/SQLite)     │          │
│  │     ✅ Giao dịch offline-first       │          │
│  │     📍 File: data/local/db/          │          │
│  └──────────────────────────────────────┘          │
│                                                     │
│  ┌──────────────────────────────────────┐          │
│  │  2. REMOTE DATABASE (Supabase)       │          │
│  │     🔄 Sync & Backup cloud           │          │
│  │     📍 File: data/remote/            │          │
│  └──────────────────────────────────────┘          │
│                                                     │
│  ┌──────────────────────────────────────┐          │
│  │  3. KEY-VALUE STORAGE (DataStore)    │          │
│  │     ⚙️ Cài đặt & Preferences         │          │
│  │     📍 File: data/local/datastore/   │          │
│  └──────────────────────────────────────┘          │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 🗄️ PART 1: LOCAL DATABASE (Room/SQLite)

### 📍 Vị trí quản lý chính

```
app/src/main/java/com/example/mymoney/data/
├── local/
│   ├── db/              ← AppDatabase.kt (định nghĩa DB)
│   ├── entity/          ← Entity models (các bảng)
│   ├── dao/             ← Data Access Objects
│   └── datastore/       ← SettingPreferences (DataStore)
└── repository/          ← Repository implementations
```

### 🎯 AppDatabase.kt (File chính)

**Đường dẫn:** `app/src/main/java/com/example/mymoney/data/local/db/AppDatabase.kt`

**Thông tin chính:**
- Database name: `mymoney_database`
- Current version: **v3**
- Total entities: **4 bảng**

**Version history:**
```
v1 → v2: Thêm wallets, categories, chat_messages
v2 → v3: Thêm cột walletId cho transactions
```

### 📊 BẢNG HIỆN CÓ (4 bảng)

#### 1️⃣ **transactions** ✅ (Giao dịch tài chính)

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `id` | Long | Primary Key (auto-increment) |
| `note` | String | Ghi chú giao dịch |
| `amount` | Double | Số tiền (VNĐ) |
| `type` | String | "income" hoặc "expense" |
| `category` | String | Tên danh mục |
| `walletId` | Long | ID ví (FK → wallets) - v3 mới |
| `timestamp` | Long | Thời điểm (epoch millis) |

**Entity file:** `data/local/entity/TransactionEntity.kt`  
**DAO file:** `data/local/dao/TransactionDao.kt`  
**Query chính:**
- `getAllTransactions()` - lấy tất cả, sắp xếp DESC
- `getTransactionsByPeriod(from, to)` - theo thời gian
- `getTotalIncome(from, to)` - tổng thu nhập
- `getTotalExpense(from, to)` - tổng chi tiêu

---

#### 2️⃣ **wallets** ✅ (Ví tiền - v2 mới)

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `id` | Long | Primary Key |
| `userId` | String | ID người dùng (Supabase user) |
| `name` | String | Tên ví (VD: "Ví chính") |
| `balance` | Double | Số dư hiện tại |
| `icon` | String | Icon tên ("wallet", ...) |
| `color` | String | Mã hex (#0088F0) |
| `isDefault` | Boolean | Ví mặc định? |
| `isArchived` | Boolean | Đã lưu trữ? |
| `createdAt` | Long | Ngày tạo |
| `updatedAt` | Long | Lần cập nhật cuối |
| `supabaseId` | String? | UUID trên Supabase (sync) |

**Entity file:** `data/local/entity/WalletEntity.kt`  
**DAO file:** `data/local/dao/WalletDao.kt`  
**Query chính:**
- `getWallets(userId)` - lấy ví của user
- `getDefaultWallet(userId)` - ví mặc định
- `getTotalBalance(userId)` - tổng số dư
- `updateBalance(walletId, newBalance)` - cập nhật số dư

---

#### 3️⃣ **categories** ✅ (Danh mục - v2 mới)

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `id` | Long | Primary Key |
| `userId` | String? | User ID (null = danh mục hệ thống) |
| `name` | String | Tên danh mục (VD: "Ăn uống") |
| `icon` | String | Icon Material (VD: "restaurant") |
| `color` | String | Mã hex |
| `type` | String | "expense", "income", "both" |
| `isSystem` | Boolean | Danh mục hệ thống (không xóa)? |
| `isArchived` | Boolean | Đã ẩn? |
| `sortOrder` | Int | Thứ tự hiển thị |
| `createdAt` | Long | Ngày tạo |
| `supabaseId` | String? | UUID Supabase (sync) |

**Entity file:** `data/local/entity/CategoryEntity.kt`  
**DAO file:** `data/local/dao/CategoryDao.kt`  
**Danh mục hệ thống seed:** Ăn uống, Di chuyển, Mua sắm, Giải trí, Sức khỏe, Giáo dục, Hoá đơn, Tiền nhà, Lương, Thưởng, Đầu tư, Bán hàng...

---

#### 4️⃣ **chat_messages** ✅ (Lịch sử chat AI - v2 mới)

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `id` | Long | Primary Key |
| `userId` | String | ID người dùng |
| `content` | String | Nội dung tin nhắn |
| `sender` | String | "user" hoặc "ai" |
| `sessionId` | String | ID phiên (group theo ngày/48h) |
| `transactionId` | Long? | Transaction được tạo từ chat (FK) |
| `timestamp` | Long | Thời điểm gửi |

**Entity file:** `data/local/entity/ChatMessageEntity.kt`  
**DAO file:** `data/local/dao/ChatMessageDao.kt`  
**Index:** `(sessionId, timestamp)` - tối ưu query phiên

**Ghi chú:** Auto-delete tin nhắn cũ hơn 7 ngày (tuỳ chọn)

---

### 🔄 Migration History

**Migration v1 → v2:**
```kotlin
// Tạo 3 bảng mới: wallets, categories, chat_messages
db.execSQL("""
    CREATE TABLE IF NOT EXISTS wallets (...)
    CREATE TABLE IF NOT EXISTS categories (...)
    CREATE TABLE IF NOT EXISTS chat_messages (...)
""")
```

**Migration v2 → v3:**
```kotlin
// Thêm cột walletId cho transactions (FK to wallets)
db.execSQL("ALTER TABLE transactions ADD COLUMN walletId INTEGER NOT NULL DEFAULT 0")
```

---

### 📚 Repository Pattern

**Repository Implementations** (data/repository/):

| Bảng | Repository | File |
|------|--------------|------|
| transactions | TransactionRepositoryImpl | `TransactionRepositoryImpl.kt` |
| wallets | WalletRepositoryImpl | `WalletRepositoryImpl.kt` |
| categories | (embedded) | `WalletRepositoryImpl.kt` |
| chat_messages | ChatRepositoryImpl | `ChatRepositoryImpl.kt` |

**Pattern:** Entity ← → Mapper ← → Domain Model

---

## ☁️ PART 2: REMOTE DATABASE (Supabase)

### 📍 Vị trí quản lý chính

```
app/src/main/java/com/example/mymoney/data/
├── remote/
│   ├── SupabaseClient.kt         ← Singleton client
│   └── GroqService.kt            ← AI service
└── repository/
    ├── SupabaseTransactionRepository.kt  ← Sync transactions
    └── AuthRepositoryImpl.kt             ← Auth
```

### 🎯 SupabaseClient.kt (Configuration)

**Đường dẫn:** `app/src/main/java/com/example/mymoney/data/remote/SupabaseClient.kt`

**Thông tin:**
```kotlin
object SupabaseClient {
    private const val SUPABASE_URL = "https://cilhdctuvhpdqodfevla.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIs..."
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)  // Database API
        install(Auth)       // Authentication
    }
}
```

**Modules cài đặt:**
- ✅ **Postgrest** - Query/Insert/Update/Delete dữ liệu
- ✅ **Auth** - Đăng nhập, đăng ký, JWT

---

### 📊 BẢNG SUPABASE CẦN TẠO (7 bảng)

⚠️ **Hiện trạng:** Chỉ "transactions" có repository. Các bảng khác **chưa implement**.

| # | Bảng | Status | Repository | Ghi chú |
|----|------|--------|------------|---------|
| 1 | `transactions` | 🔄 Partial | `SupabaseTransactionRepository.kt` | Chỉ insert, chưa full CRUD |
| 2 | `wallets` | ❌ Not started | - | Cần tạo |
| 3 | `categories` | ❌ Not started | - | Cần tạo |
| 4 | `budgets` | ❌ Not started | - | Cần tạo |
| 5 | `saving_goals` | ❌ Not started | - | Cần tạo |
| 6 | `saving_contributions` | ❌ Not started | - | Cần tạo |
| 7 | `recurring_transactions` | ❌ Not started | - | Cần tạo |
| 8 | `profiles` | ❌ Not started | - | Auth info |

---

### 💾 SupabaseTransactionRepository.kt (Current Implementation)

**Đường dẫn:** `app/src/main/java/com/example/mymoney/data/repository/SupabaseTransactionRepository.kt`

**Functionality:**
- ✅ `insertTransaction()` - Insert 1 giao dịch mới
- ✅ Error handling non-fatal (không crash app)
- ❌ KHÔNG hỗ trợ: Update, Delete, Query, Sync

**Workflow:**
1. User nhập giao dịch qua AI Chat
2. Lưu vào **Room** (local - ngay)
3. Gọi `SupabaseTransactionRepository.insertTransaction()` nếu có network
4. Insert lên Supabase (async, non-blocking)

---

### 🔐 Cấu hình RLS (Row Level Security)

**Hư hưởng:** Bắt buộc kích hoạt trên tất cả bảng Supabase

**Policy mẫu:**
```sql
-- Tạo bảng trên Supabase
CREATE TABLE transactions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    note TEXT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    type TEXT NOT NULL,
    category_id UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    -- ...
);

-- Bật RLS
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

-- Policy: User chỉ thấy data của mình
CREATE POLICY "Users can CRUD own transactions"
    ON transactions FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);
```

---

## 🔑 PART 3: KEY-VALUE STORAGE (DataStore)

### 📍 Vị trí quản lý chính

**File:** `app/src/main/java/com/example/mymoney/data/local/datastore/SettingPreferences.kt`

### ⚙️ Thông tin chi tiết

**DataStore name:** `app_settings`

**Type:** Jetpack DataStore Preferences (thay thế SharedPreferences)

**Có 4 preference keys hiện tại:**

| Key | Kiểu | Default | Mô tả |
|-----|------|---------|-------|
| `IS_ONBOARDING_COMPLETED` | Boolean | false | User đã xem onboarding? |
| `SUPABASE_USER_ID` | String? | null | UUID user sau khi đăng nhập |
| `USERNAME` | String | "" | Username đã lưu (offline) |
| `IS_THOUSAND_SEPARATOR_ENABLED` | Boolean | true | Bật phân tách hàng nghìn? |

**Cách sử dụng:**

```kotlin
// Khởi tạo
val prefs = SettingPreferences(context)

// Đọc (Flow)
prefs.isOnboardingCompleted.collect { isCompleted ->
    // true/false
}

prefs.currentUserId.collect { userId ->
    // "uuid-string" hoặc null
}

// Ghi (suspend function)
prefs.saveOnboardingCompleted()
prefs.saveUserId(userId)
prefs.saveUsername(username)
```

**Ưu điểm DataStore:**
- ✅ Hỗ trợ coroutine/Flow (async-first)
- ✅ Type-safe (không string key error)
- ✅ Hoạt động tốt offline
- ✅ Atomic writes (không data corruption)

---

## 🏗️ ARCHITECTURE & LAYERS

### Clean Architecture Pattern

```
Domain Layer (Pure Kotlin models)
    ↓
Repository Interface & Impls (Data layer)
    ↓
Entity/DAO/DataStore (Persistence layer)
    ↓
ViewModels & UI (Presentation layer)
```

### Entity ↔ Model Mapping

Mỗi Entity có **extension functions** để convert tự động:

```kotlin
// Entity → Domain Model
val model = entity.toDomain()

// Domain Model → Entity
val entity = TransactionEntity.fromDomain(model)
```

---

## 🚀 DEPENDENCIES & LIBRARIES

**build.gradle.kts:**

```kotlin
// Room (Local Database)
implementation(libs.androidx.room.ktx)
implementation(libs.room.runtime)
ksp(libs.room.compiler)

// DataStore (Key-Value Storage)
implementation(libs.androidx.datastore.preferences)

// Supabase (Remote Database)
implementation(libs.supabase.postgrest)
implementation(libs.supabase.gotrue)

// HTTP Client
implementation(libs.ktor.client.android)
implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
```

---

## 📋 TÓMO TẮTSTATUS IMPLEMENTATION

### ✅ HOÀN THÀNH (Local)
- [x] TransactionEntity + Dao + Repository
- [x] WalletEntity + Dao + Repository
- [x] CategoryEntity + Dao
- [x] ChatMessageEntity + Dao + Repository
- [x] SettingPreferences (DataStore)
- [x] Database migrations (v1 → v3)

### 🔄 PARTIAL (Remote)
- [x] SupabaseClient (cấu hình)
- [x] SupabaseTransactionRepository (insert only)
- [ ] Full CRUD operations cho transactions
- [ ] Các repositories khác (wallets, categories, etc.)

### ⏳ CHƯA LÀM (Remaining)
- [ ] Wallets remote sync
- [ ] Categories remote sync
- [ ] Budgets table & sync
- [ ] Saving goals & contributions
- [ ] Recurring transactions
- [ ] Sync conflict resolution logic
- [ ] Migration guide implementation

---

## 🎯 RECOMMENDED NEXT STEPS

### Phase 2 (Hiện tại)
```
1. ✅ Hoàn thành Remote CRUD cho transactions
   - Thêm update, delete operations
   - Sync conflict resolution

2. 🆕 Implement Wallets remote repository
   - PUT/POST/DELETE operations
   - Balance sync logic

3. 🆕 Implement Categories remote repository
   - System categories seed
   - Custom categories CRUD
```

### Phase 3 (Tương lai)
```
1. Budgets & Saving features
2. Recurring transactions auto-generation
3. Offline-first sync worker
4. Analytics & Reports
```

---

## 📞 TÀI LIỆU LIÊN QUAN

| File | Mô tả |
|------|-------|
| `database.md` | 📘 Design doc - thiết kế DB toàn diện |
| `SUPABASE_AUTH_GUIDE.md` | 🔐 Guide auth & RLS |
| `AI_INTEGRATION_GUIDE.md` | 🤖 AI features (Groq, Gemini) |
| `build.gradle.kts` | 📦 Dependencies |

---

## 🔍 QUICK REFERENCE

### File Structure
```
data/
├── local/
│   ├── db/AppDatabase.kt           ← Database definition
│   ├── entity/                      ← Entity models
│   │   ├── TransactionEntity.kt
│   │   ├── WalletEntity.kt
│   │   ├── CategoryEntity.kt
│   │   └── ChatMessageEntity.kt
│   ├── dao/                         ← Database queries
│   │   ├── TransactionDao.kt
│   │   ├── WalletDao.kt
│   │   ├── CategoryDao.kt
│   │   └── ChatMessageDao.kt
│   └── datastore/
│       └── SettingPreferences.kt    ← Key-value storage
├── remote/
│   ├── SupabaseClient.kt            ← Remote config
│   ├── GroqService.kt               ← AI service
│   └── dto/                         ← DTO models
└── repository/
    ├── TransactionRepositoryImpl.kt  ← Local repo
    ├── WalletRepositoryImpl.kt
    ├── ChatRepositoryImpl.kt
    ├── SupabaseTransactionRepository.kt  ← Remote repo
    └── AuthRepositoryImpl.kt
```

### Key Methods

**Local (Room):**
```kotlin
val db = AppDatabase.getInstance(context)
db.transactionDao().getAllTransactions()
db.walletDao().getWallets(userId)
db.categoryDao().getCategories()
```

**Remote (Supabase):**
```kotlin
val supabase = SupabaseClient.client
supabase.postgrest["transactions"].insert(dto)
supabase.auth.signUp(email, password)
```

**DataStore:**
```kotlin
val prefs = SettingPreferences(context)
prefs.isOnboardingCompleted.collect { ... }
prefs.saveUserId(userId)
```

---

**Tạo bởi:** GitHub Copilot  
**Ngày:** 2026-05-01  
**Status:** ✅ Current

