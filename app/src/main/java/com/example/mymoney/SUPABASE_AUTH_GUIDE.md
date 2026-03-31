# 🔐 Supabase Auth Integration Guide — MyMoney App

> Hướng dẫn chi tiết cách tích hợp Supabase Authentication vào dự án MyMoney.
> Cập nhật: March 31, 2026

---

## 📋 Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Cài đặt Supabase Dashboard](#2-cài-đặt-supabase-dashboard)
3. [Dependencies đã cài](#3-dependencies-đã-cài)
4. [Cấu trúc files](#4-cấu-trúc-files)
5. [Luồng hoạt động chi tiết](#5-luồng-hoạt-động-chi-tiết)
6. [API Reference — AuthRepository](#6-api-reference--authrepository)
7. [Cách Supabase Auth hoạt động](#7-cách-supabase-auth-hoạt-động)
8. [Cấu hình Supabase Dashboard](#8-cấu-hình-supabase-dashboard)
9. [Xử lý lỗi phổ biến](#9-xử-lý-lỗi-phổ-biến)
10. [Tích hợp Social Login (Google/Facebook)](#10-tích-hợp-social-login-googlefacebook)
11. [Bảo mật & Best Practices](#11-bảo-mật--best-practices)
12. [Checklist triển khai](#12-checklist-triển-khai)

---

## 1. Tổng quan kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                        UI LAYER                                  │
│  ┌─────────────┐  ┌──────────────┐                               │
│  │ SignInScreen │  │ SignUpScreen  │                               │
│  │  (Compose)  │  │  (Compose)   │                               │
│  └──────┬──────┘  └──────┬───────┘                               │
│         │   AuthEvent     │                                      │
│         ▼                 ▼                                      │
│  ┌──────────────────────────────┐                                │
│  │        AuthViewModel         │  ← UDF: Event → State → UI    │
│  │  • handleSignIn()            │                                │
│  │  • handleSignUp()            │                                │
│  │  • handleForgotPassword()    │                                │
│  │  • mapAuthError()            │                                │
│  └──────────┬───────────────────┘                                │
│             │                                                    │
├─────────────┼────────────────────────────────────────────────────┤
│  DOMAIN     │  AuthRepository (interface)                        │
│             │    • signInWithEmail()                              │
│             │    • signUpWithEmail()                              │
│             │    • resetPassword()                                │
│             │    • signOut()                                      │
│             │    • getCurrentUserId()                             │
├─────────────┼────────────────────────────────────────────────────┤
│  DATA       │  AuthRepositoryImpl                                │
│             │    └── SupabaseClient.client.auth                  │
│             │         • signInWith(Email) { ... }                │
│             │         • signUpWith(Email) { ... }                │
│             │         • resetPasswordForEmail()                  │
│             │         • signOut()                                │
│             │         • currentUserOrNull()                      │
│             │                                                    │
│             │  SettingPreferences (DataStore)                     │
│             │    • saveUserId(userId)                             │
│             │    • currentUserId (Flow<String?>)                  │
│             │    • clearUserId()                                  │
└─────────────┴────────────────────────────────────────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │   Supabase Cloud      │
              │   ┌─────────────────┐ │
              │   │  Auth (GoTrue)  │ │  ← Email/Password auth
              │   │  • auth.users   │ │
              │   │  • sessions     │ │
              │   └─────────────────┘ │
              │   ┌─────────────────┐ │
              │   │  PostgREST      │ │  ← Database API
              │   │  • profiles     │ │
              │   │  • transactions │ │
              │   └─────────────────┘ │
              └───────────────────────┘
```

---

## 2. Cài đặt Supabase Dashboard

### 2.1 Tắt Email Confirmation (Dev Mode)

Mặc định Supabase bật "Confirm email" — user phải click link trong email mới đăng nhập được.
**Trong giai đoạn dev**, nên **TẮT** để test nhanh:

1. Vào [Supabase Dashboard](https://supabase.com/dashboard) → chọn project
2. **Authentication** → **Providers** → **Email**
3. Tắt **"Confirm email"** → Save
4. (Tuỳ chọn) Tắt **"Double confirm email changes"** nếu không cần

> ⚠️ **Lưu ý:** Khi deploy production, **BẬT LẠI** email confirmation để bảo mật.

### 2.2 Cấu hình Password Policy

1. **Authentication** → **Providers** → **Email**
2. Minimum password length: **6** (hoặc tuỳ chọn)
3. Password regex (tuỳ chọn): yêu cầu chữ hoa + số + ký tự đặc biệt

### 2.3 Tạo bảng `profiles` (tuỳ chọn)

Nếu muốn lưu thông tin bổ sung (username, avatar...) ngoài `auth.users`:

```sql
-- Chạy trong Supabase SQL Editor
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT,
    display_name TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Bật Row Level Security
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Policy: user chỉ đọc/sửa profile của chính mình
CREATE POLICY "Users can view own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

CREATE POLICY "Users can insert own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- Tự động tạo profile khi user mới đăng ký
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, username, display_name)
    VALUES (
        NEW.id,
        NEW.raw_user_meta_data->>'username',
        NEW.raw_user_meta_data->>'display_name'
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger: sau khi user mới được tạo trong auth.users → tạo profile
CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();
```

---

## 3. Dependencies đã cài

Trong `gradle/libs.versions.toml`:

```toml
[versions]
supabase = "2.5.0"
ktor = "2.3.0"
kotlinSerialization = "1.9.0"

[libraries]
supabase-postgrest = { group = "io.github.jan-tennert.supabase", name = "postgrest-kt", version.ref = "supabase" }
supabase-gotrue = { group = "io.github.jan-tennert.supabase", name = "gotrue-kt", version.ref = "supabase" }
ktor-client-android = { group = "io.ktor", name = "ktor-client-android", version.ref = "ktor" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlinSerialization" }
```

Trong `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.supabase.postgrest)  // Database queries
    implementation(libs.supabase.gotrue)     // Authentication
    implementation(libs.ktor.client.android) // HTTP engine
}
```

Trong `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 4. Cấu trúc files

```
app/src/main/java/com/example/mymoney/
│
├── data/
│   ├── remote/
│   │   └── SupabaseClient.kt              ← Singleton client (URL + Key)
│   ├── repository/
│   │   └── AuthRepositoryImpl.kt          ← 🆕 Gọi Supabase Auth API
│   └── local/
│       └── datastore/
│           └── SettingPreferences.kt       ← 🔄 Thêm saveUserId / currentUserId
│
├── domain/
│   └── repository/
│       └── AuthRepository.kt              ← 🆕 Interface (pure Kotlin)
│
├── presentation/
│   └── viewmodel/
│       └── auth/
│           ├── AuthViewModel.kt            ← 🔄 Tích hợp Supabase Auth
│           └── auth/
│               └── AuthContract.kt         ← 🔄 Thêm successMessage
│
├── ui/
│   ├── auth/
│   │   ├── SignInScreen.kt                 ← 🔄 Thêm successMessage UI
│   │   ├── SignUpScreen.kt                 ← Giữ nguyên
│   │   └── components/
│   │       ├── AuthTextField.kt            ← Giữ nguyên
│   │       └── SocialLoginSection.kt       ← Giữ nguyên
│   └── navigation/
│       ├── Screen.kt                       ← Có SignIn + SignUp routes
│       └── AppNavigation.kt               ← Có auth routes
│
└── MainActivity.kt                         ← 🔄 Check auth session
```

---

## 5. Luồng hoạt động chi tiết

### 5.1 App khởi động (Cold Start)

```
MainActivity.onCreate()
       │
       ▼
  DataStore đọc 2 giá trị:
  ┌──────────────────────────────┐
  │ isOnboardingCompleted: Bool  │
  │ currentUserId: String?       │
  └──────────────┬───────────────┘
                 │
       ┌─────────┼─────────────────┐
       ▼         ▼                 ▼
   Chưa xem   Đã xem +          Đã xem +
   onboarding  chưa login        đã login
       │         │                 │
       ▼         ▼                 ▼
   Onboarding  SignIn            Main
   Screen      Screen            Screen
```

### 5.2 Sign In Flow

```
User nhập email + password
       │
       ▼ AuthEvent.OnSignInClicked
       │
  AuthViewModel.handleSignIn()
       │
  ┌────┤ Validation OK?
  │ NO → update errorMessage → UI hiện lỗi
  │
  │ YES
  ▼
  authRepository.signInWithEmail(email, password)
       │
       ▼ SupabaseClient.client.auth.signInWith(Email) { ... }
       │
  ┌────┤ Supabase Response
  │
  │ SUCCESS (session returned)
  │    │
  │    ▼ settingPreferences.saveUserId(userId)
  │    │
  │    ▼ emit NavigateToMain
  │    │
  │    ▼ UI navigate → MainScreen
  │
  │ ERROR (wrong credentials / network)
  │    │
  │    ▼ mapAuthError(exception) → Vietnamese message
  │    │
  │    ▼ update errorMessage → UI hiện lỗi
  └────┘
```

### 5.3 Sign Up Flow

```
User nhập username + email + password + confirm + agree terms
       │
       ▼ AuthEvent.OnSignUpClicked
       │
  AuthViewModel.handleSignUp()
       │
  ┌────┤ Validation OK?
  │ NO → update errorMessage
  │
  │ YES
  ▼
  authRepository.signUpWithEmail(email, password, username)
       │
       ▼ SupabaseClient.client.auth.signUpWith(Email) {
       │     this.email = ...
       │     this.password = ...
       │     this.data = { "username": "...", "display_name": "..." }
       │ }
       │
  ┌────┤ Supabase Response
  │
  │ SUCCESS
  │    │
  │    ├── Supabase tạo user trong auth.users
  │    ├── Trigger tự động tạo profile trong public.profiles
  │    ├── Session trả về ngay (nếu tắt email confirm)
  │    │
  │    ▼ saveUserId → emit NavigateToMain
  │
  │ ERROR
  │    ▼ mapAuthError → UI hiện lỗi
  └────┘
```

---

## 6. API Reference — AuthRepository

### Interface (Domain layer)

```kotlin
interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): String
    suspend fun signUpWithEmail(email: String, password: String, username: String): String
    suspend fun resetPassword(email: String)
    suspend fun signOut()
    suspend fun getCurrentUserId(): String?
}
```

### Implementation (Data layer)

| Method | Supabase API Call | Trả về |
|--------|-------------------|--------|
| `signInWithEmail()` | `auth.signInWith(Email) { ... }` | userId (UUID) |
| `signUpWithEmail()` | `auth.signUpWith(Email) { ... }` | userId (UUID) |
| `resetPassword()` | `auth.resetPasswordForEmail(email)` | void |
| `signOut()` | `auth.signOut()` | void |
| `getCurrentUserId()` | `auth.currentUserOrNull()?.id` | userId or null |

---

## 7. Cách Supabase Auth hoạt động

### 7.1 Khi `signUpWith(Email)` được gọi

```
Client (App)                    Supabase Cloud
    │                                │
    │  POST /auth/v1/signup          │
    │  { email, password, data }     │
    │ ──────────────────────────────►│
    │                                │
    │                                ├── Tạo user trong auth.users
    │                                ├── Hash password (bcrypt)
    │                                ├── Lưu user_metadata = data
    │                                ├── Trigger → tạo profile
    │                                ├── Tạo session (JWT access + refresh token)
    │                                │
    │  ◄──────────────────────────── │
    │  200 OK { user, session }      │
    │                                │
    │  SDK tự lưu session vào bộ nhớ │
    │  (auto-refresh khi token hết)  │
```

### 7.2 Session & Token Management

- **Access token**: JWT, hết hạn sau 1 giờ (mặc định)
- **Refresh token**: Dùng để lấy access token mới
- **Supabase SDK tự động**:
  - Lưu session trong bộ nhớ
  - Refresh token khi sắp hết hạn
  - Attach access token vào mọi API request (PostgREST, Storage...)

### 7.3 Tại sao lưu userId vào DataStore?

SDK Supabase quản lý session **trong RAM** — khi app bị kill hoặc restart,
session có thể mất. Lưu userId vào DataStore để:

1. **Kiểm tra nhanh** lúc cold start (DataStore đọc ngay, không cần network)
2. **Filter dữ liệu local** (Room) theo user: `WHERE user_id = :userId`
3. **Fallback** khi Supabase session chưa sẵn sàng

---

## 8. Cấu hình Supabase Dashboard

### 8.1 Lấy URL và Key

1. Vào [Supabase Dashboard](https://supabase.com/dashboard)
2. Chọn project → **Settings** → **API**
3. Copy:
   - **Project URL**: `https://xxxxx.supabase.co`
   - **anon/public key**: `eyJhbGci...` (key này an toàn để embed trong app)

### 8.2 Cập nhật trong code

File `data/remote/SupabaseClient.kt`:

```kotlin
object SupabaseClient {
    private const val SUPABASE_URL = "https://your-project-url.supabase.co"
    private const val SUPABASE_KEY = "your-anon-key"
    // ...
}
```

> ⚠️ **anon key** là public key — chỉ cho phép truy cập data được RLS cho phép.
> **KHÔNG BAO GIỜ** dùng `service_role key` trong app client.

---

## 9. Xử lý lỗi phổ biến

### 9.1 Error Messages từ Supabase

| Supabase Error | Nguyên nhân | Cách xử lý |
|----------------|-------------|-------------|
| `Invalid login credentials` | Sai email hoặc password | Hiện "Email hoặc mật khẩu không đúng" |
| `User already registered` | Email đã tồn tại | Hiện "Email đã được đăng ký" |
| `Email not confirmed` | Chưa confirm email | Hiện "Kiểm tra hộp thư" |
| `Signup is disabled` | Admin tắt đăng ký | Hiện "Liên hệ quản trị viên" |
| `Rate limit exceeded` | Quá nhiều request | Hiện "Đợi một lúc rồi thử lại" |
| Network error | Mất mạng / timeout | Hiện "Kiểm tra kết nối mạng" |

### 9.2 Debug Tips

```kotlin
// Trong AuthRepositoryImpl, thêm log để debug:
try {
    supabase.auth.signInWith(Email) { ... }
} catch (e: Exception) {
    Log.e("Auth", "Sign in failed: ${e.message}", e)
    throw e
}
```

### 9.3 Lỗi thường gặp khi dev

| Vấn đề | Nguyên nhân | Giải pháp |
|--------|-------------|-----------|
| `Unable to resolve host` | Thiếu INTERNET permission | Thêm vào AndroidManifest.xml |
| `401 Unauthorized` | Sai anon key | Kiểm tra SUPABASE_KEY |
| Sign up thành công nhưng null userId | Email confirm bật | Tắt confirm trong Dashboard |
| App crash khi cold start | DataStore chưa đọc xong | Check `null`/`"loading"` trước |

---

## 10. Tích hợp Social Login (Google/Facebook)

### 10.1 Google Sign-In (chưa triển khai)

**Bước 1:** Thêm dependency

```kotlin
// build.gradle.kts
implementation("io.github.jan-tennert.supabase:compose-auth:2.5.0")
implementation("io.github.jan-tennert.supabase:compose-auth-ui:2.5.0")
implementation("androidx.credentials:credentials:1.3.0")
implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
```

**Bước 2:** Cấu hình Supabase Dashboard
- Authentication → Providers → Google → Enable
- Nhập Google OAuth Client ID + Secret

**Bước 3:** Cấu hình Google Cloud Console
- Tạo OAuth 2.0 Client ID (Android type)
- Thêm SHA-1 fingerprint
- Redirect URI: `https://your-project.supabase.co/auth/v1/callback`

**Bước 4:** Code

```kotlin
// Trong AuthRepositoryImpl
suspend fun signInWithGoogle(context: Context) {
    supabase.auth.signInWith(Google) {
        // Google OAuth flow
    }
}
```

### 10.2 Facebook Login (chưa triển khai)

Tương tự Google nhưng cần:
- Facebook Developer App
- Facebook App ID + App Secret
- Cấu hình trong Supabase Dashboard → Providers → Facebook

---

## 11. Bảo mật & Best Practices

### 11.1 Row Level Security (RLS)

**BẮT BUỘC** bật RLS cho mọi bảng:

```sql
-- Bật RLS
ALTER TABLE public.transactions ENABLE ROW LEVEL SECURITY;

-- Policy: user chỉ thấy data của mình
CREATE POLICY "Users can CRUD own transactions"
    ON public.transactions
    FOR ALL
    USING (auth.uid() = user_id);
```

### 11.2 Không lưu sensitive data trong code

- ❌ KHÔNG lưu `service_role key` trong app
- ✅ Chỉ dùng `anon key` (public key)
- ✅ RLS bảo vệ data ở server side

### 11.3 Token Security

- Supabase SDK tự manage access token + refresh token
- Access token hết hạn sau 1 giờ → SDK auto-refresh
- Khi signOut → SDK xoá session + token

### 11.4 Production Checklist

- [ ] Bật Email Confirmation
- [ ] Bật RLS cho TẤT CẢ bảng
- [ ] KHÔNG hardcode secret key trong code
- [ ] Set rate limit phù hợp
- [ ] Enable CAPTCHA (nếu cần)

---

## 12. Checklist triển khai

### ✅ Đã hoàn thành

- [x] `SupabaseClient.kt` — Singleton với Auth + Postgrest modules
- [x] `AuthRepository.kt` — Interface (Domain layer, pure Kotlin)
- [x] `AuthRepositoryImpl.kt` — Gọi Supabase Auth API thật
- [x] `SettingPreferences.kt` — Thêm `SUPABASE_USER_ID` key
- [x] `AuthContract.kt` — State + Event + NavEvent + successMessage
- [x] `AuthViewModel.kt` — Tích hợp Supabase Auth + error mapping
- [x] `SignInScreen.kt` — UI đăng nhập + success message
- [x] `SignUpScreen.kt` — UI đăng ký
- [x] `MainActivity.kt` — Check auth session cho auto-login
- [x] Navigation flow: Onboarding → SignIn ↔ SignUp → Main

### 🔲 Cần làm thêm

- [ ] Tạo bảng `profiles` trên Supabase Dashboard (xem SQL ở section 2.3)
- [ ] Tắt Email Confirmation trong Supabase Dashboard (dev mode)
- [ ] Test đăng ký + đăng nhập trên thiết bị thật
- [ ] Thêm Sign Out button trong OtherScreen/Settings
- [ ] Tích hợp Google Sign-In
- [ ] Tích hợp Facebook Login
- [ ] Bật RLS cho tất cả bảng
- [ ] Thêm `user_id` vào TransactionEntity (Room migration)

---

**Document Version:** 1.0
**Last Updated:** March 31, 2026
**Author:** GitHub Copilot
