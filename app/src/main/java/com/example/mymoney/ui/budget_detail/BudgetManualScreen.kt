@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mymoney.ui.budget_detail

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.budget_details.BudgetManualViewModel
import com.example.mymoney.presentation.viewmodel.budget_details.BudgetManualViewModelFactory
import com.example.mymoney.presentation.viewmodel.budget_details.budget_detail.BudgetManualEvent
import com.example.mymoney.presentation.viewmodel.budget_details.budget_detail.BudgetManualUiState
import com.example.mymoney.ui.theme.MyMoneyTheme
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.mymoney.ui.common.SelectionBottomSheet
import com.example.mymoney.ui.common.SelectionOption


/**
 * Màn hình Thiết lập ngân sách thủ công — dùng cho cả tạo mới và chỉnh sửa.
 * @param userId         ID người dùng
 * @param budgetId       null = tạo mới, khác null = chỉnh sửa
 * @param onNavigateBack Callback khi nhấn Back hoặc lưu / xóa thành công
 */

@Composable
fun BudgetManualScreen(
    userId: String,
    budgetId: Long? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: BudgetManualViewModel = viewModel(
        factory = BudgetManualViewModelFactory(context, userId, budgetId),
        key     = "budget_${budgetId ?: "new"}"
    )
    val uiState by viewModel.uiState.collectAsState()

    // Tự navigate back khi lưu thành công
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onNavigateBack()
    }

    BudgetManualContent(
        uiState        = uiState,
        onEvent        = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun BudgetManualContent(
    uiState: BudgetManualUiState,
    onEvent: (BudgetManualEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isValid = uiState.categoryId > 0 && uiState.currentAmountLimit.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint               = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text       = if (uiState.isEditMode) "Chỉnh sửa ngân sách" else "Tạo ngân sách",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                modifier   = Modifier.weight(1f).padding(start = 4.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        // ── Form ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Số tiền ngân sách
            OutlinedTextField(
                value          = uiState.currentAmountLimit,
                onValueChange  = { onEvent(BudgetManualEvent.OnAmountChange(it)) },
                label          = { Text("Số tiền ngân sách") },
                singleLine     = true,
                shape          = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon  = { Text("₫", modifier = Modifier.padding(end = 12.dp)) },
                modifier       = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text  = "Danh mục",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            CategorySelector(
                selectedId = uiState.categoryId,
                onSelect = {
                    onEvent(BudgetManualEvent.OnCategorySelected(it))
                },
                onEvent = onEvent
            )

            uiState.error?.let { error ->
                Snackbar(
                    action = {
                        Text(
                            "OK",
                            modifier = Modifier.clickable {
                                onEvent(BudgetManualEvent.DismissError)
                            }
                        )
                    }
                ) {
                    Text(error)
                }
            }

            Spacer(Modifier.height(16.dp))

            MonthYearSelector(
                month = uiState.month,
                year = uiState.year,
                onMonthSelected = { onEvent(BudgetManualEvent.OnMonthSelected(it)) },
                onYearSelected = { onEvent(BudgetManualEvent.OnYearSelected(it)) }
            )

            Spacer(Modifier.height(8.dp))

            BudgetInfoCard(
                month = uiState.month,
                year = uiState.year,
                isEdit = uiState.isEditMode,
                amountPreview = uiState.currentAmountLimit
            )
        }

        // ── Nút Tiếp tục ──
        Button(
            onClick = { onEvent(BudgetManualEvent.Save) },
            enabled = isValid && !uiState.isLoading,
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



@Composable
private fun CategorySelector(
    selectedId: Long,
    onSelect: (Long) -> Unit,
    onEvent: (BudgetManualEvent) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Nếu chưa chọn
        if (selectedId <= 0L) {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onEvent(BudgetManualEvent.CategoryClicked)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("＋ Thêm danh mục")
            }
        } else {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onEvent(BudgetManualEvent.CategoryClicked)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Danh mục #$selectedId")
            }
        }
    }
}


@Composable
private fun MonthYearSelector(
    month: Int,
    year: Int,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // ── Dropdown tháng ──
        val months = (1..12).toList()
        var monthExpanded by remember { mutableStateOf(false) }
        var monthText by remember { mutableStateOf("Tháng $month") }

        ExposedDropdownMenuBox(
            expanded = monthExpanded,
            onExpandedChange = { monthExpanded = !monthExpanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = monthText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tháng") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(monthExpanded) },
                shape = RoundedCornerShape(12.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = monthExpanded,
                onDismissRequest = { monthExpanded = false },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
            ) {
                months.forEach { m ->
                    DropdownMenuItem(
                        text = { Text("Tháng $m") },
                        onClick = {
                            monthText = "Tháng $m"
                            onMonthSelected(m)
                            monthExpanded = false
                        }
                    )
                }
            }
        }

        // ── Dropdown năm ──
        val years = (year - 2..year + 2).toList()
        var yearExpanded by remember { mutableStateOf(false) }
        var yearText by remember { mutableStateOf("Năm $year") }

        ExposedDropdownMenuBox(
            expanded = yearExpanded,
            onExpandedChange = { yearExpanded = !yearExpanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = yearText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Năm") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(yearExpanded) },
                shape = RoundedCornerShape(12.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = yearExpanded,
                onDismissRequest = { yearExpanded = false },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
            ) {
                years.forEach { y ->
                    DropdownMenuItem(
                        text = { Text("Năm $y") },
                        onClick = {
                            yearText = "Năm $y"
                            onYearSelected(y)
                            yearExpanded = false
                        }
                    )
                }
            }
        }
    }
}

// Thẻ ngân sách thông báo trước - chỉ có tác dụng trang trí
@Composable
fun BudgetInfoCard(
    month: Int,
    year: Int,
    isEdit: Boolean,
    amountPreview: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

            // Row 1: title + trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tháng $month/$year",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = if (isEdit) "EDIT" else "CREATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Row 2: label
            Text(
                text = "Ngân sách chi tiêu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            // Row 3: amount preview
            Text(
                text = amountPreview.ifBlank { "0 đ" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BudgetManualAddPreview() {
    BudgetManualContent(
        uiState = BudgetManualUiState(),
        onEvent = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BudgetManualEditPreview() {
    MyMoneyTheme(darkTheme = false) {
        BudgetManualContent(
            uiState = BudgetManualUiState(
                isEditMode = true
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}