package com.example.mymoney.ui.saving

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.saving.add_saving_record.AddSavingRecordEvent
import com.example.mymoney.presentation.viewmodel.saving.add_saving_record.AddSavingRecordUiState
import com.example.mymoney.presentation.viewmodel.saving.add_saving_record.AddSavingRecordViewModel
import com.example.mymoney.presentation.viewmodel.saving.add_saving_record.AddSavingRecordViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.mymoney.domain.model.WalletModel

@Composable
fun AddSavingRecordScreen(
    userId: String,
    goalId: Long,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AddSavingRecordViewModel = viewModel (
        factory = AddSavingRecordViewModelFactory(context, userId, goalId)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Tự navigate back khi lưu thành công
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    AddSavingRecordContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun AddSavingRecordContent(
    uiState: AddSavingRecordUiState,
    onEvent: (AddSavingRecordEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val cleanAmount = uiState.amount.replace(".", "").replace(",", "").toLongOrNull() ?: 0L
    val isValid =  uiState.note.isNotBlank() && cleanAmount > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Thêm bản ghi tiết kiệm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight .SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
        }

        Text(
            text = "Thêm bản ghi để tăng số tiền tiết kiệm của bạn",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))


        // ── Form ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { onEvent(AddSavingRecordEvent.OnNoteChanged(it)) },
                label = { Text("Ghi chú") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { onEvent(AddSavingRecordEvent.OnAmountChanged(it)) },
                label = { Text("Số tiền") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { Text("₫", modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = formatDate(uiState.currentDate),
                onValueChange = {},
                readOnly = true,
                label = {
                    Text("Ngày")
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // ── Danh sách ví ──
            WalletSelector(
                wallets = uiState.wallets,
                selectedWalletId = uiState.selectedWalletId,
                onWalletSelected = {
                    onEvent(AddSavingRecordEvent.OnWalletSelected(it))
                }
            )
        }

        Button(
            onClick = { onEvent(AddSavingRecordEvent.SaveRecord) },
            enabled = isValid && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4DD0C4)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Tiếp tục",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WalletSelector(
    wallets: List<WalletModel>,
    selectedWalletId: Long?,
    onWalletSelected: (Long) -> Unit
) {
    Column {
        Text(
            text = "Từ ví",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(wallets) { wallet ->
                val isSelected = wallet.id == selectedWalletId

                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable {
                            onWalletSelected(wallet.id)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor =
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            wallet.name,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            formatMoney(wallet.balance),
                            color = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).format(Date(millis))
}

private fun formatMoney(amount: Double): String {
    return "%,.0f ₫".format(amount)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddSavingRecordPreview() {
    val mockWallets = listOf(
        WalletModel(
            id = 1,
            userId = "1",
            name = "Ví chính",
            balance = 1_000_000.0
        ),
        WalletModel(
            id = 2,
            userId = "1",
            name = "Ví phụ",
            balance = 500_000.0
        )
    )

    AddSavingRecordContent(
        uiState = AddSavingRecordUiState(
            wallets = mockWallets,
            selectedWalletId = 1L,
            note = "Tiết kiệm"
        ),
        onEvent = {},
        onNavigateBack = {}
    )
}