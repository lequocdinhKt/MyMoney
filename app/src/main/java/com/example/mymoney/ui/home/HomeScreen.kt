package com.example.mymoney.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.mymoney.presentation.viewmodel.home.home.WalletItem
import com.example.mymoney.ui.components.EmptyStateComposable
import com.example.mymoney.ui.components.shimmer.UiStateContainer
import com.example.mymoney.ui.home.components.BalanceSection
import com.example.mymoney.ui.home.components.HomeSkeletonScreen
import com.example.mymoney.ui.home.components.TimePeriodFilter
import com.example.mymoney.ui.home.components.TransactionItemRow
import com.example.mymoney.ui.home.components.TransactionSummaryHeader
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Trang chủ – tab đầu tiên trong Bottom Navigation.
 * UI stateless: chỉ nhận state từ ViewModel, không chứa logic nghiệp vụ.
 */
@Composable
fun HomeScreen(
    factory: HomeViewModelFactory,
    onNavigateToAddWallet: () -> Unit = {},
    onNavigateToEditWallet: (walletId: Long) -> Unit = {},
    onWalletColorChanged: (colorHex: String) -> Unit = {},
    onSelectedWalletIdChanged: (walletId: Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    // Báo màu active wallet lên MainScreen mỗi khi thay đổi
    androidx.compose.runtime.LaunchedEffect(uiState.activeWalletColor) {
        onWalletColorChanged(uiState.activeWalletColor)
    }

    // Báo selectedWalletId lên MainScreen để dùng khi navigate AddTransaction
    androidx.compose.runtime.LaunchedEffect(uiState.selectedWalletId) {
        onSelectedWalletIdChanged(uiState.selectedWalletId)
    }

    HomeContent(
        uiState                = uiState,
        onEvent                = viewModel::onEvent,
        onNavigateToAddWallet  = onNavigateToAddWallet,
        onNavigateToEditWallet = onNavigateToEditWallet,
        modifier               = modifier
    )
}

/**
 * Nội dung hiển thị của màn hình Trang chủ.
 */
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateToAddWallet: () -> Unit = {},
    onNavigateToEditWallet: (walletId: Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    UiStateContainer(
        isLoading = uiState.isLoading,
        modifier  = modifier.fillMaxSize(),
        skeleton  = { HomeSkeletonScreen() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

                // ── 1. Thẻ ví cuộn ngang + Nút "+" ──
                BalanceSection(
                    wallets          = uiState.wallets,
                    selectedWalletId = uiState.selectedWalletId,
                    onSelectWallet   = { id -> onEvent(HomeEvent.SelectWallet(id)) },
                    onAddClick       = { onNavigateToAddWallet() },
                    onEditWallet     = { walletId -> onNavigateToEditWallet(walletId) },
                    onReorderWallets = { orderedIds -> onEvent(HomeEvent.ReorderWallets(orderedIds)) }
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
                    selectedPeriod         = uiState.selectedPeriod,
                    onPeriodSelected       = { period -> onEvent(HomeEvent.SelectPeriod(period)) },
                    onCustomPeriodSelected = { from, to -> onEvent(HomeEvent.SelectCustomPeriod(from, to)) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── 4. Danh sách giao dịch ──
                if (uiState.transactions.isEmpty()) {
                    EmptyStateComposable(message = "Chưa có giao dịch nào trong kỳ này")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item(key = "summary_header", contentType = "header") {
                            TransactionSummaryHeader(
                                groupLabel   = uiState.groupLabel,
                                totalIncome  = uiState.totalIncome,
                                totalExpense = uiState.totalExpense,
                                totalBalance = uiState.totalBalance
                            )
                        }
                        items(
                            items       = uiState.transactions,
                            key         = { it.id },
                            contentType = { "transaction" }
                        ) { transaction ->
                            TransactionItemRow(
                                transaction = transaction,
                                onDelete = { onEvent(HomeEvent.RequestDeleteTransaction(transaction.id)) },
                                isDeleting = uiState.transactionToDeleteId == transaction.id
                            )
                            HorizontalDivider(
                                color    = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
        }
    }

    if (uiState.transactionToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { onEvent(HomeEvent.CancelDeleteTransaction) },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa giao dịch này không?") },
            confirmButton = {
                Button(
                    onClick = { onEvent(HomeEvent.ConfirmDeleteTransaction) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(HomeEvent.CancelDeleteTransaction) }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (uiState.showCreateWalletDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(HomeEvent.DismissCreateWalletDialog) },
            title = { Text("Tạo ví đầu tiên") },
            text = {
                Text(
                    "Bạn chưa có ví nào. Hãy tạo ví đầu tiên để bắt đầu quản lý tài chính.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { onNavigateToAddWallet() }) { Text("Tạo ví") }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(HomeEvent.DismissCreateWalletDialog) }) { Text("Để sau") }
            }
        )
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        HomeContent(
            uiState = HomeUiState(
                isLoading      = false,
                wallets        = listOf(WalletItem(1L, "Ví chính", "1.000.000 vnđ", "#0088F0")),
                selectedPeriod = TimePeriod.DAY,
                groupLabel     = "Hôm nay, 02 tháng 4",
                totalIncome    = "1.000.000",
                totalExpense   = "1.000.000",
                totalBalance   = "1.000.000",
                transactions   = previewTransactions()
            ),
            onEvent = {}
        )
    }
}

private fun previewTransactions() = listOf(
    TransactionItem("1", com.example.mymoney.R.drawable.ic_category_expense_noodle, "Ăn sáng", "7:00, 02/04/2026", -50_000L, "-50.000"),
    TransactionItem("2", com.example.mymoney.R.drawable.ic_category_income_money, "Lương tháng 4", "8:00, 01/04/2026", 10_000_000L, "+10.000.000"),
    TransactionItem("3", com.example.mymoney.R.drawable.ic_category_expense_bill, "Tiền điện", "9:00, 02/04/2026", -300_000L, "-300.000"),
    TransactionItem("4", com.example.mymoney.R.drawable.ic_category_expense_noodle, "Cafe sáng", "7:30, 02/04/2026", -35_000L, "-35.000"),
    TransactionItem("5", com.example.mymoney.R.drawable.ic_category_income_bonus, "Thưởng dự án", "10:00, 02/04/2026", 2_000_000L, "+2.000.000"),
)
