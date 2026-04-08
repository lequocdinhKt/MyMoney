package com.example.mymoney.ui.main.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Thanh Top Bar tùy chỉnh cho các màn hình chính.
 *
 * Composable HOÀN TOÀN stateless – nhận toàn bộ dữ liệu và callback từ bên ngoài.
 * Không truy cập ViewModel, Repository, hay bất kỳ logic nghiệp vụ nào.
 *
 * @param title          Tiêu đề hiển thị (lấy từ BottomTab.title)
 * @param onSettingsClick Callback khi nhấn icon Settings (bên trái)
 * @param onSearchClick   Callback khi nhấn icon Search (bên phải)
 * @param onCalendarClick Callback khi nhấn icon Calendar (bên phải)
 * @param modifier        Modifier tùy chỉnh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    title: String,
    showback: Boolean = false,
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        // ── Tiêu đề ──
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        // ── Icon trái: Settings ──
        navigationIcon = {
            if (showback) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            } else {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        },
        // ── Icon phải: Search + Calendar ──
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tìm kiếm",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onCalendarClick) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Lịch",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        // ── Màu sắc: theo Material3 theme ──
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// ── Previews ──

@Preview(showBackground = true)
@Composable
private fun CustomTopAppBarLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        CustomTopAppBar(
            title = "Trang chủ",
            onSettingsClick = {},
            onSearchClick = {},
            onCalendarClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomTopAppBarDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        CustomTopAppBar(
            title = "Ngân sách",
            onSettingsClick = {},
            onSearchClick = {},
            onCalendarClick = {}
        )
    }
}
