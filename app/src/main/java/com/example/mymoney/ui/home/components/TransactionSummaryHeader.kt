package com.example.mymoney.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymoney.ui.theme.MyMoneyTheme
import com.example.mymoney.ui.theme.SuccessGreen

/**
 * Header tóm tắt giao dịch theo kỳ, gồm:
 *  - Nhãn ngày/tuần/tháng (groupLabel)
 *  - Hàng: Thu nhập (xanh lá) | Chi tiêu (đỏ) | Số dư (mặc định)
 *
 * @param groupLabel   Nhãn thời gian, ví dụ "Hôm nay, 02 tháng 4"
 * @param totalIncome  Tổng thu đã format, ví dụ "1.000.000"
 * @param totalExpense Tổng chi đã format, ví dụ "1.000.000"
 * @param totalBalance Số dư kỳ đã format, ví dụ "1.000.000"
 */
@Composable
fun TransactionSummaryHeader(
    groupLabel: String,
    totalIncome: String,
    totalExpense: String,
    totalBalance: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // ── Nhãn ngày ──
        Text(
            text = groupLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        // ── Hàng 3 cột: tiêu đề ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryColumn(
                label = "Thu nhập",
                value = totalIncome,
                valueColor = SuccessGreen
            )
            SummaryColumn(
                label = "Chi tiêu",
                value = totalExpense,
                valueColor = MaterialTheme.colorScheme.error
            )
            SummaryColumn(
                label = "Số dư",
                value = totalBalance,
                valueColor = MaterialTheme.colorScheme.onBackground
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Cột nhỏ hiển thị nhãn + giá trị */
@Composable
private fun SummaryColumn(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

// ── Previews ──

@Preview(showBackground = true)
@Composable
private fun TransactionSummaryHeaderLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        TransactionSummaryHeader(
            groupLabel   = "Hôm nay, 02 tháng 4",
            totalIncome  = "1.000.000",
            totalExpense = "1.000.000",
            totalBalance = "1.000.000"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionSummaryHeaderDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        TransactionSummaryHeader(
            groupLabel   = "Hôm nay, 02 tháng 4",
            totalIncome  = "500.000",
            totalExpense = "200.000",
            totalBalance = "300.000"
        )
    }
}
