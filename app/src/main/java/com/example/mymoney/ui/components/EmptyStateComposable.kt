package com.example.mymoney.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Composable tái sử dụng hiển thị trạng thái rỗng (empty state).
 * Dùng khi danh sách không có dữ liệu.
 *
 * Stateless — nhận toàn bộ dữ liệu qua tham số.
 *
 * @param message   Thông báo hiển thị (VD: "Chưa có giao dịch nào")
 * @param modifier  Modifier tùy chỉnh
 */
@Composable
fun EmptyStateComposable(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon minh hoạ trạng thái rỗng
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null, // Trang trí, không cần mô tả
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Thông báo rỗng
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ── Previews ──

@Preview(showBackground = true)
@Composable
private fun EmptyStateLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        EmptyStateComposable(message = "Chưa có giao dịch nào")
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        EmptyStateComposable(message = "Chưa có giao dịch nào")
    }
}
