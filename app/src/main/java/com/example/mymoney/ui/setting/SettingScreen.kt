package com.example.mymoney.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.setting.setting.ThemeMode
import com.example.mymoney.presentation.viewmodel.setting.SettingViewModel
import com.example.mymoney.presentation.viewmodel.setting.setting.CurrencyMode
import com.example.mymoney.presentation.viewmodel.setting.setting.NumberFormat
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingEvent
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingItem
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingNavEvent
import com.example.mymoney.presentation.viewmodel.setting.setting.SettingUiState
import com.example.mymoney.ui.setting.common.bottomsheet.SelectionBottomSheet
import com.example.mymoney.ui.setting.common.bottomsheet.SelectionOption
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
    val items = remember(uiState) { buildSettingItems(uiState) }

    // Collect navigation side-effect — chạy 1 lần, lắng nghe liên tục
    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                is SettingNavEvent.NavigateToSignIn -> onSignOut()
            }
        }
    }

    SettingContent(
        uiState  = uiState,
        items    = items,
        onEvent  = viewModel::onEvent,
        onItemClick = { route ->
            when (route) {
                "logout" -> viewModel.onEvent(SettingEvent.SignOut)
                "backup" -> viewModel.onEvent(SettingEvent.BackupToSupabaseClicked)
                "theme"  -> viewModel.onEvent(SettingEvent.ThemeClicked)
                "currency" -> viewModel.onEvent(SettingEvent.CurrencyClicked)
                "number_format" -> viewModel.onEvent(SettingEvent.NumberFormatClicked)
                else     -> onItemClick()
            }
        }
    )
}

/** * Nội dung hiển thị của màn hình Cài đặt.
 * * Composable thuần tuý – không phụ thuộc ViewModel, dễ test và preview.
 * */

@Composable
fun SettingContent(
    uiState: SettingUiState,
    items: List<SettingItem>,
    onEvent: (SettingEvent) -> Unit,
    onItemClick: (route: String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Khi có resultMessage → hiện Snackbar
    LaunchedEffect(uiState.backupResultMessage) {
        val msg = uiState.backupResultMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            onEvent(SettingEvent.DismissBackupResult)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // ── Nội dung chính ──
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Header username
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text  = if (uiState.username.isNotBlank()) uiState.username else "...",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(items) { item ->
                            when (item) {
                                is SettingItem.SettingNavigation -> SettingNavigationItem(
                                    item       = item,
                                    onItemClick = { onItemClick(item.route) }
                                )
                                is SettingItem.SettingToggle -> SettingToggleItem(
                                    item            = item,
                                    onCheckedChange = { onEvent(SettingEvent.ToggleThousandSeparator(it)) }
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    // ── Loading Overlay: dùng Dialog để phủ toàn màn hình thực sự ──
    if (uiState.isBackingUp) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress   = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(32.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color    = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text  = "Đang sao lưu dữ liệu...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // ── Dialog xác nhận backup ──
    if (uiState.showBackupConfirmDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(SettingEvent.BackupDismissed) },
            title = {
                Text(
                    text       = "Sao lưu dữ liệu",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text  = "Tất cả giao dịch trong thiết bị sẽ được đồng bộ lên đám mây (Supabase).\nBạn có muốn tiếp tục?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { onEvent(SettingEvent.BackupConfirmed) }) {
                    Text("Sao lưu ngay")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onEvent(SettingEvent.BackupDismissed) }) {
                    Text("Huỷ")
                }
            }
        )
    }

    if(uiState.showThemeSheet) {
        SelectionBottomSheet(
            title = "Giao diện",
            options = listOf(
                SelectionOption("Sáng", ThemeMode.LIGHT),
                SelectionOption("Tối", ThemeMode.DARK),
                SelectionOption("Theo hệ thống", ThemeMode.SYSTEM)
            ),
            selected = uiState.selectedTheme,
            onSelected = { onEvent(SettingEvent.ThemeSelected(it)) },
            onDismiss = { onEvent(SettingEvent.ThemeDismissed) }
        )
    }

    if (uiState.showCurrencySheet) {
        SelectionBottomSheet(
            title = "Đơn vị tiền tệ",
            options = listOf(
                SelectionOption("Việt Nam Đồng (VNĐ)", CurrencyMode.VND)
            ),
            selected = uiState.selectedCurrency,
            onSelected = {
                onEvent(SettingEvent.CurrencySelected(it))
            },
            onDismiss = {
                onEvent(SettingEvent.CurrencyDismissed)
            }
        )
    }

    if (uiState.showNumberFormat) {
        SelectionBottomSheet(
            title = "Định dạng số",
            options = listOf(
                SelectionOption("1,000,000", NumberFormat.COMMA)
            ),
            selected = uiState.selectedNumberFormat,
            onSelected = {
                onEvent(SettingEvent.NumberFormatSelected(it))
            },
            onDismiss = {
                onEvent(SettingEvent.NumberFormatDismissed)
            }
        )
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
            text  = item.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
        )
        Icon(imageVector = item.icon, contentDescription = null)
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
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = item.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Switch(checked = item.isChecked, onCheckedChange = onCheckedChange)
    }
}

// Hàm tạo danh sách các cài đặt (Setting List Item)
private fun buildSettingItems(
    state: SettingUiState
): List<SettingItem> {
    val arrow = Icons.AutoMirrored.Filled.KeyboardArrowRight
    return listOfNotNull(
        SettingItem.SettingNavigation("Tài khoản",                 arrow, "account"),
        SettingItem.SettingNavigation("Mã PIN",                    arrow, "pin"),
        SettingItem.SettingNavigation("Giao diện",                 arrow, "theme"),
        SettingItem.SettingNavigation("Đơn vị tiền tệ",            arrow, "currency"),
        SettingItem.SettingToggle("Dấu phân cách hàng nghìn", isChecked = state.isThousandSeparatorEnabled),
        if (state.isThousandSeparatorEnabled)
            SettingItem.SettingNavigation("Dạng hiển thị số",      arrow, "number_format")
        else null,
        SettingItem.SettingNavigation("Dữ liệu và sao lưu",        arrow, "backup"),
        SettingItem.SettingNavigation("Đăng xuất",                 arrow, "logout")
    )
}


// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        val arrow = Icons.AutoMirrored.Filled.KeyboardArrowRight
        SettingContent(
            uiState = SettingUiState(username = "Nguyễn Văn A", isThousandSeparatorEnabled = true),
            items   = listOf(
                SettingItem.SettingNavigation("Tài khoản",             arrow, "account"),
                SettingItem.SettingNavigation("Dữ liệu và sao lưu",    arrow, "backup"),
                SettingItem.SettingToggle("Dấu phân cách hàng nghìn", isChecked = true),
                SettingItem.SettingNavigation("Đăng xuất",             arrow, "logout")
            ),
            onEvent     = {},
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingBackupLoadingPreview() {
    MyMoneyTheme(darkTheme = true) {
        val arrow = Icons.AutoMirrored.Filled.KeyboardArrowRight
        SettingContent(
            uiState = SettingUiState(username = "Test User", isBackingUp = true),
            items   = listOf(
                SettingItem.SettingNavigation("Dữ liệu và sao lưu", arrow, "backup")
            ),
            onEvent     = {},
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingBackupDialogPreview() {
    MyMoneyTheme(darkTheme = false) {
        val arrow = Icons.AutoMirrored.Filled.KeyboardArrowRight
        SettingContent(
            uiState = SettingUiState(username = "Test User", showBackupConfirmDialog = true),
            items   = listOf(
                SettingItem.SettingNavigation("Dữ liệu và sao lưu", arrow, "backup")
            ),
            onEvent     = {},
            onItemClick = {}
        )
    }
}
