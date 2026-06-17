package com.example.mymoney.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.security.PinEvent
import com.example.mymoney.presentation.viewmodel.security.PinViewModel
import com.example.mymoney.presentation.viewmodel.security.PinViewModelFactory

@Composable
fun PinEntryScreen(
    onSuccess: () -> Unit,
    viewModel: PinViewModel = viewModel(
        factory = PinViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Tự động hiện sinh trắc học nếu được bật
    LaunchedEffect(uiState.isBiometricEnabled) {
        if (uiState.isBiometricEnabled && context is FragmentActivity) {
            BiometricHelper.showBiometricPrompt(
                activity = context,
                onSuccess = onSuccess,
                onError = { /* Xử lý lỗi nếu cần */ }
            )
        }
    }

    // Kiểm tra PIN đúng
    LaunchedEffect(uiState.enteredPin) {
        if (uiState.enteredPin.length == 6) {
            if (uiState.enteredPin == uiState.currentPin) {
                onSuccess()
            } else {
                viewModel.onEvent(PinEvent.OnClearClick)
                // Hiển thị lỗi rung lắc hoặc toast
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
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

            Spacer(modifier = Modifier.height(48.dp))

            // Numeric Keypad
            NumericKeypad(
                onNumberClick = { viewModel.onEvent(PinEvent.OnNumberClick(it)) },
                onDeleteClick = { viewModel.onEvent(PinEvent.OnDeleteClick) }
            )

            if (uiState.isBiometricEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = {
                        if (context is FragmentActivity) {
                            BiometricHelper.showBiometricPrompt(
                                activity = context,
                                onSuccess = onSuccess,
                                onError = { }
                            )
                        }
                    },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Sinh trắc học",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
