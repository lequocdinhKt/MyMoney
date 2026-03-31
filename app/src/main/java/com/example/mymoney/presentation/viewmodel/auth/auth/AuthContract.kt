package com.example.mymoney.presentation.viewmodel.auth.auth

// ─────────────────────────────────────────────────────────────────────────────
// Contract: tập hợp State, Event, NavEvent cho SignInScreen & SignUpScreen
// Tách riêng để ViewModel và Screen không import lẫn nhau
// ─────────────────────────────────────────────────────────────────────────────

// ── UI State ──

/**
 * Trạng thái bất biến dùng chung cho cả Sign In và Sign Up.
 * ViewModel cập nhật bằng copy(), UI chỉ đọc.
 *
 * @param email             Email người dùng nhập
 * @param password          Mật khẩu
 * @param username          Tên người dùng (chỉ dùng cho Sign Up)
 * @param confirmPassword   Xác nhận mật khẩu (chỉ dùng cho Sign Up)
 * @param isPasswordVisible Hiển thị mật khẩu (toggle eye icon)
 * @param isConfirmPasswordVisible Hiển thị xác nhận mật khẩu
 * @param rememberMe        Ghi nhớ đăng nhập (Sign In)
 * @param agreeTerms        Đồng ý điều khoản (Sign Up)
 * @param isLoading         Đang xử lý đăng nhập/đăng ký
 * @param errorMessage      Thông báo lỗi (null = không có lỗi)
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val rememberMe: Boolean = false,
    val agreeTerms: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

// ── UI Event ──

/**
 * Hành động người dùng gửi từ UI lên ViewModel.
 * Bao gồm cả Sign In và Sign Up events.
 */
sealed class AuthEvent {
    // ── Thay đổi input ──
    data class OnEmailChanged(val email: String) : AuthEvent()
    data class OnPasswordChanged(val password: String) : AuthEvent()
    data class OnUsernameChanged(val username: String) : AuthEvent()
    data class OnConfirmPasswordChanged(val confirmPassword: String) : AuthEvent()

    // ── Toggle ──
    data object OnTogglePasswordVisibility : AuthEvent()
    data object OnToggleConfirmPasswordVisibility : AuthEvent()
    data object OnToggleRememberMe : AuthEvent()
    data object OnToggleAgreeTerms : AuthEvent()

    // ── Hành động chính ──
    data object OnSignInClicked : AuthEvent()
    data object OnSignUpClicked : AuthEvent()
    data object OnForgotPasswordClicked : AuthEvent()

    // ── Social Login ──
    data object OnGoogleLoginClicked : AuthEvent()
    data object OnFacebookLoginClicked : AuthEvent()

    // ── Chuyển trang ──
    data object OnNavigateToSignUp : AuthEvent()
    data object OnNavigateToSignIn : AuthEvent()
}

// ── Navigation Side-Effect ──

/**
 * Side-effect điều hướng phát qua SharedFlow.
 * Không lưu trong UiState – tránh xử lý lại khi recompose.
 */
sealed class AuthNavEvent {
    /** Đăng nhập/đăng ký thành công → chuyển sang MainScreen */
    data object NavigateToMain : AuthNavEvent()

    /** Từ Sign In → chuyển sang Sign Up */
    data object NavigateToSignUp : AuthNavEvent()

    /** Từ Sign Up → quay lại Sign In */
    data object NavigateToSignIn : AuthNavEvent()
}
