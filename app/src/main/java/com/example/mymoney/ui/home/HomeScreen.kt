package com.example.mymoney.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.home.HomeViewModel
import com.example.mymoney.presentation.viewmodel.home.HomeViewModelFactory
import com.example.mymoney.presentation.viewmodel.home.home.HomeEvent
import com.example.mymoney.presentation.viewmodel.home.home.HomeUiState
import com.example.mymoney.presentation.viewmodel.home.home.TimePeriod
import com.example.mymoney.presentation.viewmodel.home.home.TransactionItem
import com.example.mymoney.ui.components.EmptyStateComposable
import com.example.mymoney.ui.home.components.BalanceSection
import com.example.mymoney.ui.home.components.TimePeriodFilter
import com.example.mymoney.ui.home.components.TransactionItemRow
import com.example.mymoney.ui.home.components.TransactionSummaryHeader
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Trang chủ – tab đầu tiên trong Bottom Navigation.
 * UI stateless: chỉ nhận state từ ViewModel, không chứa logic nghiệp vụ.
 *
 * Cấu trúc:
 *   Column
 *     ├── BalanceSection        (Thẻ số dư + Nút "+")
 *     ├── HorizontalDivider
 *     ├── Text "Lịch sử giao dịch:"
 *     ├── TimePeriodFilter      (Ngày / Tuần / Tháng / Năm / ...)
 *     └── LazyColumn
 *           ├── header: TransactionSummaryHeader
 *           └── items : TransactionItemRow × N  (key = id, contentType = "transaction")
 *
 * @param viewModel ViewModel cung cấp trạng thái cho màn hình
 * @param modifier  Modifier tuỳ chỉnh từ bên ngoài (thường là innerPadding từ Scaffold)
 */
@Composable
fun HomeScreen(
    factory: HomeViewModelFactory,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        uiState   = uiState,
        onEvent   = viewModel::onEvent,
        modifier  = modifier
    )
}

/**
 * Nội dung hiển thị của màn hình Trang chủ.
 * Composable thuần tuý – không phụ thuộc ViewModel, dễ test và preview.
 */
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        // ── Skeleton / spinner: hiển thị khi đang load lần đầu ──
        // Dùng AnimatedVisibility + fadeOut để transition mượt, không flash
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(),
            exit  = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // ── Nội dung thực: chỉ hiện sau khi load xong ──
        AnimatedVisibility(
            visible = !uiState.isLoading,
            enter = fadeIn(),
            exit  = fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── 1. Thẻ số dư + Nút "+" ──
                BalanceSection(
                    formattedBalance = uiState.formattedBalance,
                    onAddClick = { onEvent(HomeEvent.AddTransactionClick) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // ── 2. Tiêu đề section ──
                Text(
                    text = "Lịch sử giao dịch:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // ── 3. Bộ lọc thời gian ──
                TimePeriodFilter(
                    selectedPeriod   = uiState.selectedPeriod,
                    onPeriodSelected = { period -> onEvent(HomeEvent.SelectPeriod(period)) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── 4. Danh sách giao dịch ──
                if (uiState.transactions.isEmpty()) {
                    EmptyStateComposable(message = "Chưa có giao dịch nào trong kỳ này")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // Sticky header: tóm tắt Thu nhập / Chi tiêu / Số dư
                        item(key = "summary_header", contentType = "header") {
                            TransactionSummaryHeader(
                                groupLabel   = uiState.groupLabel,
                                totalIncome  = uiState.totalIncome,
                                totalExpense = uiState.totalExpense,
                                totalBalance = uiState.totalBalance
                            )
                        }
                        // Danh sách giao dịch
                        // key = id       → Compose chỉ recompose đúng item thay đổi
                        // contentType    → tái sử dụng node khi scroll (tương tự RecyclerView)
                        items(
                            items       = uiState.transactions,
                            key         = { it.id },
                            contentType = { "transaction" }
                        ) { transaction ->
                            TransactionItemRow(transaction = transaction)
                            HorizontalDivider(
                                color    = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        HomeContent(
            uiState = HomeUiState(
                isLoading        = false,
                formattedBalance = "1.000.000 vnđ",
                selectedPeriod   = TimePeriod.DAY,
                groupLabel       = "Hôm nay, 02 tháng 4",
                totalIncome      = "1.000.000",
                totalExpense     = "1.000.000",
                totalBalance     = "1.000.000",
                transactions     = previewTransactions()
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        HomeContent(
            uiState = HomeUiState(
                isLoading        = false,
                formattedBalance = "1.000.000 vnđ",
                selectedPeriod   = TimePeriod.MONTH,
                groupLabel       = "Tháng 4, 2026",
                totalIncome      = "5.000.000",
                totalExpense     = "3.000.000",
                totalBalance     = "2.000.000",
                transactions     = previewTransactions()
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenEmptyPreview() {
    MyMoneyTheme(darkTheme = false) {
        HomeContent(
            uiState = HomeUiState(isLoading = false, transactions = emptyList()),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenLoadingPreview() {
    MyMoneyTheme(darkTheme = false) {
        HomeContent(
            uiState = HomeUiState(isLoading = true),
            onEvent = {}
        )
    }
}

/** Dữ liệu mẫu chỉ dùng trong Preview */
private fun previewTransactions() = listOf(
    TransactionItem("1", null, "Ăn sáng",      "7:00, 02/04/2026",  -50_000L,     "-50.000"),
    TransactionItem("2", null, "Lương tháng 4", "8:00, 01/04/2026",  10_000_000L, "+10.000.000"),
    TransactionItem("3", null, "Tiền điện",     "9:00, 02/04/2026",  -300_000L,   "-300.000"),
    TransactionItem("4", null, "Cafe sáng",     "7:30, 02/04/2026",  -35_000L,    "-35.000"),
    TransactionItem("5", null, "Thưởng dự án",  "10:00, 02/04/2026", 2_000_000L,  "+2.000.000"),
)
