package com.example.mymoney.ui.recurring

import android.app.DatePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.domain.model.RecurringTransactionModel
import com.example.mymoney.presentation.viewmodel.recurring.RecurringEvent
import com.example.mymoney.presentation.viewmodel.recurring.RecurringFrequency
import com.example.mymoney.presentation.viewmodel.recurring.RecurringNavEvent
import com.example.mymoney.presentation.viewmodel.recurring.RecurringUiState
import com.example.mymoney.presentation.viewmodel.recurring.RecurringViewModel
import com.example.mymoney.presentation.viewmodel.recurring.RecurringViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RecurringScreen(
    onNavigateBack: () -> Unit,
    walletId: Long = 0L,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vm: RecurringViewModel = viewModel(
        factory = RecurringViewModelFactory(context, walletId)
    )
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.navEvent.collect { event ->
            when (event) {
                is RecurringNavEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    RecurringContent(
        uiState      = uiState,
        onEvent      = vm::onEvent,
        onNavigateBack = onNavigateBack,
        modifier     = modifier
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Content
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringContent(
    uiState: RecurringUiState,
    onEvent: (RecurringEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.statusBarsPadding()
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Giao dịch định kỳ",
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
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    // Wallet chip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(RecurringEvent.OnAddClicked) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm định kỳ")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
                uiState.isEmpty -> EmptyRecurringState(
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> RecurringList(
                    items = uiState.items,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // ── Bottom Sheet: Add / Edit ──
    if (uiState.isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(RecurringEvent.OnSheetDismissed) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            RecurringFormSheet(
                uiState = uiState,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyRecurringState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Update,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Text(
            text = "Chưa có giao dịch định kỳ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Nhấn nút + để thêm giao dịch tự động\nnhư tiền thuê nhà, lương, tiết kiệm...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// List
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecurringList(
    items: List<RecurringTransactionModel>,
    onEvent: (RecurringEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items = items, key = { it.id }) { item ->
            RecurringItemCard(item = item, onEvent = onEvent)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Item card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecurringItemCard(
    item: RecurringTransactionModel,
    onEvent: (RecurringEvent) -> Unit
) {
    val isIncome = item.type == "income"
    val amountColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
    val amountSign  = if (isIncome) "+" else "-"
    val bgColor by animateColorAsState(
        targetValue = if (item.isActive)
            MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "card_bg"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (item.isActive) 2.dp else 0.dp,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Type icon ──
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // ── Info ──
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isActive) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$amountSign${formatDisplayAmount(item.amount)}đ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = amountColor,
                        fontWeight = FontWeight.Medium
                    )
                    // Frequency badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = RecurringFrequency.fromCode(item.frequency).label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tiếp theo: ${formatDate(item.nextDueDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // ── Actions ──
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Switch(
                    checked = item.isActive,
                    onCheckedChange = { onEvent(RecurringEvent.OnToggleActive(item.id, it)) },
                    modifier = Modifier.height(24.dp)
                )
                Row {
                    IconButton(
                        onClick = { onEvent(RecurringEvent.OnEditClicked(item)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit, "Sửa",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onEvent(RecurringEvent.OnDeleteClicked(item.id)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete, "Xóa",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form Sheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecurringFormSheet(
    uiState: RecurringUiState,
    onEvent: (RecurringEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title
        Text(
            text = if (uiState.editingItem == null) "Thêm giao dịch định kỳ" else "Chỉnh sửa định kỳ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Note
        OutlinedTextField(
            value = uiState.formNote,
            onValueChange = { onEvent(RecurringEvent.OnFormNoteChanged(it)) },
            label = { Text("Tên giao dịch") },
            placeholder = { Text("VD: Tiền thuê nhà, Lương, Tiết kiệm...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        // Amount
        OutlinedTextField(
            value = uiState.formAmount,
            onValueChange = { onEvent(RecurringEvent.OnFormAmountChanged(it)) },
            label = { Text("Số tiền (VNĐ)") },
            placeholder = { Text("0") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        // Type selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("expense" to "Chi tiêu", "income" to "Thu nhập").forEach { (code, label) ->
                val selected = uiState.formType == code
                FilterChip(
                    selected = selected,
                    onClick = { onEvent(RecurringEvent.OnFormTypeChanged(code)) },
                    label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (code == "expense")
                            MaterialTheme.colorScheme.errorContainer
                        else Color(0xFFE8F5E9),
                        selectedLabelColor = if (code == "expense")
                            MaterialTheme.colorScheme.onErrorContainer
                        else Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category
        OutlinedTextField(
            value = uiState.formCategory,
            onValueChange = { onEvent(RecurringEvent.OnFormCategoryChanged(it)) },
            label = { Text("Danh mục") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        // Frequency
        Column {
            Text(
                text = "Tần suất",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RecurringFrequency.entries.forEach { freq ->
                    val selected = uiState.formFrequency == freq
                    FilterChip(
                        selected = selected,
                        onClick = { onEvent(RecurringEvent.OnFormFrequencyChanged(freq)) },
                        label = {
                            Text(
                                freq.label.removePrefix("Hàng ").replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Start date
        OutlinedButton(
            onClick = {
                val cal = Calendar.getInstance().apply { timeInMillis = uiState.formStartDate }
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val selected = Calendar.getInstance().apply {
                            set(year, month, day, 8, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        onEvent(RecurringEvent.OnFormStartDateChanged(selected))
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bắt đầu: ${dateFormatter.format(Date(uiState.formStartDate))}")
        }

        // Error message
        if (uiState.formError != null) {
            Text(
                text = uiState.formError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Save button
        Button(
            onClick = { onEvent(RecurringEvent.OnFormSaveClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (uiState.editingItem == null) "Thêm định kỳ" else "Lưu thay đổi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun formatDisplayAmount(amount: Double): String =
    String.format(Locale.US, "%,.0f", amount).replace(',', '.')

private fun formatDate(ms: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ms))

