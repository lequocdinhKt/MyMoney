package com.example.mymoney.ui.budget.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymoney.domain.model.BudgetModel
import com.example.mymoney.domain.usecase.MoneyFormatter
import com.example.mymoney.ui.common.LocalMoneyFormatConfig
import androidx.compose.foundation.gestures.detectDragGestures
import java.util.Collections

/**
 * Khu vực đầu ngân sách gồm các thẻ ngân sách cuộn xuống và "xóa ở góc tên bên phải của thẻ
 *
 * Gestures:
 *  - Nhấn đơn       : chọn ngân sách (onSelectBudget)
 *  - Nhấn đúp       : mở màn hình chỉnh sửa ví (onEditBudget)
 *  - Ấn giữ + kéo   : sắp xếp lại ngân sách; khi nhả tay →  onReorderWallets với thứ tự mới
 *
 * Loading Indicator:
 *  - Khi người dùng chọn ngân sách, thẻ đó sẽ hiển thị spinner để indicate dữ liệu đang được load
 *  - Spinner tự động biến mất khi dữ liệu load xong
 *  
 * @param budgets           Danh sách ngân sách từ database
 * @param selectedBudgetId  ID của ngân sách hiện được chọn
 * @param onSelectBudget    Callback khi nhấn ngân sách (chọn ngân sách)
 * @param onDeleteClick     Callback khi nhấn nút "xóa" (xóa ngân sách)
 * @param onEditBudget      Callback khi nhấn đúp thẻ ngân sách (chỉnh sửa)
 * @param onReorderBudget   Callback khi drag & drop kết thúc, nhận danh sách id theo thứ tự mới
 * @param modifier          Modifier tùy chỉnh
 */

@Composable
fun BudgetSection(
    budgets: List<BudgetModel>,
    selectedBudgetId: Long,
    onSelectBudget: (budgetId: Long) -> Unit,
    onDeleteClick: (budgetId: Long) -> Unit,
    onEditBudget: (budgetId: Long) -> Unit,
    onReorderBudget: (orderedIds: List<Long>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Danh sách nội bộ để hoán đổi vị trí khi kéo thả
    val localBudgets = remember { mutableStateListOf<BudgetModel>() }

    // Trạng thái scroll – dùng để reset về thẻ đầu tiên khi danh sách ví thay đổi
    val listState = rememberLazyListState()

    // Trạng thái kéo thả
    var isDragging by remember { mutableStateOf(false) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    // Bước di chuyển mỗi ô: chiều rộng thẻ (200dp) + khoảng cách (12dp)
    val density = LocalDensity.current
    val cardStepPx = with(density) { 212.dp.toPx() }

    // Đồng bộ localWallets với DB, nhưng không làm gián đoạn kéo thả.
    // Sau khi cập nhật, luôn cuộn về card đầu tiên để ưu tiên hiển thị từ đầu.
    LaunchedEffect(budgets) {
        if (!isDragging) {
            localBudgets.clear()
            localBudgets.addAll(budgets)
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        ) {
        itemsIndexed(items = localBudgets, key = { _, b -> b.id }) { index, budget ->
            BudgetCard(
                budget = budget,
                isSelected = budget.id == selectedBudgetId,
                isDragging = index == draggingIndex,
                onClick = { onSelectBudget(budget.id) },
                onDoubleTap = { onEditBudget(budget.id) },
                onDeleteClick = { onDeleteClick(budget.id) },
                onDragStart = {
                    isDragging = true
                    draggingIndex = index
                    dragOffsetX = 0f
                },
                onDrag = { delta ->
                    dragOffsetX += delta
                    val threshold = cardStepPx / 2
                    when {
                        dragOffsetX > threshold && draggingIndex < localBudgets.size - 1 -> {
                            Collections.swap(localBudgets, draggingIndex, draggingIndex + 1)
                            draggingIndex++
                            dragOffsetX -= cardStepPx
                        }

                        dragOffsetX < -threshold && draggingIndex > 0 -> {
                            Collections.swap(localBudgets, draggingIndex, draggingIndex - 1)
                            draggingIndex--
                            dragOffsetX += cardStepPx
                        }
                    }
                },
                onDragEnd = {
                    isDragging = false
                    draggingIndex = -1
                    dragOffsetX = 0f
                    onReorderBudget(localBudgets.map { it.id })
                }
            )
        }
    }
}

// ── Thẻ ngân sách đơn ──
@Composable
private fun BudgetCard(
    budget:BudgetModel,
    isSelected: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    onDoubleTap: () -> Unit,
    onDeleteClick: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (deltaX: Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.03f else 1f,
        label = "budget_scale"
    )
    val fmt = LocalMoneyFormatConfig.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .scale(scale)
            .pointerInput(onClick, onDoubleTap) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleTap() },
                    onLongPress = {
                        onDragStart()
                    }
                )
            }
            .pointerInput(isDragging) {
                if (isDragging) {
                    detectDragGestures(
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    ) { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x)
                    }
                }
            },

        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),

        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Danh mục ${budget.categoryId}",
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        Color.LightGray,
                        RoundedCornerShape(999.dp)
                    )
            )

            Text(
                text = "${MoneyFormatter.format(budget.amountLimit, fmt.useThousandSep)} đ",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// ── Previews ──
@Preview(showBackground = true)
@Composable
fun BudgetSectionPreview() {
    BudgetSection(
        budgets = emptyList(),
        selectedBudgetId = -1L,
        onSelectBudget = {},
        onDeleteClick = {},
        onEditBudget = {},
        onReorderBudget = {}
    )
}

