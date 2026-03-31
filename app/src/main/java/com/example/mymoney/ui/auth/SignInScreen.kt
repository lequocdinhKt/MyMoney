package com.example.mymoney.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.auth.AuthViewModel
import com.example.mymoney.presentation.viewmodel.auth.auth.AuthEvent
import com.example.mymoney.presentation.viewmodel.auth.auth.AuthNavEvent
import com.example.mymoney.ui.auth.components.AuthTextField
import com.example.mymoney.ui.auth.components.SocialLoginSection
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Đăng nhập (Sign In).
 *
 * UI hoàn toàn stateless — state đến từ [AuthViewModel].
 * Navigation side-effect qua [AuthViewModel.navEvent].
 *
 * @param onNavigateToMain  Callback khi đăng nhập thành công → chuyển sang MainScreen
 * @param onNavigateToSignUp Callback khi nhấn "Sign Up" → chuyển sang SignUpScreen
 * @param modifier          Modifier tuỳ chỉnh
 * @param viewModel         ViewModel quản lý state auth
 */
@Composable
fun SignInScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(LocalContext.current)
    ),
) {
    // Quan sát UI state từ ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect navigation side-effect
    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is AuthNavEvent.NavigateToMain -> onNavigateToMain()
                is AuthNavEvent.NavigateToSignUp -> onNavigateToSignUp()
                is AuthNavEvent.NavigateToSignIn -> { /* Đã ở Sign In */ }
            }
        }
    }

    // ── UI Content ──
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            // ── Tiêu đề ──
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Email ──
            AuthTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(AuthEvent.OnEmailChanged(it)) },
                placeholder = "your.email@example.com",
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Password ──
            AuthTextField(
                value = uiState.password,
                onValueChange = { viewModel.onEvent(AuthEvent.OnPasswordChanged(it)) },
                placeholder = "Password",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                isPasswordVisible = uiState.isPasswordVisible,
                onTogglePasswordVisibility = {
                    viewModel.onEvent(AuthEvent.OnTogglePasswordVisibility)
                },
                keyboardType = KeyboardType.Password,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Nút SIGN IN ──
            Button(
                onClick = { viewModel.onEvent(AuthEvent.OnSignInClicked) },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "SIGN IN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // ── Error message ──
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // ── Success message (VD: "Đã gửi email đặt lại mật khẩu") ──
            if (uiState.successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // ── Forgot Password ──
            TextButton(
                onClick = { viewModel.onEvent(AuthEvent.OnForgotPasswordClicked) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = "Forgot Password?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Social Login ──
            SocialLoginSection(
                onGoogleClick = { viewModel.onEvent(AuthEvent.OnGoogleLoginClicked) },
                onFacebookClick = { viewModel.onEvent(AuthEvent.OnFacebookLoginClicked) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Remember me ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = uiState.rememberMe,
                    onCheckedChange = { viewModel.onEvent(AuthEvent.OnToggleRememberMe) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                Text(
                    text = "Remember me",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            // ── Spacer đẩy text xuống dưới ──
            Spacer(modifier = Modifier.weight(1f))

            // ── Don't have an account? Sign Up ──
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        viewModel.onEvent(AuthEvent.OnNavigateToSignUp)
                    },
                )
            }
        }
    }
}

// ── Preview ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignInScreenPreview() {
    MyMoneyTheme {
        SignInScreen(
            onNavigateToMain = {},
            onNavigateToSignUp = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SignInScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        SignInScreen(
            onNavigateToMain = {},
            onNavigateToSignUp = {},
        )
    }
}
