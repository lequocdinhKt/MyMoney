@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mymoney.ui.saving

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.saving.add_saving.AddSavingEvent
import com.example.mymoney.presentation.viewmodel.saving.add_saving.AddSavingFormUiState
import com.example.mymoney.presentation.viewmodel.saving.add_saving.AddSavingFormViewModel
import com.example.mymoney.presentation.viewmodel.saving.add_saving.AddSavingFormViewModelFactory
import com.example.mymoney.presentation.viewmodel.saving.add_saving.RecurringType
import com.example.mymoney.presentation.viewmodel.saving.add_saving.SavingMode
import com.example.mymoney.ui.common.SelectionBottomSheet
import com.example.mymoney.ui.common.SelectionLayout
import com.example.mymoney.ui.common.SelectionOption
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun SavingFormScreen(
    userId: String,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AddSavingFormViewModel = viewModel (
        factory = AddSavingFormViewModelFactory(context, userId)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Tự navigate back khi lưu thành công
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    SavingFormContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun SavingFormContent(
    uiState: AddSavingFormUiState,
    onEvent: (AddSavingEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val cleanAmount = uiState.amount.replace(".", "").replace(",", "").toLongOrNull() ?: 0L
    val isValid = uiState.title.isNotBlank() && cleanAmount > 0

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
                text = "Tạo mục tiêu tiết kiệm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight .SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
        }

        Text(
            text = "Đặt mục tiêu một lần hoặc kế hoạch tiết kiệm định kỳ (hàng tuần hoặc hàng tháng).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        SavingModeSelector(
            selected = uiState.mode,
            onSelected = {
                onEvent(
                    AddSavingEvent.OnModeChanged(it)
                )
            }
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
                value = uiState.title,
                onValueChange = { onEvent(AddSavingEvent.OnTitleChanged(it)) },
                label = { Text("Tiêu đề") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { onEvent(AddSavingEvent.OnAmountChanged(it)) },
                label = { Text("Số tiền tiết kiệm") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { Text("₫", modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.fillMaxWidth(),
            )

            CurrencySelector(
                currency = uiState.currency,
                onClick = {
                    onEvent(AddSavingEvent.OnShowCurrencySheet)
                }
            )

            if (uiState.showCurrencySheet) {
                CurrencySheet(
                    selected = uiState.currency,
                    onSelected = {
                        onEvent(AddSavingEvent.OnCurrencyChanged(it))
                    },
                    onDismiss = {
                        onEvent(AddSavingEvent.OnDismissCurrencySheet)
                    }
                )
            }

            if (uiState.showDatePicker) {
                SavingDatePickerDialog(
                    initialDate = uiState.targetDate,
                    onDateSelected = {
                        onEvent(
                            AddSavingEvent.OnTargetDateSelected(it)
                        )
                    },
                    onDismiss = {
                        onEvent(
                            AddSavingEvent.OnDismissDatePicker
                        )
                    }
                )
            }

            if (uiState.mode == SavingMode.ONE_TIME) {
                TargetDateSelector(
                    date = uiState.targetDate,
                    onClick = {
                        // mở date picker
                        onEvent(AddSavingEvent.OnShowDatePicker)
                    }
                )
            } else {
                RecurringSelector(
                    selected = uiState.recurringType,
                    onSelected = { onEvent(AddSavingEvent.OnRecurringTypeSelected(it)) }
                )
            }
        }

        Button(
            onClick = { onEvent(AddSavingEvent.OnSave) },
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

        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun SavingModeSelector(
    selected: SavingMode,
    onSelected: (SavingMode) -> Unit
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedBorder = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        OutlinedButton(
            onClick = { onSelected(SavingMode.ONE_TIME) },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                if (selected == SavingMode.ONE_TIME) selectedColor else unselectedBorder
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selected == SavingMode.ONE_TIME)
                    selectedColor
                else
                    Color.Transparent,
                contentColor = if (selected == SavingMode.ONE_TIME)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
            Spacer(modifier = Modifier.width(6.dp))

            Text("Một lần")
        }

        OutlinedButton(
            onClick = { onSelected(SavingMode.RECURRING) },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                if (selected == SavingMode.RECURRING) selectedColor else unselectedBorder
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selected == SavingMode.RECURRING)
                    selectedColor
                else
                    Color.Transparent,
                contentColor = if (selected == SavingMode.RECURRING)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text("Định kỳ")
        }
    }
}

@Composable
private fun CurrencySelector(
    currency: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp) // Bằng chiều cao mặc định của OutlinedTextField
            .padding(top = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tiền tệ",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currency,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CurrencySheet(
    selected: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    SelectionBottomSheet(
        title = "Tiền tệ",
        options = listOf(
            SelectionOption("VNĐ", "VNĐ"),
            SelectionOption("USD", "USD"),
            SelectionOption("EUR", "EUR")
        ),
        selected = selected,
        onSelected = {
            onSelected(it)
        },
        onDismiss = onDismiss,
        layout = SelectionLayout.LIST
    )
}

@Composable
private fun RecurringSelector(
    selected: RecurringType,
    onSelected: (RecurringType) -> Unit
) {
    val options = listOf(
        RecurringType.WEEKLY to "Hàng tuần",
        RecurringType.MONTHLY to "Hàng tháng"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = options.first { it.first == selected }.second,
            onValueChange = {},
            readOnly = true,
            label = { Text("Chu kỳ tiết kiệm") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
            ,
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (type, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TargetDateSelector(
    date: LocalDate,
    onClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    OutlinedTextField(
        value = date.format(formatter),
        onValueChange = {},
        readOnly = true,
        label = {
            Text("Ngày mục tiêu")
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Chọn ngày",
                modifier = Modifier.clickable {
                    onClick()
                }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun SavingDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate =
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Hủy")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = null,
            headline = null,
            showModeToggle = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddSavingFormPreview() {
    SavingFormContent(
        uiState = AddSavingFormUiState(),
        onEvent = {},
        onNavigateBack = {}
    )
}