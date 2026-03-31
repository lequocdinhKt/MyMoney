# 🗄️ Database Design — MyMoney App

> Clean Architecture · Room (Local) · Supabase (Remote)  
> Thiết kế cho app quản lý tài chính cá nhân với AI Chat

---

## Mục lục

1. [Tổng quan kiến trúc dữ liệu](#1-tổng-quan-kiến-trúc-dữ-liệu)
2. [Bảng hiện có](#2-bảng-hiện-có)
3. [Bảng đề xuất thêm](#3-bảng-đề-xuất-thêm)
4. [Chi tiết từng bảng](#4-chi-tiết-từng-bảng)
5. [Quan hệ giữa các bảng (ERD)](#5-quan-hệ-giữa-các-bảng-erd)
6. [Supabase Tables (Remote)](#6-supabase-tables-remote)
7. [DataStore Preferences (Key-Value)](#7-datastore-preferences-key-value)
8. [Chiến lược đồng bộ Local ↔ Remote](#8-chiến-lược-đồng-bộ-local--remote)
9. [Thứ tự triển khai](#9-thứ-tự-triển-khai)
10. [Migration Guide](#10-migration-guide)

---

## 1. Tổng quan kiến trúc dữ liệu

```
┌──────────────────────────────────────────────────────┐
│                    MyMoney App                       │
│                                                      │
│  ┌─────────────┐    ┌─────────────┐    ┌──────────┐ │
│  │  Room (Local)│    │  Supabase   │    │ DataStore│ │
│  │  SQLite DB   │    │  (Remote)   │    │ (Prefs)  │ │
│  └──────┬───────┘    └──────┬──────┘    └────┬─────┘ │
│         │                   │                │       │
│         │   ┌───────────────┘                │       │
│         ▼   ▼                                ▼       │
│  ┌─────────────────┐              ┌─────────────────┐│
│  │  Repository Impl│              │SettingPreferences││
│  └────────┬────────┘              └─────────────────┘│
│           ▼                                          │
│  ┌─────────────────┐                                 │
│  │    Use Cases     │                                │
│  └────────┬────────┘                                 │
│           ▼                                          │
│  ┌─────────────────┐                                 │
│  │   ViewModels     │                                │
│  └──────────────────┘                                │
└──────────────────────────────────────────────────────┘
```

**Nguyên tắc:**
- **Room** = nguồn dữ liệu chính (offline-first)
- **Supabase** = đồng bộ cloud, auth, backup
- **DataStore** = cài đặt nhỏ (key-value), không lưu dữ liệu lớn

---

## 2. Bảng hiện có

| # | Bảng | Storage | Trạng thái |
|---|------|---------|------------|
| 1 | `transactions` | Room | ✅ Đã tạo |

---

## 3. Bảng đề xuất thêm

| # | Bảng | Storage | Phục vụ màn hình | Mô tả |
|---|------|---------|------------------|-------|
| 2 | `wallets` | Room + Supabase | Home, tất cả | Ví tiền (Ví chính, Tiền mặt, Ngân hàng...) |
| 3 | `categories` | Room + Supabase | AI Chat, Budget | Danh mục giao dịch (Ăn uống, Di chuyển...) |
| 4 | `budgets` | Room + Supabase | Budget | Ngân sách hàng tháng theo danh mục |
| 5 | `saving_goals` | Room + Supabase | Saving | Mục tiêu tiết kiệm |
| 6 | `saving_contributions` | Room + Supabase | Saving | Lịch sử nạp/rút tiền tiết kiệm |
| 7 | `chat_messages` | Room | AI Chat | Lịch sử chat với AI |
| 8 | `recurring_transactions` | Room + Supabase | AI Chat | Giao dịch định kỳ (lương, tiền nhà...) |
| 9 | `profiles` | Supabase only | Other/Settings | Thông tin người dùng (auth) |

---

## 4. Chi tiết từng bảng

### 4.1 `transactions` ✅ (Đã có)

> Giao dịch tài chính — bảng cốt lõi của app.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | `Long` | PK, auto-increment | Mã giao dịch |
| `user_id` | `String` | NOT NULL | ID người dùng (Supabase auth.uid). Dùng để phân biệt giao dịch của ai |
| `note` | `String` | NOT NULL | Ghi chú (VD: "Bữa tối") |
| `amount` | `Double` | NOT NULL | Số tiền (VNĐ). Luôn dương |
| `type` | `String` | NOT NULL, DEFAULT "expense" | `"income"` hoặc `"expense"` |
| `category` | `String` | NOT NULL, DEFAULT "Khác" | Tên danh mục |
| `timestamp` | `Long` | NOT NULL | Thời điểm tạo (epoch millis) |

> ⚠️ **Tại sao cần `user_id` ở local (Room)?**
> - Khi user đăng xuất rồi đăng nhập tài khoản khác trên cùng thiết bị → dữ liệu không bị lẫn.
> - Tất cả query cần filter theo `user_id` hiện tại: `WHERE user_id = :currentUserId`.
> - Giá trị `user_id` lấy từ Supabase `auth.uid()` sau khi đăng nhập, lưu vào DataStore key `SUPABASE_USER_ID`.

**Đề xuất thêm cột mới:**

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `wallet_id` | `Long` | FK → wallets.id, DEFAULT 1 | Ví thực hiện giao dịch |
| `category_id` | `Long?` | FK → categories.id, nullable | Liên kết danh mục (thay thế cột `category` text) |
| `recurring_id` | `Long?` | FK → recurring_transactions.id, nullable | Giao dịch sinh từ recurring nào |
| `ai_generated` | `Boolean` | DEFAULT false | Giao dịch do AI tạo hay thủ công |
| `date` | `Long` | NOT NULL | Ngày giao dịch (epoch millis, 00:00 của ngày) — tách khỏi timestamp |
| `supabase_id` | `String?` | UNIQUE, nullable | UUID đồng bộ với Supabase |

---

### 4.2 `wallets` 🆕

> Ví tiền — mỗi người dùng có thể có nhiều ví.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | `Long` | PK, auto-increment | Mã ví |
| `user_id` | `String` | NOT NULL | ID người dùng (Supabase auth.uid) |
| `name` | `String` | NOT NULL | Tên ví (VD: "Ví chính", "Tiền mặt") |
| `balance` | `Double` | NOT NULL, DEFAULT 0.0 | Số dư hiện tại |
| `icon` | `String` | NOT NULL, DEFAULT "wallet" | Tên icon (material icon name) |
| `color` | `String` | NOT NULL, DEFAULT "#4CAF50" | Mã màu hex |
| `is_default` | `Boolean` | NOT NULL, DEFAULT false | Ví mặc định |
| `is_archived` | `Boolean` | NOT NULL, DEFAULT false | Đã ẩn/lưu trữ |
| `created_at` | `Long` | NOT NULL | Ngày tạo (epoch millis) |
| `updated_at` | `Long` | NOT NULL | Lần cập nhật cuối |
| `supabase_id` | `String?` | UNIQUE, nullable | UUID đồng bộ Supabase |

**Dữ liệu mặc định (seed):**

| id | name | balance | icon | color | is_default |
|----|------|---------|------|-------|------------|
| 1 | Ví chính | 0.0 | wallet | #4CAF50 | true |

**Quy tắc nghiệp vụ:**
- Luôn có ít nhất 1 ví (không được xoá ví cuối cùng)
- Khi thêm giao dịch expense → trừ balance; income → cộng balance
- Di chuyển quỹ = 1 expense ở ví nguồn + 1 income ở ví đích

---

### 4.3 `categories` 🆕

> Danh mục giao dịch — phân loại chi tiêu/thu nhập.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | `Long` | PK, auto-increment | Mã danh mục |
| `user_id` | `String?` | nullable | ID người dùng. null = danh mục hệ thống (dùng chung) |
| `name` | `String` | NOT NULL, UNIQUE | Tên (VD: "Ăn uống") |
| `icon` | `String` | NOT NULL | Tên icon |
| `color` | `String` | NOT NULL | Mã màu hex |
| `type` | `String` | NOT NULL | `"expense"`, `"income"`, hoặc `"both"` |
| `is_system` | `Boolean` | NOT NULL, DEFAULT false | Danh mục hệ thống (không được xoá) |
| `is_archived` | `Boolean` | NOT NULL, DEFAULT false | Đã ẩn |
| `sort_order` | `Int` | NOT NULL, DEFAULT 0 | Thứ tự hiển thị |
| `created_at` | `Long` | NOT NULL | Ngày tạo |

**Dữ liệu mặc định (seed) — Chi tiêu:**

| name | icon | color | type | is_system |
|------|------|-------|------|-----------|
| Ăn uống | restaurant | #FF5722 | expense | true |
| Di chuyển | directions_car | #2196F3 | expense | true |
| Mua sắm | shopping_bag | #E91E63 | expense | true |
| Giải trí | movie | #9C27B0 | expense | true |
| Sức khoẻ | medical_services | #F44336 | expense | true |
| Giáo dục | school | #3F51B5 | expense | true |
| Hoá đơn | receipt_long | #FF9800 | expense | true |
| Tiền nhà | home | #795548 | expense | true |
| Khác | more_horiz | #607D8B | both | true |

**Dữ liệu mặc định (seed) — Thu nhập:**

| name | icon | color | type | is_system |
|------|------|-------|------|-----------|
| Lương | payments | #4CAF50 | income | true |
| Thưởng | card_giftcard | #8BC34A | income | true |
| Đầu tư | trending_up | #00BCD4 | income | true |
| Bán hàng | storefront | #CDDC39 | income | true |
| Khác (Thu) | add_circle | #009688 | income | true |

---

### 4.4 `budgets` 🆕

> Ngân sách hàng tháng — đặt hạn mức chi tiêu cho từng danh mục.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | `Long` | PK, auto-increment | Mã ngân sách |
| `user_id` | `String` | NOT NULL | ID người dùng (Supabase auth.uid) |
| `category_id` | `Long` | FK → categories.id, NOT NULL | Danh mục áp dụng |
| `amount_limit` | `Double` | NOT NULL | Hạn mức (VNĐ) |
| `spent` | `Double` | NOT NULL, DEFAULT 0.0 | Đã chi (tính toán hoặc cache) |
| `month` | `Int` | NOT NULL | Tháng (1-12) |
| `year` | `Int` | NOT NULL | Năm (VD: 2026) |
| `is_active` | `Boolean` | NOT NULL, DEFAULT true | Đang áp dụng |
| `created_at` | `Long` | NOT NULL | Ngày tạo |
| `supabase_id` | `String?` | UNIQUE, nullable | UUID đồng bộ |

**Index:** `UNIQUE(category_id, month, year)` — mỗi danh mục chỉ có 1 budget/tháng.

**Quy tắc nghiệp vụ:**
- `spent` = SUM(transactions.amount) WHERE category_id = X AND month/year khớp
- Có thể tính realtime từ query hoặc cache trong cột `spent`
- Hiển thị thanh tiến trình: `spent / amount_limit * 100%`
- Cảnh báo khi `spent >= 80%` của `amount_limit`

---

### 4.5 `saving_goals` 🆕

> Mục tiêu tiết kiệm — VD: "Mua laptop", "Du lịch Đà Lạt".

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | `Long` | PK, auto-increment | Mã mục tiêu |
| `user_id` | `String` | NOT NULL | ID người dùng (Supabase auth.uid) |
| `name` | `String` | NOT NULL | Tên (VD: "Mua laptop") |
| `target_amount` | `Double` | NOT NULL | Số tiền cần đạt |
| `current_amount` | `Double` | NOT NULL, DEFAULT 0.0 | Đã tiết kiệm được |
| `icon` | `String` | NOT NULL, DEFAULT "savings" | Icon mục tiêu |
| `color` | `String` | NOT NULL, DEFAULT "#4CAF50" | Mã màu |
| `deadline` | `Long?` | nullable | Hạn chót (epoch millis), null = không hạn |
| `is_completed` | `Boolean` | NOT NULL, DEFAULT false | Đã đạt mục tiêu |
| `is_archived` | `Boolean` | NOT NULL, DEFAULT false | Đã lưu trữ |
| `created_at` | `Long` | NOT NULL | Ngày tạo |
| `updated_at` | `Long` | NOT NULL | Lần cập nhật cuối |
| `supabase_id` | `String?` | UNIQUE, nullable | UUID đồng bộ |

**Quy tắc nghiệp vụ:**
- `current_amount` = SUM(saving_contributions.amount) WHERE goal_id = X
- Tự động set `is_completed = true` khi `current_amount >= target_amount`
- Hiển thị tiến trình: `current_amount / target_amount * 100%`

---

### 4.6 `saving_contributions` 🆕

> Lịch sử nạp/rút tiền cho mục tiêu tiết kiệm.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | `Long` | PK, auto-increment | Mã giao dịch tiết kiệm |
| `goal_id` | `Long` | FK → saving_goals.id, NOT NULL | Mục tiêu liên quan |
| `amount` | `Double` | NOT NULL | Số tiền (dương = nạp, âm = rút) |
| `note` | `String?` | nullable | Ghi chú (VD: "Lương tháng 3") |
| `timestamp` | `Long` | NOT NULL | Thời điểm giao dịch |
| `supabase_id` | `String?` | UNIQUE, nullable | UUID đồng bộ |

**Quy tắc nghiệp vụ:**
- Nạp tiền tiết kiệm = tạo 1 expense transaction ở ví + 1 contribution dương
- Rút tiền = tạo 1 income transaction ở ví + 1 contribution âm

---

### 4.7 `chat_messages` 🆕

> Lịch sử chat giữa người dùng và AI.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | `Long` | PK, auto-increment | Mã tin nhắn |
| `user_id` | `String` | NOT NULL | ID người dùng (Supabase auth.uid) |
| `content` | `String` | NOT NULL | Nội dung tin nhắn |
| `sender` | `String` | NOT NULL | `"user"` hoặc `"ai"` |
| `session_id` | `String` | NOT NULL | ID phiên chat (group theo ngày/48h) |
| `transaction_id` | `Long?` | FK → transactions.id, nullable | Giao dịch được tạo từ tin nhắn này |
| `timestamp` | `Long` | NOT NULL | Thời điểm gửi |

**Index:** `INDEX(session_id, timestamp)` — query nhanh theo phiên.

**Quy tắc nghiệp vụ:**
- Mỗi phiên chat kéo dài 48 giờ (theo title hiện tại của app)
- Khi AI phản hồi + tạo giao dịch → lưu `transaction_id` để liên kết
- Xoá tin nhắn cũ hơn 7 ngày (tuỳ chọn, tiết kiệm storage)

---

### 4.8 `recurring_transactions` 🆕

> Giao dịch định kỳ — lặp lại tự động (lương, tiền nhà, Netflix...).

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | `Long` | PK, auto-increment | Mã giao dịch định kỳ |
| `user_id` | `String` | NOT NULL | ID người dùng (Supabase auth.uid) |
| `note` | `String` | NOT NULL | Mô tả (VD: "Lương tháng") |
| `amount` | `Double` | NOT NULL | Số tiền |
| `type` | `String` | NOT NULL | `"income"` hoặc `"expense"` |
| `category_id` | `Long` | FK → categories.id | Danh mục |
| `wallet_id` | `Long` | FK → wallets.id | Ví thực hiện |
| `frequency` | `String` | NOT NULL | `"daily"`, `"weekly"`, `"monthly"`, `"yearly"` |
| `day_of_month` | `Int?` | nullable | Ngày trong tháng (1-31), dùng cho monthly |
| `day_of_week` | `Int?` | nullable | Thứ trong tuần (1=Mon, 7=Sun), dùng cho weekly |
| `start_date` | `Long` | NOT NULL | Ngày bắt đầu |
| `end_date` | `Long?` | nullable | Ngày kết thúc (null = vô hạn) |
| `next_due_date` | `Long` | NOT NULL | Ngày thực hiện tiếp theo |
| `is_active` | `Boolean` | NOT NULL, DEFAULT true | Đang hoạt động |
| `created_at` | `Long` | NOT NULL | Ngày tạo |
| `supabase_id` | `String?` | UNIQUE, nullable | UUID đồng bộ |

**Quy tắc nghiệp vụ:**
- App check `next_due_date` mỗi khi mở → tự động tạo transaction nếu đến hạn
- Sau khi tạo → cập nhật `next_due_date` theo `frequency`
- Hiển thị danh sách recurring trên UI để người dùng quản lý

---

## 5. Quan hệ giữa các bảng (ERD)

```
┌─────────────┐       ┌──────────────┐       ┌─────────────────────┐
│   wallets    │       │  categories  │       │ recurring_           │
│─────────────│       │──────────────│       │   transactions      │
│ id (PK)     │◄──┐   │ id (PK)     │◄──┐   │─────────────────────│
│ name        │   │   │ name        │   │   │ id (PK)             │
│ balance     │   │   │ icon        │   │   │ category_id (FK) ───┤──► categories
│ ...         │   │   │ type        │   │   │ wallet_id (FK) ─────┤──► wallets
└─────────────┘   │   │ ...         │   │   │ frequency           │
                  │   └──────────────┘   │   │ next_due_date       │
                  │                      │   └──────────┬──────────┘
                  │                      │              │
                  │   ┌──────────────────┤              │ generates
                  │   │                  │              ▼
                  │   │   ┌──────────────┴──────────────────┐
                  │   │   │         transactions             │
                  │   │   │──────────────────────────────────│
                  └───┤   │ id (PK)                          │
                      │   │ wallet_id (FK) ──► wallets       │
                      │   │ category_id (FK) ──► categories  │
                      │   │ recurring_id (FK) ──► recurring  │
                      │   │ note                             │
                      │   │ amount                           │
                      │   │ type                             │
                      │   │ timestamp                        │
                      │   └──────────────┬──────────────────┘
                      │                  │
                      │                  │ linked
                      │                  ▼
                      │   ┌──────────────────────────┐
                      │   │     chat_messages         │
                      │   │──────────────────────────│
                      │   │ id (PK)                  │
                      │   │ transaction_id (FK)      │
                      │   │ content                  │
                      │   │ sender                   │
                      │   │ session_id               │
                      │   └──────────────────────────┘
                      │
                      │   ┌──────────────────────────┐
                      │   │     budgets               │
                      │   │──────────────────────────│
                      └──►│ category_id (FK)         │
                          │ amount_limit             │
                          │ month, year              │
                          └──────────────────────────┘

┌─────────────────┐       ┌──────────────────────────┐
│  saving_goals   │       │ saving_contributions     │
│─────────────────│       │──────────────────────────│
│ id (PK)         │◄──────│ goal_id (FK)             │
│ name            │       │ amount                   │
│ target_amount   │       │ note                     │
│ current_amount  │       │ timestamp                │
│ deadline        │       └──────────────────────────┘
└─────────────────┘
```

---

## 6. Supabase Tables (Remote)

> Dùng để đồng bộ, backup, và auth. Cấu trúc tương tự Room nhưng thêm `user_id`.

### 6.1 `profiles` (Supabase Auth)

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | `UUID` | PK (= auth.users.id) | Mã người dùng |
| `email` | `String` | UNIQUE, NOT NULL | Email đăng nhập |
| `display_name` | `String?` | nullable | Tên hiển thị |
| `currency` | `String` | DEFAULT "VND" | Đơn vị tiền tệ |
| `language` | `String` | DEFAULT "vi" | Ngôn ngữ |
| `created_at` | `Timestamp` | auto | Ngày đăng ký |
| `updated_at` | `Timestamp` | auto | Lần cập nhật cuối |

### 6.2 Các bảng remote khác

Mỗi bảng remote có thêm:

| Cột bổ sung | Kiểu | Mô tả |
|-------------|------|-------|
| `id` | `UUID` | PK (thay vì Long auto-increment) |
| `user_id` | `UUID` | FK → profiles.id (RLS filter) |
| `created_at` | `Timestamp` | Server-generated |
| `updated_at` | `Timestamp` | Server-generated |
| `is_deleted` | `Boolean` | Soft delete (đồng bộ xoá) |

**Bảng Supabase cần tạo:**
- `wallets` (remote)
- `categories` (remote)
- `transactions` (remote)
- `budgets` (remote)
- `saving_goals` (remote)
- `saving_contributions` (remote)
- `recurring_transactions` (remote)

> **Lưu ý:** Bật **RLS (Row Level Security)** cho tất cả bảng.  
> Policy: `user_id = auth.uid()` — mỗi user chỉ thấy data của mình.

---

## 7. DataStore Preferences (Key-Value)

> Không dùng Room cho các giá trị đơn giản — dùng DataStore.

| Key | Kiểu | Mặc định | Mô tả |
|-----|------|----------|-------|
| `IS_ONBOARDING_COMPLETED` | Boolean | false | ✅ Đã tạo. Đã xem onboarding chưa |
| `SELECTED_WALLET_ID` | Long | 1 | Ví đang chọn (hiện trên AI Chat) |
| `IS_DARK_MODE` | Boolean | false | Chế độ tối |
| `NOTIFICATION_ENABLED` | Boolean | true | Bật thông báo |
| `DAILY_REMINDER_TIME` | String | "20:00" | Giờ nhắc nhở ghi chép |
| `CURRENCY_SYMBOL` | String | "đ" | Ký hiệu tiền tệ |
| `LAST_SYNC_TIMESTAMP` | Long | 0 | Lần đồng bộ Supabase cuối |
| `SUPABASE_USER_ID` | String? | null | UUID user sau khi đăng nhập |

---

## 8. Chiến lược đồng bộ Local ↔ Remote

### Offline-first

```
Người dùng thao tác
       │
       ▼
  Lưu vào Room (ngay lập tức)
       │
       ▼
  Đánh dấu "pending_sync = true"
       │
       ▼
  Khi có mạng → Sync Worker chạy
       │
       ├─► Push local changes → Supabase
       │
       └─► Pull remote changes → Room
```

### Xung đột (Conflict Resolution)

| Tình huống | Giải pháp |
|------------|-----------|
| Cùng record sửa cả 2 phía | `updated_at` mới hơn thắng |
| Local xoá, remote sửa | Remote thắng (khôi phục) |
| Remote xoá, local sửa | Remote thắng (xoá) |
| Tạo mới cả 2 phía | Giữ cả 2 (UUID khác nhau) |

### Cột đồng bộ cần thêm vào Room entities

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `supabase_id` | String? | UUID trên Supabase |
| `pending_sync` | Boolean | Chưa đồng bộ lên server |
| `is_deleted` | Boolean | Soft delete (chờ sync xoá) |
| `updated_at` | Long | Timestamp cập nhật cuối |

---

## 9. Thứ tự triển khai

Ưu tiên theo tính năng của app và dependency giữa các bảng:

### Phase 1 — Nền tảng (Hiện tại → Sprint 1)
```
✅ transactions (đã có)
🔲 wallets          → Home hiển thị số dư
🔲 categories       → AI parse danh mục chính xác
```

### Phase 2 — Tính năng chính (Sprint 2)
```
🔲 chat_messages    → Lưu lịch sử chat AI
🔲 budgets          → Màn hình Ngân sách hoạt động
```

### Phase 3 — Tiết kiệm (Sprint 3)
```
🔲 saving_goals         → Màn hình Tiết kiệm
🔲 saving_contributions → Nạp/rút tiền tiết kiệm
```

### Phase 4 — Nâng cao (Sprint 4)
```
🔲 recurring_transactions → Giao dịch tự động
🔲 profiles (Supabase)    → Đăng nhập, đồng bộ
🔲 Sync mechanism          → Offline-first sync
```

---

## 10. Migration Guide

### Khi thêm bảng mới vào Room

```kotlin
// 1. Tạo Entity
@Entity(tableName = "wallets")
data class WalletEntity(...)

// 2. Tạo DAO
@Dao
interface WalletDao { ... }

// 3. Cập nhật AppDatabase
@Database(
    entities = [
        TransactionEntity::class,
        WalletEntity::class,       // ← thêm
        CategoryEntity::class,     // ← thêm
        // ...
    ],
    version = 2,                   // ← tăng version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao       // ← thêm
    abstract fun categoryDao(): CategoryDao   // ← thêm
}

// 4. Tạo Migration
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS wallets (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                balance REAL NOT NULL DEFAULT 0.0,
                icon TEXT NOT NULL DEFAULT 'wallet',
                color TEXT NOT NULL DEFAULT '#4CAF50',
                is_default INTEGER NOT NULL DEFAULT 0,
                is_archived INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                supabase_id TEXT
            )
        """)
        // Insert default wallet
        db.execSQL("""
            INSERT INTO wallets (name, balance, is_default, created_at, updated_at)
            VALUES ('Ví chính', 0.0, 1, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """)
        
        // Thêm cột wallet_id vào transactions
        db.execSQL("ALTER TABLE transactions ADD COLUMN wallet_id INTEGER NOT NULL DEFAULT 1")
    }
}

// 5. Áp dụng migration trong AppDatabase.getInstance()
Room.databaseBuilder(context, AppDatabase::class.java, "my_money.db")
    .addMigrations(MIGRATION_1_2)
    .build()
```

### Khi thêm bảng trên Supabase

```sql
-- Tạo bảng wallets trên Supabase
CREATE TABLE wallets (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    name TEXT NOT NULL,
    balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    icon TEXT NOT NULL DEFAULT 'wallet',
    color TEXT NOT NULL DEFAULT '#4CAF50',
    is_default BOOLEAN NOT NULL DEFAULT false,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Bật RLS
ALTER TABLE wallets ENABLE ROW LEVEL SECURITY;

-- Policy: user chỉ thấy data của mình
CREATE POLICY "Users can CRUD own wallets"
ON wallets FOR ALL
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);
```

---

## Mapping: Bảng → Layer files cần tạo

| Bảng | Domain Model | Room Entity | DAO | Repository Interface | Repository Impl | Use Cases |
|------|-------------|-------------|-----|---------------------|-----------------|-----------|
| `wallets` | `WalletModel.kt` | `WalletEntity.kt` | `WalletDao.kt` | `WalletRepository.kt` | `WalletRepositoryImpl.kt` | Get, Add, Update, Delete |
| `categories` | `CategoryModel.kt` | `CategoryEntity.kt` | `CategoryDao.kt` | `CategoryRepository.kt` | `CategoryRepositoryImpl.kt` | Get, Add, Update, Delete |
| `budgets` | `BudgetModel.kt` | `BudgetEntity.kt` | `BudgetDao.kt` | `BudgetRepository.kt` | `BudgetRepositoryImpl.kt` | Get, Add, Update, Delete, GetByMonth |
| `saving_goals` | `SavingGoalModel.kt` | `SavingGoalEntity.kt` | `SavingGoalDao.kt` | `SavingGoalRepository.kt` | `SavingGoalRepositoryImpl.kt` | Get, Add, Update, Delete, Contribute |
| `saving_contributions` | `ContributionModel.kt` | `ContributionEntity.kt` | `ContributionDao.kt` | *(trong SavingGoalRepository)* | *(trong SavingGoalRepositoryImpl)* | *(trong SavingGoal use cases)* |
| `chat_messages` | `ChatMessageModel.kt` | `ChatMessageEntity.kt` | `ChatMessageDao.kt` | `ChatRepository.kt` | `ChatRepositoryImpl.kt` | GetMessages, SaveMessage, ClearSession |
| `recurring_transactions` | `RecurringModel.kt` | `RecurringEntity.kt` | `RecurringDao.kt` | `RecurringRepository.kt` | `RecurringRepositoryImpl.kt` | Get, Add, Update, Delete, ProcessDue |
