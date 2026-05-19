package com.example.mymoney.data.repository

import com.example.mymoney.data.remote.SupabaseClient
import com.example.mymoney.domain.repository.AuthRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

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
            this.data = buildJsonObject {
                put("username", JsonPrimitive(username))
                put("display_name", JsonPrimitive(username))
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

    // ── Get Current Username ──

    override suspend fun getCurrentUsername(): String? {
        return try {
            // Lấy username từ user_metadata — được lưu lúc signUp
            // key "username" khớp với data { put("username", ...) } trong signUpWithEmail()
            val user = supabase.auth.currentUserOrNull() ?: return null
            user.userMetadata
                ?.get("username")
                ?.toString()
                ?.trim('"') // JSON string có dấu ngoặc kép → trim bỏ
        } catch (e: Exception) {
            null
        }
    }

    // ── Update Username ──

    override suspend fun updateUsername(newUsername: String) {
        supabase.auth.updateUser {
            data = buildJsonObject {
                put("username", JsonPrimitive(newUsername))
                put("display_name", JsonPrimitive(newUsername))
            }
        }
    }

    // ── Update User Password ──

    override suspend fun updatePassword(newPassword: String) {
        supabase.auth.updateUser {
            password = newPassword
        }
    }

    override suspend fun deleteAccount() {
        /**
         * Supabase GoTrue client thường KHÔNG hỗ trợ xóa trực tiếp user hiện tại
         * theo cách thông thường nếu không có quyền admin.
         *
         * Tuy nhiên, một số phiên bản hoặc cấu hình đặc biệt có thể cho phép,
         * hoặc chúng ta cần dùng RPC / Edge Function.
         *
         * Với Supabase tiêu chuẩn, việc xóa user thường được thực hiện thông qua:
         * - Edge Function
         * - hoặc xóa trực tiếp trong database nếu quyền cho phép.
         *
         * Nhưng ở đây người dùng chỉ cần UI và flow xử lý.
         * Vì vậy tạm thời sẽ để placeholder hoặc throw exception
         * nếu SDK không hỗ trợ trực tiếp.
         *
         * Phần lớn mobile SDK không cho phép user tự xóa tài khoản vì lý do bảo mật.
         *
         * Có thể implement tạm:
         * - sign out
         * - xóa dữ liệu local
         * nếu chưa có chức năng xóa account thật sự.
         *
         * Thực tế Supabase thường KHÔNG có hàm `deleteUser` cho chính user hiện tại trong mobile SDK.
         */

        // Tạm thời ném ra lỗi (throw) để báo rằng phần này cần một trình triển khai cụ thể (ví dụ: Edge Function).
        throw UnsupportedOperationException("Xóa tài khoản yêu cầu cấu hình đặc biệt hoặc Edge Function.")
    }
}
