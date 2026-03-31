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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
 * Màn hình Đăng ký (Create Account / Sign Up).
 *
 * UI hoàn toàn stateless — state đến từ [AuthViewModel].
 * Navigation side-effect qua [AuthViewModel.navEvent].
 *
 * @param onNavigateToMain   Callback khi đăng ký thành công → chuyển sang MainScreen
 * @param onNavigateToSignIn Callback khi nhấn "Sign In" → quay lại SignInScreen
 * @param modifier           Modifier tuỳ chỉnh
 * @param viewModel          ViewModel quản lý state auth
 */
@Composable
fun SignUpScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToSignIn: () -> Unit,
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
                is AuthNavEvent.NavigateToSignIn -> onNavigateToSignIn()
                is AuthNavEvent.NavigateToSignUp -> { /* Đã ở Sign Up */ }
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
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Username ──
            AuthTextField(
                value = uiState.username,
                onValueChange = { viewModel.onEvent(AuthEvent.OnUsernameChanged(it)) },
                placeholder = "Choose a username",
                leadingIcon = Icons.Filled.Person,
                keyboardType = KeyboardType.Text,
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // ── Confirm Password ──
            AuthTextField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onEvent(AuthEvent.OnConfirmPasswordChanged(it)) },
                placeholder = "Confirm Password",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                isPasswordVisible = uiState.isConfirmPasswordVisible,
                onTogglePasswordVisibility = {
                    viewModel.onEvent(AuthEvent.OnToggleConfirmPasswordVisibility)
                },
                keyboardType = KeyboardType.Password,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Agree Terms ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = uiState.agreeTerms,
                    onCheckedChange = { viewModel.onEvent(AuthEvent.OnToggleAgreeTerms) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                Text(
                    text = "I agree to the ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Terms of Service & Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        // TODO: Mở trang Terms of Service
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Nút CREATE ACCOUNT ──
            Button(
                onClick = { viewModel.onEvent(AuthEvent.OnSignUpClicked) },
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
                        text = "CREATE ACCOUNT",
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

            Spacer(modifier = Modifier.height(24.dp))

            // ── Social Login ──
            SocialLoginSection(
                onGoogleClick = { viewModel.onEvent(AuthEvent.OnGoogleLoginClicked) },
                onFacebookClick = { viewModel.onEvent(AuthEvent.OnFacebookLoginClicked) },
            )

            // ── Spacer đẩy text xuống dưới ──
            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(24.dp))

            // ── Already have an account? Sign In ──
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        viewModel.onEvent(AuthEvent.OnNavigateToSignIn)
                    },
                )
            }
        }
    }
}

// ── Preview ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SignUpScreenPreview() {
    MyMoneyTheme {
        SignUpScreen(
            onNavigateToMain = {},
            onNavigateToSignIn = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SignUpScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        SignUpScreen(
            onNavigateToMain = {},
            onNavigateToSignIn = {},
        )
    }
}
