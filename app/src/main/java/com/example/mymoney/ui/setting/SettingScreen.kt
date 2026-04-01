package com.example.mymoney.ui.setting

import androidx.compose.material3.Switch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.setting.SettingViewModel
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingEvent
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingItem
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingNavEvent
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingUiState
import com.example.mymoney.ui.theme.MyMoneyTheme

/** * Màn hình Cài đặt.
 * * UI stateless: chỉ nhận state từ ViewModel, không chứa logic nghiệp vụ.
 * */

@Composable
fun SettingScreen(
    onItemClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: SettingViewModel = viewModel(
        factory = SettingViewModel.factory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val items = remember(uiState) {
        buildSettingItems(uiState)
    }

    // Collect navigation side-effect — chạy 1 lần, lắng nghe liên tục
    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is SettingNavEvent.NavigateToSignIn -> onSignOut()
            }
        }
    }

    SettingContent(
        items = items,
        username = uiState.username,
        onToggleThousandSeparator = {
            viewModel.onEvent(SettingEvent.ToggleThousandSeparator(it))
        },
        onItemClick = { route ->
            if (route == "logout") {
                viewModel.onEvent(SettingEvent.SignOut)
            } else {
                onItemClick()
            }
        }
    )
}

/** * Nội dung hiển thị của màn hình Cài đặt.
 * * Composable thuần tuý – không phụ thuộc ViewModel, dễ test và preview.
 * */

@Composable
fun SettingContent(
    items: List<SettingItem>,
    onToggleThousandSeparator: (Boolean) -> Unit,
    onItemClick: (route: String) -> Unit,
    username: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header: chỉ hiện username ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (username.isNotBlank()) username else "...",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Divider
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        // LIST MENU
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                when (item) {
                    is SettingItem.SettingNavigation -> {
                        SettingNavigationItem(
                            item = item,
                            onItemClick = { onItemClick(item.route) }
                        )
                    }

                    is SettingItem.SettingToggle -> {
                        SettingToggleItem(
                            item = item,
                            onCheckedChange = { isChecked ->
                                onToggleThousandSeparator(isChecked)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingNavigationItem(
    item: SettingItem.SettingNavigation,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = item.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
        )
        Icon(
            imageVector = item.icon,
            contentDescription = null
        )
    }
}

@Composable
fun SettingToggleItem(
    item: SettingItem.SettingToggle,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Switch(
            checked = item.isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

// Hàm tạo danh sách các cài đặt (Setting List Item)
private fun buildSettingItems(
    state: SettingUiState
): List<SettingItem> {
    return listOfNotNull(
        SettingItem.SettingNavigation("Tài khoản", Icons.AutoMirrored.Filled.KeyboardArrowRight, "account"),
        SettingItem.SettingNavigation("Mã PIN", Icons.AutoMirrored.Filled.KeyboardArrowRight, "pin"),
        SettingItem.SettingNavigation("Giao diện", Icons.AutoMirrored.Filled.KeyboardArrowRight, "theme"),
        SettingItem.SettingNavigation("Đơn vị tiền tệ", Icons.AutoMirrored.Filled.KeyboardArrowRight, "currency"),
        SettingItem.SettingToggle(title = "Dấu phân cách hàng nghìn", isChecked = state.isThousandSeparatorEnabled,),

        if (state.isThousandSeparatorEnabled)
            SettingItem.SettingNavigation("Dạng hiển thị số", Icons.AutoMirrored.Filled.KeyboardArrowRight, "number_format")
        else null,

        SettingItem.SettingNavigation("Dữ liệu và sao lưu", Icons.AutoMirrored.Filled.KeyboardArrowRight, "backup"),
        SettingItem.SettingNavigation("Đăng xuất", Icons.AutoMirrored.Filled.KeyboardArrowRight, "logout")
    )
}


/** Sau này sẽ xóa đi */
// ── Previews ──
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        val arrow = Icons.AutoMirrored.Filled.KeyboardArrowRight
        val previewItems = listOf(
            SettingItem.SettingNavigation("Tài khoản", arrow, "account"),
            SettingItem.SettingNavigation("Mã PIN", arrow, "pin"),
            SettingItem.SettingNavigation("Giao diện", arrow, "theme"),
            SettingItem.SettingNavigation("Đơn vị tiền tệ", arrow, "currency"),
            SettingItem.SettingToggle("Dấu phân cách hàng nghìn", isChecked = true),
            SettingItem.SettingNavigation("Dạng hiển thị số", arrow, "number_format"),
            SettingItem.SettingNavigation("Dữ liệu và sao lưu", arrow, "backup"),
            SettingItem.SettingNavigation("Đăng xuất", arrow, "logout")
        )
        SettingContent(
            items = previewItems,
            onToggleThousandSeparator = {},
            onItemClick = { _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        val arrow = Icons.AutoMirrored.Filled.KeyboardArrowRight
        val previewItems = listOf(
            SettingItem.SettingNavigation("Tài khoản", arrow, "account"),
            SettingItem.SettingNavigation("Mã PIN", arrow, "pin"),
            SettingItem.SettingNavigation("Giao diện", arrow, "theme"),
            SettingItem.SettingNavigation("Đơn vị tiền tệ", arrow, "currency"),
            SettingItem.SettingToggle("Dấu phân cách hàng nghìn", isChecked = false),
            SettingItem.SettingNavigation("Dữ liệu và sao lưu", arrow, "backup"),
            SettingItem.SettingNavigation("Đăng xuất", arrow, "logout")
        )
        SettingContent(
            items = previewItems,
            onToggleThousandSeparator = {},
            onItemClick = { _ -> }
        )
    }
}
