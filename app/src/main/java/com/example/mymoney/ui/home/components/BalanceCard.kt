package com.example.mymoney.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Khu vực đầu trang chủ gồm:
 *  - Thẻ số dư màu primary (bo góc + đổ bóng qua Card)
 *  - Nút FAB "+" để thêm giao dịch
 *
 * @param formattedBalance Số dư đã được format sẵn, ví dụ "1.000.000 vnđ"
 * @param onAddClick       Callback khi nhấn nút "+"
 */
@Composable
fun BalanceSection(
    formattedBalance: String,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Thẻ số dư ──
        // Dùng Card thay vì Box+background để có Elevation đúng chuẩn Material3
        Card(
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                pressedElevation  = 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Số dư:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedBalance,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // ── Nút "+" thêm giao dịch ──
        // FAB lớn màu secondaryContainer để tạo tương phản với thẻ xanh
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation  = 2.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm giao dịch",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

// ── Previews ──

@Preview(showBackground = true)
@Composable
private fun BalanceSectionLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        BalanceSection(
            formattedBalance = "1.000.000 vnđ",
            onAddClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceSectionDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        BalanceSection(
            formattedBalance = "1.000.000 vnđ",
            onAddClick = {}
        )
    }
}
