package com.example.mymoney.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.wallet.WALLET_PRESET_COLORS
import com.example.mymoney.presentation.viewmodel.wallet.WalletSetupEvent
import com.example.mymoney.presentation.viewmodel.wallet.WalletSetupUiState
import com.example.mymoney.presentation.viewmodel.wallet.WalletSetupViewModel
import com.example.mymoney.presentation.viewmodel.wallet.WalletSetupViewModelFactory
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Thiết lập ví — dùng cho cả tạo mới và chỉnh sửa.
 *
 * @param userId         ID người dùng
 * @param walletId       null = tạo mới, khác null = chỉnh sửa
 * @param onNavigateBack Callback khi nhấn Back hoặc lưu thành công
 */
@Composable
fun WalletSetupScreen(
    userId: String,
    walletId: Long? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: WalletSetupViewModel = viewModel(
        factory = WalletSetupViewModelFactory(context, userId, walletId),
        key     = "wallet_${walletId ?: "new"}"
    )
    val uiState by viewModel.uiState.collectAsState()

    // Tự navigate back khi lưu thành công
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    WalletSetupContent(
        uiState        = uiState,
        onEvent        = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun WalletSetupContent(
    uiState: WalletSetupUiState,
    onEvent: (WalletSetupEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    // ── Loading Overlay: dùng Dialog để phủ toàn màn hình thực sự ── //
    if (uiState.isDeleting) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress   = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(32.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color    = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text  = "Đang xóa ví...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // ── Dialog xác nhận xóa ──
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(WalletSetupEvent.DeleteDismissed)},
            title = {
                Text(
                    text       = "Xóa ví?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text  = "Hành động này sẽ xóa tất cả dữ liệu liên quan đến ví này. Bạn có chắc chắn muốn tiếp tục?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                OutlinedButton(onClick = { onEvent(WalletSetupEvent.DeleteConfirm) }) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onEvent(WalletSetupEvent.DeleteDismissed) }) {
                    Text("Huỷ")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        // ── Top Bar ──
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                enabled = !uiState.isDeleting
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint               = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text       = if (uiState.isEditMode) "Chỉnh sửa ví" else "Thiết lập ví",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                modifier   = Modifier.weight(1f).padding(start = 4.dp)
            )
        }

        // ── Form ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Tên ví
            OutlinedTextField(
                value          = uiState.name,
                onValueChange  = { onEvent(WalletSetupEvent.NameChanged(it)) },
                label          = { Text("Tên ví") },
                singleLine     = true,
                shape          = RoundedCornerShape(12.dp),
                modifier       = Modifier.fillMaxWidth(),
                enabled        = !uiState.isDeleting,
            )

            // Số dư ban đầu
            OutlinedTextField(
                value         = uiState.initialBalance,
                onValueChange = { onEvent(WalletSetupEvent.BalanceChanged(it)) },
                label         = { Text("Số dư ban đầu") },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon  = { Text("₫", modifier = Modifier.padding(end = 12.dp)) },
                modifier      = Modifier.fillMaxWidth(),
                enabled        = !uiState.isDeleting,
            )

            // Bộ chọn màu
            ColorSwatchSection(
                selectedColor = uiState.selectedColor,
                enabled = !uiState.isDeleting,
                onColorSelected = { onEvent(WalletSetupEvent.ColorChanged(it)) }
            )

            // Ví mặc định
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Đặt làm ví mặc định",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Switch(
                    checked         = uiState.isDefault,
                    enabled         = !uiState.isDeleting,
                    onCheckedChange = { onEvent(WalletSetupEvent.DefaultChanged(it)) }
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        // ── Error snackbar ──
        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action   = {
                    Text(
                        text     = "OK",
                        modifier = Modifier.clickable { onEvent(WalletSetupEvent.DismissError) }
                    )
                }
            ) { Text(uiState.error) }
        }

        // ── Nút xóa ──
        if(uiState.isEditMode) {
            Button(
                onClick = { onEvent(WalletSetupEvent.DeleteClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    disabledContainerColor = Color(0xFFE57373)
                )
            ) {
                Text(
                    text = "Xóa ví",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

        }

        // ── Nút Tiếp tục ──
        Button(
            onClick  = { onEvent(WalletSetupEvent.Submit) },
            enabled  = !uiState.isLoading && !uiState.isDeleting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(52.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4DD0C4)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color    = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text     = if (uiState.isEditMode) "Lưu thay đổi" else "Tiếp tục",
                    color    = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Color Swatch Picker ──

@Composable
private fun ColorSwatchSection(
    selectedColor: String,
    enabled: Boolean = true,
    onColorSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text  = "Màu ví",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            WALLET_PRESET_COLORS.forEach { hex ->
                val color = runCatching { Color(hex.toColorInt()) }.getOrElse { Color.Gray }
                val isSelected = hex == selectedColor
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected)
                                Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                            else
                                Modifier.border(1.dp, Color.Transparent, CircleShape)
                        )
                        .clickable (
                            enabled = enabled
                        ) {
                            onColorSelected(hex)
                        }
                )
            }
        }
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WalletSetupAddPreview() {
    MyMoneyTheme(darkTheme = false) {
        WalletSetupContent(
            uiState        = WalletSetupUiState(),
            onEvent        = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WalletSetupEditPreview() {
    MyMoneyTheme(darkTheme = false) {
        WalletSetupContent(
            uiState = WalletSetupUiState(
                isEditMode     = true,
                name           = "Ví chính",
                initialBalance = "1000000",
                selectedColor  = "#FF6B9D",
                isDefault      = true
            ),
            onEvent        = {},
            onNavigateBack = {}
        )
    }
}
