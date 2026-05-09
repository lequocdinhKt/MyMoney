package com.example.mymoney.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymoney.presentation.viewmodel.home.home.TimePeriod
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Hàng nút lọc thời gian: Ngày | Tuần | Tháng | Năm | ...
 *
 * Khi nhấn "..." (CUSTOM) → mở [DateRangePickerDialog].
 * Sau khi confirm → gọi [onCustomPeriodSelected] với (fromMs, toMs).
 *
 * @param selectedPeriod         Period hiện đang được chọn
 * @param onPeriodSelected       Callback khi chọn period có sẵn (DAY/WEEK/MONTH/YEAR)
 * @param onCustomPeriodSelected Callback khi người dùng confirm date range tùy chỉnh
 */
@Composable
fun TimePeriodFilter(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier,
    onCustomPeriodSelected: (fromMs: Long, toMs: Long) -> Unit = { _, _ -> }
) {
    var showDatePicker by remember { mutableStateOf(false) }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(TimePeriod.entries, key = { it.name }) { period ->
            // Click handler: CUSTOM selalu buka dialog, lainnya langsung pilih
            val onClick: () -> Unit = if (period == TimePeriod.CUSTOM) {
                { showDatePicker = true }
            } else {
                { onPeriodSelected(period) }
            }

            if (period == selectedPeriod) {
                Button(
                    onClick        = onClick,
                    shape          = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors         = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = period.label, style = MaterialTheme.typography.labelMedium)
                }
            } else {
                OutlinedButton(
                    onClick        = onClick,
                    shape          = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors         = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                ) {
                    Text(text = period.label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    // Dialog chọn ngày tùy chỉnh
    if (showDatePicker) {
        DateRangePickerDialog(
            onConfirm = { fromMs, toMs ->
                showDatePicker = false
                onCustomPeriodSelected(fromMs, toMs)
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

// ── Previews ──

@Preview(showBackground = true)
@Composable
private fun TimePeriodFilterLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        TimePeriodFilter(selectedPeriod = TimePeriod.DAY, onPeriodSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun TimePeriodFilterDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        TimePeriodFilter(selectedPeriod = TimePeriod.MONTH, onPeriodSelected = {})
    }
}
