package com.example.mymoney.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.security.PinEvent
import com.example.mymoney.presentation.viewmodel.security.PinStep
import com.example.mymoney.presentation.viewmodel.security.PinViewModel
import com.example.mymoney.presentation.viewmodel.security.PinViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: PinViewModel = viewModel(
        factory = PinViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.currentPin == null) "Thiết lập mã PIN" else "Thay đổi mã PIN") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = when (uiState.step) {
                    PinStep.ENTER_CURRENT_PIN -> "Nhập mã PIN hiện tại"
                    PinStep.ENTER_NEW_PIN -> "Nhập mã PIN mới"
                    PinStep.CONFIRM_NEW_PIN -> "Xác nhận mã PIN mới"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Biometric Toggle (chỉ hiện khi đã ở bước nhập PIN mới hoặc đã có PIN)
            if (uiState.currentPin != null || uiState.step == PinStep.ENTER_NEW_PIN) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sử dụng Face ID / Vân tay", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = { viewModel.onEvent(PinEvent.OnToggleBiometric(it)) }
                    )
                }
            }

            // Numeric Keypad
            NumericKeypad(
                onNumberClick = { viewModel.onEvent(PinEvent.OnNumberClick(it)) },
                onDeleteClick = { viewModel.onEvent(PinEvent.OnDeleteClick) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PinDot(filled: Boolean) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(
                if (filled) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.outlineVariant
            )
    )
}

@Composable
fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "delete")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        numbers.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { item ->
                    KeypadButton(
                        text = item,
                        onClick = {
                            if (item == "delete") onDeleteClick()
                            else if (item.isNotEmpty()) onNumberClick(item)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(enabled = text.isNotEmpty()) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text == "delete") {
            Icon(Icons.Default.Backspace, contentDescription = "Xóa")
        } else if (text.isNotEmpty()) {
            Text(
                text = text,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
