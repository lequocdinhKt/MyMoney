package com.example.mymoney.domain.repository

/**
 * Repository interface cho xác thực — thuộc Domain layer.
 * Data layer sẽ implement interface này (dependency inversion).
 *
 * Quy tắc:
 *   - KHÔNG import Supabase, Android SDK, hay bất kỳ thư viện cụ thể nào.
 *   - Chỉ dùng Kotlin + Coroutines.
 */
interface AuthRepository {

    /**
     * Đăng nhập bằng email + password.
     * @return userId (UUID string) nếu thành công
     * @throws Exception nếu thất bại (sai email/password, mạng lỗi...)
     */
    suspend fun signInWithEmail(email: String, password: String): String

    /**
     * Đăng ký tài khoản mới bằng email + password.
     * @param username Tên hiển thị — lưu vào metadata/profile
     * @return userId (UUID string) nếu session được tạo ngay (email confirm tắt),
     *         hoặc null nếu cần xác minh email trước (email confirm bật)
     * @throws Exception nếu thất bại (email đã tồn tại, mạng lỗi...)
     */
    suspend fun signUpWithEmail(email: String, password: String, username: String): String?

    /**
     * Gửi email đặt lại mật khẩu.
     * @throws Exception nếu thất bại
     */
    suspend fun resetPassword(email: String)

    /**
     * Đăng xuất người dùng hiện tại.
     * @throws Exception nếu thất bại
     */
    suspend fun signOut()

    /**
     * Kiểm tra xem người dùng đã đăng nhập chưa (có session hợp lệ không).
     * @return userId nếu đã đăng nhập, null nếu chưa
     */
    suspend fun getCurrentUserId(): String?

    /**
     * Lấy username của user hiện tại từ Supabase Auth metadata.
     * @return username (chuỗi) nếu có, null nếu chưa đăng nhập hoặc chưa có metadata
     */
    suspend fun getCurrentUsername(): String?
}
