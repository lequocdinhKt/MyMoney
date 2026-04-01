package com.example.mymoney.presentation.viewmodel.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.repository.AuthRepositoryImpl
import com.example.mymoney.domain.repository.AuthRepository
import com.example.mymoney.presentation.viewmodel.auth.auth.AuthEvent
import com.example.mymoney.presentation.viewmodel.auth.auth.AuthNavEvent
import com.example.mymoney.presentation.viewmodel.auth.auth.AuthUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel: xử lý logic đăng nhập/đăng ký với Supabase Auth
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel dùng chung cho SignInScreen và SignUpScreen.
 *
 * Luồng UDF:
 *   UI gửi [AuthEvent] → ViewModel xử lý (gọi Supabase Auth) → cập nhật [AuthUiState]
 *   hoặc phát [AuthNavEvent] qua SharedFlow → UI collect và navigate
 *
 * Luồng Auth:
 *   1. User nhập email/password → UI gửi AuthEvent
 *   2. ViewModel gọi AuthRepository (→ Supabase Auth API)
 *   3. Thành công → lưu userId vào DataStore → emit NavigateToMain
 *   4. Thất bại → cập nhật errorMessage trên UI
 *
 * @param authRepository     Repository xử lý xác thực (gọi Supabase)
 * @param settingPreferences Truy cập DataStore để lưu/đọc userId
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val settingPreferences: SettingPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())

    /** State cho UI quan sát – chỉ đọc */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // replay = 0: không phát lại event cũ khi subscriber mới collect
    private val _navEvent = MutableSharedFlow<AuthNavEvent>()

    /** Navigation side-effect – UI collect 1 lần qua LaunchedEffect(Unit) */
    val navEvent: SharedFlow<AuthNavEvent> = _navEvent.asSharedFlow()

    // ── Xử lý event từ UI ──

    fun onEvent(event: AuthEvent) {
        when (event) {
            // ── Thay đổi input ──
            is AuthEvent.OnEmailChanged ->
                _uiState.update { it.copy(email = event.email, errorMessage = null, successMessage = null) }

            is AuthEvent.OnPasswordChanged ->
                _uiState.update { it.copy(password = event.password, errorMessage = null, successMessage = null) }

            is AuthEvent.OnUsernameChanged ->
                _uiState.update { it.copy(username = event.username, errorMessage = null, successMessage = null) }

            is AuthEvent.OnConfirmPasswordChanged ->
                _uiState.update { it.copy(confirmPassword = event.confirmPassword, errorMessage = null, successMessage = null) }

            // ── Toggle ──
            is AuthEvent.OnTogglePasswordVisibility ->
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

            is AuthEvent.OnToggleConfirmPasswordVisibility ->
                _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }

            is AuthEvent.OnToggleRememberMe ->
                _uiState.update { it.copy(rememberMe = !it.rememberMe) }

            is AuthEvent.OnToggleAgreeTerms ->
                _uiState.update { it.copy(agreeTerms = !it.agreeTerms) }

            // ── Hành động chính ──
            is AuthEvent.OnSignInClicked -> handleSignIn()
            is AuthEvent.OnSignUpClicked -> handleSignUp()
            is AuthEvent.OnForgotPasswordClicked -> handleForgotPassword()

            // ── Social Login ──
            is AuthEvent.OnGoogleLoginClicked -> handleGoogleLogin()
            is AuthEvent.OnFacebookLoginClicked -> handleFacebookLogin()

            // ── Chuyển trang ──
            is AuthEvent.OnNavigateToSignUp ->
                viewModelScope.launch { _navEvent.emit(AuthNavEvent.NavigateToSignUp) }

            is AuthEvent.OnNavigateToSignIn ->
                viewModelScope.launch { _navEvent.emit(AuthNavEvent.NavigateToSignIn) }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIGN IN — Đăng nhập bằng Email + Password
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Xử lý đăng nhập:
     *   1. Validate input (email, password không rỗng)
     *   2. Gọi authRepository.signInWithEmail() → Supabase Auth API
     *   3. Thành công → lưu userId vào DataStore → emit NavigateToMain
     *   4. Thất bại → hiển thị errorMessage trên UI
     */
    private fun handleSignIn() {
        val state = _uiState.value

        // ── Validation cơ bản ──
        if (state.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập email") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập mật khẩu") }
            return
        }

        // ── Gọi Supabase Auth ──
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Gọi repository → Supabase signInWith(Email) { ... }
                val userId = authRepository.signInWithEmail(
                    email = state.email.trim(),
                    password = state.password,
                )

                // Đăng nhập thành công → lưu userId vào DataStore
                settingPreferences.saveUserId(userId)

                // Lưu username vào DataStore để đọc offline khi mở app lại
                val username = authRepository.getCurrentUsername()
                if (!username.isNullOrBlank()) {
                    settingPreferences.saveUsername(username)
                }

                // Phát navigation event → UI sẽ navigate sang MainScreen
                _uiState.update { it.copy(isLoading = false) }
                _navEvent.emit(AuthNavEvent.NavigateToMain)

            } catch (e: Exception) {
                // Đăng nhập thất bại → hiển thị lỗi
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = mapAuthError(e),
                    )
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SIGN UP — Đăng ký tài khoản mới
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Xử lý đăng ký:
     *   1. Validate input (username, email, password, confirm, terms)
     *   2. Gọi authRepository.signUpWithEmail() → Supabase Auth API
     *   3. Thành công → lưu userId vào DataStore → emit NavigateToMain
     *   4. Thất bại → hiển thị errorMessage trên UI
     *
     * Lưu ý: Supabase mặc định yêu cầu xác minh email (confirm email).
     * Nếu bật confirm email trong Supabase Dashboard:
     *   - signUp thành công nhưng session = null cho đến khi user confirm
     *   - Cần hiển thị "Kiểm tra email để xác minh tài khoản"
     * Nếu tắt confirm email (dev mode):
     *   - signUp trả về session ngay → navigate sang Main
     */
    private fun handleSignUp() {
        val state = _uiState.value

        // ── Validation cơ bản ──
        if (state.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên người dùng") }
            return
        }
        if (state.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập email") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu cần ít nhất 6 ký tự") }
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu xác nhận không khớp") }
            return
        }
        if (!state.agreeTerms) {
            _uiState.update { it.copy(errorMessage = "Vui lòng đồng ý điều khoản dịch vụ") }
            return
        }

        // ── Gọi Supabase Auth ──
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                // Gọi repository → Supabase signUpWith(Email) { ... }
                // Trả về userId nếu email confirm tắt, null nếu cần confirm email
                val userId = authRepository.signUpWithEmail(
                    email = state.email.trim(),
                    password = state.password,
                    username = state.username.trim(),
                )

                if (userId != null) {
                    // Email confirm TẮT → có session ngay → lưu userId + username + navigate sang Main
                    settingPreferences.saveUserId(userId)
                    settingPreferences.saveUsername(state.username.trim())
                    _uiState.update { it.copy(isLoading = false) }
                    _navEvent.emit(AuthNavEvent.NavigateToMain)
                } else {
                    // Email confirm BẬT → chưa có session → hiện thông báo kiểm tra email
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Đăng ký thành công! Kiểm tra email \"${state.email.trim()}\" để xác minh tài khoản, sau đó quay lại đăng nhập.",
                            errorMessage = null,
                        )
                    }
                    // Chuyển về màn hình Sign In sau khi hiện thông báo
                    _navEvent.emit(AuthNavEvent.NavigateToSignIn)
                }

            } catch (e: Exception) {
                // Đăng ký thất bại → hiển thị lỗi
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = mapAuthError(e),
                    )
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FORGOT PASSWORD — Gửi email đặt lại mật khẩu
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Gửi email reset password qua Supabase Auth.
     * Supabase sẽ gửi link đặt lại mật khẩu đến email người dùng.
     */
    private fun handleForgotPassword() {
        val state = _uiState.value
        if (state.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập email để đặt lại mật khẩu") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                authRepository.resetPassword(state.email.trim())
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Đã gửi email đặt lại mật khẩu! Kiểm tra hộp thư của bạn.",
                        errorMessage = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = mapAuthError(e),
                    )
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SOCIAL LOGIN — Google / Facebook (chưa triển khai)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * TODO: Tích hợp Google Sign-In với Supabase OAuth
     * Cần thêm:
     *   - Google OAuth Client ID trong Supabase Dashboard → Auth → Providers → Google
     *   - Dependency: implementation("io.github.jan-tennert.supabase:compose-auth:2.5.0")
     *   - Dependency: implementation("io.github.jan-tennert.supabase:compose-auth-ui:2.5.0")
     */
    private fun handleGoogleLogin() {
        _uiState.update {
            it.copy(errorMessage = "Google Sign-In chưa được tích hợp. Vui lòng dùng email.")
        }
    }

    /**
     * TODO: Tích hợp Facebook Login với Supabase OAuth
     * Cần thêm:
     *   - Facebook App ID trong Supabase Dashboard → Auth → Providers → Facebook
     */
    private fun handleFacebookLogin() {
        _uiState.update {
            it.copy(errorMessage = "Facebook Login chưa được tích hợp. Vui lòng dùng email.")
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ERROR MAPPING — Chuyển Exception thành thông báo thân thiện
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Map Supabase Exception thành thông báo lỗi tiếng Việt thân thiện.
     * Supabase trả về các lỗi phổ biến:
     *   - "Invalid login credentials"      → Sai email hoặc mật khẩu
     *   - "User already registered"        → Email đã được đăng ký
     *   - "Email not confirmed"            → Chưa xác minh email
     *   - Network error                    → Kiểm tra kết nối mạng
     */
    private fun mapAuthError(e: Exception): String {
        val message = e.message?.lowercase() ?: return "Đã xảy ra lỗi không xác định"

        return when {
            "invalid login credentials" in message ->
                "Email hoặc mật khẩu không đúng"

            "user already registered" in message ->
                "Email này đã được đăng ký. Hãy đăng nhập hoặc dùng email khác."

            "email not confirmed" in message ->
                "Email chưa được xác minh. Kiểm tra hộp thư của bạn."

            "password" in message && ("too short" in message || "at least" in message) ->
                "Mật khẩu cần ít nhất 6 ký tự"

            "email" in message && "invalid" in message ->
                "Địa chỉ email không hợp lệ"

            "rate limit" in message || "too many requests" in message ->
                "Quá nhiều lần thử. Vui lòng đợi một lúc rồi thử lại."

            "network" in message || "unable to resolve host" in message
                    || "timeout" in message || "connect" in message ->
                "Không thể kết nối. Kiểm tra kết nối mạng của bạn."

            "signup is disabled" in message ->
                "Đăng ký hiện đang tắt. Liên hệ quản trị viên."

            else -> "Đã xảy ra lỗi: ${e.message}"
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Factory: tạo ViewModel với dependencies
    // Dùng thủ công khi chưa có DI framework (Hilt/Koin)
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        /**
         * Tạo [ViewModelProvider.Factory] nhận [Context].
         *
         * Cách dùng trong Composable:
         * ```kotlin
         * val ctx = LocalContext.current
         * val vm: AuthViewModel = viewModel(factory = AuthViewModel.factory(ctx))
         * ```
         */
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AuthViewModel(
                        authRepository = AuthRepositoryImpl(),
                        settingPreferences = SettingPreferences(context.applicationContext),
                    ) as T
            }
    }
}
