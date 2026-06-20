package com.example.mymoney.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.security.PinEvent
import com.example.mymoney.presentation.viewmodel.security.PinViewModel
import com.example.mymoney.presentation.viewmodel.security.PinViewModelFactory

@Composable
fun PinEntryScreen(
    onSuccess: () -> Unit,
    viewModel: PinViewModel = viewModel(
        factory = PinViewModelFactory(LocalContext.current, isSetupFlow = false)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Tự động hiện sinh trắc học nếu được bật
    LaunchedEffect(uiState.isBiometricEnabled) {
        if (uiState.isBiometricEnabled && context is FragmentActivity && uiState.lockUntil <= System.currentTimeMillis()) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                onSuccess = { viewModel.onEvent(PinEvent.OnBiometricSuccess) },
                onError = { /* Xử lý lỗi nếu cần */ }
            )
        }
    }

    // Kiểm tra thành công
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
            viewModel.onEvent(PinEvent.OnResetState)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Nhập mã PIN để tiếp tục",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(6) { index ->
                        PinDot(filled = index < uiState.enteredPin.length)
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Numeric Keypad
                NumericKeypad(
                    onNumberClick = { viewModel.onEvent(PinEvent.OnNumberClick(it)) },
                    onDeleteClick = { viewModel.onEvent(PinEvent.OnDeleteClick) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quên mã PIN
                    TextButton(onClick = { viewModel.onEvent(PinEvent.OnForgotPinClick) }) {
                        Text("Quên mã PIN?", color = MaterialTheme.colorScheme.primary)
                    }

                    // Biometric Button
                    if (uiState.isBiometricEnabled) {
                        IconButton(
                            onClick = {
                                if (context is FragmentActivity && uiState.lockUntil <= System.currentTimeMillis()) {
                                    BiometricHelper.showBiometricPrompt(
                                        activity = context,
                                        onSuccess = { viewModel.onEvent(PinEvent.OnBiometricSuccess) },
                                        onError = { }
                                    )
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Sinh trắc học",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Lockout Overlay
            if (uiState.lockUntil > System.currentTimeMillis()) {
                val remainingSeconds = (uiState.lockUntil - System.currentTimeMillis()) / 1000
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ứng dụng đang bị khóa",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vui lòng thử lại sau ${remainingSeconds / 60}:${String.format("%02d", remainingSeconds % 60)}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.onEvent(PinEvent.OnForgotPinClick) }) {
                            Text("Quên mã PIN? Đăng xuất ngay")
                        }
                    }
                }
            }
        }
    }

    // Forgot PIN Dialog
    if (uiState.isForgotPinDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(PinEvent.OnDismissForgotPin) },
            title = { Text("Quên mã PIN?") },
            text = { Text("Nếu bạn quên mã PIN, bạn cần đăng xuất và đăng nhập lại để thiết lập mã PIN mới. Toàn bộ dữ liệu local chưa đồng bộ có thể bị mất.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(PinEvent.OnConfirmForgotPin) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xác nhận Đăng xuất")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(PinEvent.OnDismissForgotPin) }) {
                    Text("Hủy")
                }
            }
        )
    }
}
