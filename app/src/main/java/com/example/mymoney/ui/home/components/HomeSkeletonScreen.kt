package com.example.mymoney.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymoney.ui.components.shimmer.ShimmerCard
import com.example.mymoney.ui.components.shimmer.ShimmerCircle
import com.example.mymoney.ui.components.shimmer.ShimmerLine
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Skeleton của HomeScreen – layout mirror chính xác với HomeContent.
 * Được hiển thị tự động khi [UiStateContainer] nhận [isLoading = true].
 *
 * Layout ghép:
 *  - LazyRow  : 3 skeleton card ví
 *  - Divider
 *  - Tiêu đề section (line 50%)
 *  - LazyRow  : 4 skeleton filter chip
 *  - LazyColumn:
 *      header  : 3 cột tóm tắt (Thu / Chi / Số dư)
 *      items×6 : skeleton dòng giao dịch (icon tròn + 2 dòng text + số tiền)
 *
 * @param darkTheme Truyền `true` ở dark mode để dùng palette shimmer tối
 */
@Composable
fun HomeSkeletonScreen(
    modifier:  Modifier = Modifier,
    darkTheme: Boolean  = false
) {
    Column(modifier = modifier.fillMaxSize()) {

        // ── 1. Skeleton khu vực ví ngang ──
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled     = false
        ) {
            items(3) {
                ShimmerCard(
                    modifier  = Modifier
                        .width(200.dp)
                        .height(100.dp),
                    darkTheme = darkTheme
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        // ── 2. Skeleton tiêu đề "Lịch sử giao dịch:" ──
        ShimmerLine(
            modifier  = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            height    = 18.dp,
            fraction  = 0.5f,
            darkTheme = darkTheme
        )

        // ── 3. Skeleton TimePeriodFilter ──
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled     = false
        ) {
            items(4) {
                ShimmerCard(
                    modifier  = Modifier
                        .width(72.dp)
                        .height(32.dp),
                    darkTheme = darkTheme
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── 4. Skeleton danh sách giao dịch ──
        LazyColumn(userScrollEnabled = false) {

            // Header tóm tắt (Thu nhập / Chi tiêu / Số dư)
            item(key = "skeleton_summary") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(3) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ShimmerLine(
                                modifier  = Modifier.width(60.dp),
                                height    = 12.dp,
                                darkTheme = darkTheme
                            )
                            ShimmerLine(
                                modifier  = Modifier.width(80.dp),
                                height    = 18.dp,
                                darkTheme = darkTheme
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            // Dòng giao dịch skeleton × 6
            items(count = 6, key = { "skeleton_tx_$it" }) {
                TransactionSkeletonRow(darkTheme = darkTheme)
                HorizontalDivider(
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

/** Skeleton của một dòng [TransactionItemRow] – layout mirror chính xác */
@Composable
private fun TransactionSkeletonRow(darkTheme: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Trái: icon tròn + 2 dòng text
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            ShimmerCircle(size = 40.dp, darkTheme = darkTheme)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                ShimmerLine(modifier = Modifier.width(130.dp), height = 14.dp, darkTheme = darkTheme)
                ShimmerLine(modifier = Modifier.width(90.dp),  height = 12.dp, darkTheme = darkTheme)
            }
        }
        // Phải: số tiền
        ShimmerLine(modifier = Modifier.width(70.dp), height = 16.dp, darkTheme = darkTheme)
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true, name = "Home Skeleton - Light")
@Composable
private fun HomeSkeletonLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        HomeSkeletonScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home Skeleton - Dark")
@Composable
private fun HomeSkeletonDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        HomeSkeletonScreen(darkTheme = true)
    }
}

