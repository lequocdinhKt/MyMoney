package com.example.mymoney.ui.addtransaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.presentation.viewmodel.addtransaction.AddTransactionViewModel
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionEvent
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionNavEvent
import com.example.mymoney.presentation.viewmodel.addtransaction.addtransaction.AddTransactionUiState
import com.example.mymoney.ui.components.EmptyStateComposable
import com.example.mymoney.ui.theme.MyMoneyTheme
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// AddTransactionScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: AddTransactionViewModel = viewModel(
        factory = AddTransactionViewModel.factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is AddTransactionNavEvent.NavigateBack -> onNavigateBack()
                is AddTransactionNavEvent.NavigateToParseSettings -> { /* TODO */ }
            }
        }
    }

    AddTransactionContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Content — Không dùng imePadding, không dùng Scaffold
//
// Cách cố định TopBar khi mở bàn phím:
//   - Dùng Column bao ngoài toàn bộ màn hình
//   - TopBar (Surface) nằm ở trên: KHÔNG có bất kỳ IME inset nào → luôn cố định
//   - Box(weight=1f): vùng nội dung co giãn theo không gian còn lại
//   - BottomInputCard: dùng windowInsetsPadding(WindowInsets.ime) để tự đẩy lên
//     đúng bằng chiều cao bàn phím, không khoảng cách thừa
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionContent(
    uiState: AddTransactionUiState,
    onEvent: (AddTransactionEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── TopBar CỐ ĐỊNH — nằm ngoài bất kỳ IME inset nào ──
        // Surface đảm bảo elevation + màu nền không bị ảnh hưởng
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),        // chỉ account for status bar
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "cuộc trò chuyện kéo dài 48 giờ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEvent(AddTransactionEvent.OnParseSettingsClicked) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cài đặt"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // ── Wallet chip: icon AccountBalanceWallet cố định + tên ví ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Icon ví — luôn cố định, không thay đổi theo tên ví
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            // Tên ví — thay đổi theo walletName trong UiState
                            Text(
                                text = uiState.walletName,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ── Phần content dưới TopBar: imePadding() ở đây ──
        // TopBar nằm NGOÀI Column này → không bị ảnh hưởng bởi bàn phím
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
        ) {
            // ── Vùng nội dung co giãn: list hoặc empty state ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.isEmpty -> {
                        EmptyStateComposable(
                            message = "Chưa có giao dịch nào",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        TransactionList(
                            transactions = uiState.transactions,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // ── Bottom Input Card ──
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomInputCard(
                    noteInput = uiState.noteInput,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BottomInputCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BottomInputCard(
    noteInput: String,
    onEvent: (AddTransactionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // ── Chips ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AssistChip(
                onClick = { onEvent(AddTransactionEvent.OnTransferFundClicked) },
                label = { Text("Di chuyển quỹ", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(18.dp))
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
            AssistChip(
                onClick = { onEvent(AddTransactionEvent.OnRecurringClicked) },
                label = { Text("Giao dịch định kỳ", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    Icon(Icons.Default.Update, null, modifier = Modifier.size(18.dp))
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── TextField + Submit FAB cùng hàng ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // TextField chiếm toàn bộ phần còn lại
            OutlinedTextField(
                value = noteInput,
                onValueChange = { onEvent(AddTransactionEvent.OnNoteChanged(it)) },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Bữa tối 100k, mua sắm 400k",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )

            // Submit FAB — cùng hàng, bên phải TextField, khoảng cách 8dp
            FloatingActionButton(
                onClick = { onEvent(AddTransactionEvent.OnSubmitClicked) },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, "Gửi giao dịch", modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Hàng icon: Camera + Mic + Settings ──
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { onEvent(AddTransactionEvent.OnCameraClicked) }) {
                Icon(Icons.Default.CameraAlt, "Chụp hoá đơn",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = { onEvent(AddTransactionEvent.OnMicClicked) }) {
                Icon(Icons.Default.Mic, "Nhập bằng giọng nói",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = { onEvent(AddTransactionEvent.OnParseSettingsClicked) }) {
                Icon(Icons.Default.Settings, "Cài đặt phân tích",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TransactionList
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TransactionList(
    transactions: List<TransactionModel>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll xuống tin nhắn mới nhất khi danh sách thay đổi
    // reverseLayout = true → index 0 = tin nhắn mới nhất (dưới cùng)
    LaunchedEffect(transactions.size) {
        if (transactions.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(horizontal = 16.dp),
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp)
    ) {
        items(items = transactions, key = { it.id }) { transaction ->
            TransactionItem(transaction = transaction)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TransactionItem
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TransactionItem(
    transaction: TransactionModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = transaction.note,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (transaction.amount != 0.0)
                "${String.format(Locale.getDefault(), "%,.0f", transaction.amount)}đ"
            else "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (transaction.type == "income") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
            textAlign = TextAlign.End
        )
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddTransactionEmptyPreview() {
    MyMoneyTheme(darkTheme = false) {
        AddTransactionContent(
            uiState = AddTransactionUiState(isLoading = false, isEmpty = true),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddTransactionWithDataPreview() {
    MyMoneyTheme(darkTheme = false) {
        AddTransactionContent(
            uiState = AddTransactionUiState(
                isLoading = false,
                isEmpty = false,
                transactions = listOf(
                    TransactionModel(id = 1, note = "Bữa tối", amount = 100000.0),
                    TransactionModel(id = 2, note = "Mua sắm", amount = 400000.0)
                )
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddTransactionDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        AddTransactionContent(
            uiState = AddTransactionUiState(isLoading = false, isEmpty = true),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}
