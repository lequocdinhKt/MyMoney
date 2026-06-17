@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mymoney.ui.budget

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.MenuAnchorType
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
import com.example.mymoney.ui.theme.MyMoneyTheme
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.mymoney.domain.model.CategoryModel
import com.example.mymoney.presentation.viewmodel.budget.add_budget.BudgetFormEvent
import com.example.mymoney.presentation.viewmodel.budget.add_budget.BudgetFormUiState
import com.example.mymoney.presentation.viewmodel.budget.add_budget.BudgetFormViewModel
import com.example.mymoney.presentation.viewmodel.budget.add_budget.BudgetFormViewModelFactory
import com.example.mymoney.ui.common.SelectionBottomSheet
import com.example.mymoney.ui.common.SelectionLayout
import com.example.mymoney.ui.common.SelectionOption
import com.example.mymoney.ui.common.mapEmojiToDrawable

/**
 * Màn hình Thiết lập ngân sách thủ công — dùng cho cả tạo mới và chỉnh sửa.
 * @param userId         ID người dùng
 * @param budgetId       null = tạo mới, khác null = chỉnh sửa
 * @param onNavigateBack Callback khi nhấn Back hoặc lưu / xóa thành công
 */

@Composable
fun BudgetFormScreen(
    userId: String,
    budgetId: Long? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: BudgetFormViewModel = viewModel(
        factory = BudgetFormViewModelFactory(context, userId, budgetId),
        key = "budget_${budgetId ?: "new"}"
    )
    val uiState by viewModel.uiState.collectAsState()

    // Tự navigate back khi lưu thành công
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onNavigateBack()
    }

    BudgetFormContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun BudgetFormContent(
    uiState: BudgetFormUiState,
    onEvent: (BudgetFormEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isValid = uiState.selectedCategory != null && uiState.currentAmountLimit.isNotBlank()

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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = if (uiState.isEditMode) "Chỉnh sửa ngân sách" else "Tạo ngân sách",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
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
            // Số tiền ngân sách
            OutlinedTextField(
                value = uiState.currentAmountLimit,
                onValueChange = { onEvent(BudgetFormEvent.OnAmountChange(it)) },
                label = { Text("Số tiền ngân sách") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { Text("₫", modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Danh mục",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            CategorySelector(
                selectedCategory = uiState.selectedCategory,
                onEvent = onEvent
            )

            if (uiState.showCategorySheet) {
                SelectionBottomSheet(
                    title = "Danh mục",
                    options = uiState.categories.map { category ->
                        SelectionOption(
                            title = category.name,
                            value = category,
                            image = mapEmojiToDrawable(category.icon)
                        )
                    },
                    selected = uiState.selectedCategory,
                    layout = SelectionLayout.GRID,
                    onSelected = {
                        onEvent(BudgetFormEvent.OnCategorySelected(it))
                    },
                    onDismiss = {
                        onEvent(BudgetFormEvent.DismissCategorySheet)
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            MonthYearSelector(
                month = uiState.month,
                year = uiState.year,
                onMonthSelected = { onEvent(BudgetFormEvent.OnMonthSelected(it)) },
                onYearSelected = { onEvent(BudgetFormEvent.OnYearSelected(it)) }
            )

            Spacer(Modifier.height(8.dp))

            BudgetInfoCard(
                month = uiState.month,
                year = uiState.year,
                isEdit = uiState.isEditMode,
                amountPreview = uiState.currentAmountLimit,
                categoryName =
                    uiState.selectedCategory?.name ?: "Chưa chọn"
            )
        }

        uiState.error?.let { error ->
            Snackbar(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                action = {
                    Text(
                        "OK",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            onEvent(BudgetFormEvent.DismissError)
                        }
                    )
                }
            ) {
                Text(error)
            }
        }

        // ── Nút Tiếp tục ──
        Button(
            onClick = { onEvent(BudgetFormEvent.Save) },
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
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (uiState.isEditMode) "Lưu thay đổi" else "Tiếp tục",
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
private fun CategorySelector(
    selectedCategory: CategoryModel?,
    onEvent: (BudgetFormEvent) -> Unit
) {
    val icon = selectedCategory?.icon
    val title = selectedCategory?.name ?: ""

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Nếu chưa chọn
        if (selectedCategory == null) {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onEvent(BudgetFormEvent.CategoryClicked)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    "＋ Thêm danh mục",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

            }
        } else {
            Row(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onEvent(BudgetFormEvent.CategoryClicked)
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    icon?.let {
                        Icon(
                            painter = painterResource(
                                id = mapEmojiToDrawable(it)
                            ),
                            contentDescription = title,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    "✕",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clickable {
                        onEvent(BudgetFormEvent.ClearCategory)
                    }
                )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
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
    amountPreview: String,
    categoryName: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary,
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
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = if (isEdit) "EDIT" else "CREATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onPrimary,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Row 2: label
            Text(
                text = "Ngân sách chi tiêu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Text(
                text = "Danh mục: $categoryName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            // Row 3: amount preview
            Text(
                text = amountPreview.ifBlank { "0 đ" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BudgetFormAddPreview() {
    BudgetFormContent(
        uiState = BudgetFormUiState(),
        onEvent = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BudgetFormEditPreview() {
    MyMoneyTheme(darkTheme = false) {
        BudgetFormContent(
            uiState = BudgetFormUiState(
                isEditMode = true
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}