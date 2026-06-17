package com.example.mymoney.ui.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.data.local.db.AppDatabase
import com.example.mymoney.data.repository.TransactionRepositoryImpl
import com.example.mymoney.presentation.viewmodel.statistics.*
import com.example.mymoney.ui.home.components.TimePeriodFilter
import com.example.mymoney.ui.statistics.components.BarChart
import com.example.mymoney.ui.statistics.components.DonutChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    userId: String,
    onBackClick: () -> Unit,
    onNavigateToBudget: () -> Unit = {},
    viewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(
            TransactionRepositoryImpl(AppDatabase.getInstance(LocalContext.current).transactionDao()),
            userId,
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = {
                        TabSelector(
                            selectedTab = uiState.selectedTab
                        ) { viewModel.onEvent(StatisticsEvent.SelectTab(it)) }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            TimePeriodFilter(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { viewModel.onEvent(StatisticsEvent.SelectPeriod(it)) },
                onCustomPeriodSelected = { from, to -> 
                    viewModel.onEvent(StatisticsEvent.SelectCustomPeriod(from, to)) 
                }
            )

            PeriodSelector(
                label = uiState.periodLabel
            )

            SummaryCards(
                income = uiState.totalIncome,
                expense = uiState.totalExpense,
                selectedType = uiState.selectedType,
                onTypeSelected = { viewModel.onEvent(StatisticsEvent.SelectType(it)) }
            )

            if (uiState.selectedTab == StatisticsTab.DISTRIBUTION) {
                DistributionView(uiState, onNavigateToBudget)
            } else {
                TrendView(uiState)
            }
        }
    }
}

@Composable
fun TabSelector(
    selectedTab: StatisticsTab,
    onTabSelected: (StatisticsTab) -> Unit
) {
    Row(
        modifier = Modifier
            .width(220.dp)
            .height(36.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (selectedTab == StatisticsTab.DISTRIBUTION) MaterialTheme.colorScheme.surface else Color.Transparent,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onTabSelected(StatisticsTab.DISTRIBUTION) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Phân bổ",
                fontSize = 14.sp,
                fontWeight = if (selectedTab == StatisticsTab.DISTRIBUTION) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == StatisticsTab.DISTRIBUTION) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (selectedTab == StatisticsTab.TREND) MaterialTheme.colorScheme.surface else Color.Transparent,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onTabSelected(StatisticsTab.TREND) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Xu hướng",
                fontSize = 14.sp,
                fontWeight = if (selectedTab == StatisticsTab.TREND) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == StatisticsTab.TREND) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PeriodSelector(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SummaryCards(
    income: String, 
    expense: String, 
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onTypeSelected("expense") },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = if (selectedType == "expense") BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text("Chi tiêu", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    expense,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onTypeSelected("income") },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = if (selectedType == "income") BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text("Thu nhập", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    income,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DistributionView(
    uiState: StatisticsUiState,
    onNavigateToBudget: () -> Unit = {}
) {
    val data = if (uiState.selectedType == "expense") uiState.expenseCategories else uiState.incomeCategories
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    data = data,
                    modifier = Modifier.size(200.dp)
                )
            }
        }

        item {
            Button(
                onClick = onNavigateToBudget,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    "Thêm Ngân sách chi tiêu tháng >",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }

        items(data) { item ->
            CategoryStatRow(item)
        }

        item {
            Text(
                "Thu gọn ^",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TrendView(uiState: StatisticsUiState) {
    val data = if (uiState.selectedType == "expense") uiState.expenseCategories else uiState.incomeCategories

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            uiState.trendComparison,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    BarChart(
                        data = uiState.trendData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            }
        }

        items(data) { item ->
            CategoryStatRow(item, showChange = true)
        }
    }
}

@Composable
fun CategoryStatRow(item: CategoryStatsItem, showChange: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot màu tương ứng với biểu đồ
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = Color(android.graphics.Color.parseColor(item.color)),
                    shape = RoundedCornerShape(6.dp)
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(item.categoryIcon.ifEmpty { "📦" })
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.categoryName, 
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!showChange) {
                    Text(
                        "${"%.1f".format(item.percentage)}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    item.formattedAmount, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Default.ChevronRight, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showChange && item.changeAmount.isNotEmpty()) {
                Text(
                    item.changeAmount,
                    fontSize = 12.sp,
                    color = if (item.changeAmount.startsWith("+")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
