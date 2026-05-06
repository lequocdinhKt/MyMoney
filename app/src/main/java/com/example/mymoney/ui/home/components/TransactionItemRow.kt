package com.example.mymoney.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymoney.presentation.viewmodel.home.home.TransactionItem
import com.example.mymoney.ui.theme.MyMoneyTheme
import com.example.mymoney.ui.theme.SuccessGreen

/**
 * Composable hiển thị một dòng giao dịch trong LazyColumn.
 * Hỗ trợ vuốt từ phải sang trái để xóa giao dịch.
 *
 * @param transaction     Dữ liệu dòng giao dịch
 * @param onDelete        Callback khi người dùng xác nhận xóa
 * @param modifier        Modifier tuỳ chỉnh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItemRow(
    transaction: TransactionItem,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val amountColor = remember(transaction.amount) {
        if (transaction.amount >= 0) SuccessGreen else null
    }

    if (onDelete != null) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.EndToStart) {
                    onDelete()
                    true
                } else false
            },
            positionalThreshold = { totalDistance -> totalDistance * 0.35f }
        )

        // Reset state sau khi xóa để tránh item bị dismiss ở trạng thái kẹt
        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                dismissState.reset()
            }
        }

        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                // Nền đỏ + icon thùng rác hiện khi vuốt trái
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE53935))
                        .padding(end = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa giao dịch",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            modifier = modifier
        ) {
            TransactionRowContent(transaction = transaction, amountColor = amountColor)
        }
    } else {
        TransactionRowContent(
            transaction = transaction,
            amountColor = amountColor,
            modifier = modifier
        )
    }
}

@Composable
private fun TransactionRowContent(
    transaction: TransactionItem,
    amountColor: Color?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Icon danh mục (hình tròn xám) ──
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (transaction.categoryIconRes != null) {
                Icon(
                    painter = painterResource(id = transaction.categoryIconRes),
                    contentDescription = transaction.title,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Placeholder icon khi chưa có resource
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ── Tiêu đề + Thời gian ──
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                text = transaction.dateTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        // ── Số tiền ──
        // Dùng MaterialTheme.colorScheme.error trong Composable scope (không thể trong remember)
        val resolvedAmountColor = amountColor ?: MaterialTheme.colorScheme.error
        Text(
            text = transaction.formattedAmount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = resolvedAmountColor
        )
    }
}

// ── Previews ──

@Preview(showBackground = true)
@Composable
private fun TransactionItemExpensePreview() {
    MyMoneyTheme(darkTheme = false) {
        TransactionItemRow(
            transaction = TransactionItem(
                id = "1",
                categoryIconRes = null,
                title = "Ăn sáng",
                dateTime = "7:00, 02/04/2026",
                amount = -50_000L,
                formattedAmount = "-50.000"
            ),
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemIncomePreview() {
    MyMoneyTheme(darkTheme = false) {
        TransactionItemRow(
            transaction = TransactionItem(
                id = "2",
                categoryIconRes = null,
                title = "Lương tháng 4",
                dateTime = "8:00, 01/04/2026",
                amount = 10_000_000L,
                formattedAmount = "+10.000.000"
            ),
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionItemDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        TransactionItemRow(
            transaction = TransactionItem(
                id = "3",
                categoryIconRes = null,
                title = "Tiền điện",
                dateTime = "9:00, 02/04/2026",
                amount = -300_000L,
                formattedAmount = "-300.000"
            ),
            onDelete = {}
        )
    }
}
