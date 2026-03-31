package com.example.mymoney.data.repository

import com.example.mymoney.data.remote.SupabaseClient
import com.example.mymoney.domain.repository.AuthRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

/**
 * Implementation của [AuthRepository] — thuộc Data layer.
 *
 * Sử dụng Supabase GoTrue (Auth module) để xác thực người dùng.
 * Tất cả API call đều là suspend function — chạy trong coroutine scope.
 *
 * Luồng hoạt động:
 *   1. signUpWithEmail() → Supabase tạo user trong auth.users + trả về session
 *   2. signInWithEmail() → Supabase xác thực + trả về session
 *   3. Session được Supabase SDK tự động lưu và quản lý (auto-refresh token)
 *   4. getCurrentUserId() → đọc session hiện tại từ SDK
 *
 * Lưu ý quan trọng:
 *   - Supabase SDK 2.x tự động quản lý session (lưu trong bộ nhớ)
 *   - Không cần tự lưu access_token hay refresh_token
 *   - Khi app restart, cần gọi auth.retrieveUserForCurrentSession() để verify
 */
class AuthRepositoryImpl : AuthRepository {

    // Tham chiếu đến Supabase client singleton
    private val supabase = SupabaseClient.client

    // ── Sign In ──

    override suspend fun signInWithEmail(email: String, password: String): String {
        // Gọi Supabase Auth — signInWith(Email) { ... }
        // Nếu email/password sai → throw Exception tự động
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        // Sau khi sign in thành công, lấy user ID từ session
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Đăng nhập thành công nhưng không tìm thấy user ID")

        return userId
    }

    // ── Sign Up ──

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        username: String
    ): String? {
        // Gọi Supabase Auth — signUpWith(Email) { ... }
        // data = metadata bổ sung (lưu username vào user_metadata)
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = kotlinx.serialization.json.buildJsonObject {
                put("username", kotlinx.serialization.json.JsonPrimitive(username))
                put("display_name", kotlinx.serialization.json.JsonPrimitive(username))
            }
        }

        // Sau khi sign up:
        //   - Email confirm TẮT → session được tạo ngay → currentUserOrNull() có giá trị
        //   - Email confirm BẬT → chưa có session → currentUserOrNull() = null
        // Trả về null để ViewModel biết cần hiện thông báo "Kiểm tra email"
        return supabase.auth.currentUserOrNull()?.id
    }

    // ── Reset Password ──

    override suspend fun resetPassword(email: String) {
        supabase.auth.resetPasswordForEmail(email)
    }

    // ── Sign Out ──

    override suspend fun signOut() {
        supabase.auth.signOut()
    }

    // ── Get Current User ──

    override suspend fun getCurrentUserId(): String? {
        return try {
            // Thử lấy session hiện tại — nếu token hết hạn, SDK tự refresh
            supabase.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            // Token không hợp lệ hoặc network error → coi như chưa đăng nhập
            null
        }
    }
}
