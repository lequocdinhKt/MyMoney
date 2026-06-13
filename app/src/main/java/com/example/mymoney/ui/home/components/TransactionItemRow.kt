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
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

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
                categoryIconRes = com.example.mymoney.R.drawable.ic_category_expense_noodle,
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
                categoryIconRes = com.example.mymoney.R.drawable.ic_category_income_money,
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
                categoryIconRes = com.example.mymoney.R.drawable.ic_category_expense_car,
                title = "Đổ xăng",
                dateTime = "9:00, 02/04/2026",
                amount = -300_000L,
                formattedAmount = "-300.000"
            ),
            onDelete = {}
        )
    }
}
