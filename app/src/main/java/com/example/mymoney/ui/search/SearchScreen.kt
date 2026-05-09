package com.example.mymoney.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.domain.model.TransactionModel
import com.example.mymoney.domain.usecase.MoneyFormatter
import com.example.mymoney.presentation.viewmodel.search.SearchViewModel
import com.example.mymoney.presentation.viewmodel.search.search.FilterType
import com.example.mymoney.presentation.viewmodel.search.search.SearchEvent
import com.example.mymoney.presentation.viewmodel.search.search.SearchUiState
import com.example.mymoney.ui.theme.MyMoneyTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import com.example.mymoney.presentation.viewmodel.search.SearchViewModelFactory
import com.example.mymoney.ui.components.EmptyStateComposable

/**
 * Màn hình Tìm kiếm.
 * UI stateless: chỉ nhận state từ ViewModel, không chứa logic nghiệp vụ.
 */
@Composable
fun SearchScreen (
    modifier: Modifier = Modifier,
    factory: SearchViewModelFactory,
    onBackClick: () -> Unit
) {
    val viewModel: SearchViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    SearchContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
        onBackClick = onBackClick
    )
}

/**
 * Nội dung hiển thị của màn hình Tìm kiếm.
 * Composable thuần tuý – không phụ thuộc ViewModel, dễ test và preview.
 */
@Composable
private fun SearchContent(
    uiState: SearchUiState,
    onEvent: (SearchEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Title
            Text(
                text = "Tìm kiếm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        // Search Bar
        OutlinedTextField(
            value = uiState.query,
            onValueChange = {
                onEvent(SearchEvent.onQueryChange(it))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tìm kiếm...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = {
                        onEvent(SearchEvent.onQueryChange(""))
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )

        Spacer(modifier = Modifier.height(12.dp))


        //Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FilterType.entries) { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = {
                        onEvent(SearchEvent.onFilterChange(filter))
                    },
                    label = {
                        Text(text = filter.displayName())
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nội dung
        when {
            uiState.transactions.isEmpty() -> {
                EmptyStateComposable(message = "Không tìm thấy")
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        uiState.transactions,
                        key = {it.id}
                    ) { tx ->
                        TransactionItem(tx)
                    }
                }
            }
        }
    }
}

// UI Item
@Composable
private fun TransactionItem(tx: TransactionModel) {
    Card (
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    text = tx.note,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = tx.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatDate(tx.timestamp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = MoneyFormatter.formatWithSign(
                    if (tx.type == "expense") -tx.amount else tx.amount
                ),
                color = if (tx.type == "expense")
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Hàm định dang ngày/tháng/năm
private fun formatDate(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)
}

// Hàm tạo các filter chips
private fun FilterType.displayName(): String {
    return when (this) {
        FilterType.All -> "Tất cả"
        FilterType.NAME -> "Tên"
        FilterType.CATEGORY -> "Danh mục"
        FilterType.DAY -> "Ngày"
        FilterType.MONTH -> "Tháng"
        FilterType.YEAR -> "Năm"
    }
}


// ── Previews ──
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SearchScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        SearchContent(
            uiState = SearchUiState(),
            onEvent = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SearchScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        SearchContent(
            uiState = SearchUiState(),
            onEvent = {},
            onBackClick = {}
        )
    }
}

/** Dữ liệu mẫu chỉ dùng trong Preview */
@Preview
@Composable
fun PreviewSearch() {
    val fakeState = SearchUiState()

    SearchContent(
        uiState = fakeState,
        onEvent = {},
        onBackClick = {}
    )
}