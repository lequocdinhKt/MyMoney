package com.example.mymoney.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.data.local.datastore.SettingPreferences
import com.example.mymoney.data.repository.AuthRepositoryImpl
import com.example.mymoney.presentation.viewmodel.profile.ProfileEvent
import com.example.mymoney.presentation.viewmodel.profile.ProfileNavEvent
import com.example.mymoney.presentation.viewmodel.profile.ProfileUiState
import com.example.mymoney.presentation.viewmodel.profile.ProfileViewModel
import com.example.mymoney.presentation.viewmodel.profile.ProfileViewModelFactory
import com.example.mymoney.ui.auth.components.AuthTextField
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Hồ sơ cá nhân - cho phép đổi tên, mật khẩu và xóa tài khoản.
 */
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            AuthRepositoryImpl(),
            SettingPreferences(LocalContext.current)
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileEvent.DismissSnackbar)
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileEvent.DismissSnackbar)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navEvent.collect { event ->
            when (event) {
                ProfileNavEvent.NavigateBack -> onNavigateBack()
                ProfileNavEvent.NavigateToSignIn -> onNavigateToSignIn()
            }
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ProfileContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onEvent = onEvent
        )
    }

    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(ProfileEvent.DeleteAccountDismissed) },
            title = { Text("Xác nhận xóa tài khoản") },
            text = { Text("Bạn có chắc chắn muốn xóa tài khoản này không? Mọi dữ liệu của bạn sẽ bị xóa vĩnh viễn và không thể khôi phục.") },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(ProfileEvent.DeleteAccountConfirmed) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa vĩnh viễn")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ProfileEvent.DeleteAccountDismissed) }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    uiState: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section: Thông tin cơ bản
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Thông tin người dùng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if(!uiState.isEditingUsername) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.username,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    IconButton(
                        onClick = {
                            onEvent(ProfileEvent.ToggleEditUsername)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Sửa tên"
                        )
                    }
                }
            } else {
                AuthTextField(
                    value = uiState.newUsername,
                    onValueChange = { onEvent(ProfileEvent.UsernameChanged(it)) },
                    placeholder = "Tên hiển thị",
                    leadingIcon = Icons.Default.PersonOutline
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            onEvent(ProfileEvent.ToggleEditUsername)
                        }
                    ) {
                        Text("Hủy")
                    }

                    Button(
                        onClick = { onEvent(ProfileEvent.UpdateUsername(uiState.newUsername)) },
                        enabled = !uiState.isLoading && uiState.newUsername != uiState.username
                    ) {
                        Text("Lưu tên")
                    }
                }
            }
        }

        HorizontalDivider()

        // Section: Đổi mật khẩu
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(ProfileEvent.ToggleExpanded) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Đổi mật khẩu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = if (uiState.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (uiState.isExpanded) "Thu gọn" else "Mở rộng"
                )
            }

            AnimatedVisibility(
                visible = uiState.isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Nhập mật khẩu cũ
                    AuthTextField(
                        value = uiState.oldPassword,
                        onValueChange = { onEvent(ProfileEvent.OldPasswordChanged(it)) },
                        placeholder = "Mật khẩu cũ",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = uiState.oldPasswordVisible,
                        onTogglePasswordVisibility = { onEvent(ProfileEvent.ToggleOldPasswordVisibility) }
                    )

                    // Nhập mật khẩu mới
                    AuthTextField(
                        value = uiState.newPassword,
                        onValueChange = { onEvent(ProfileEvent.NewPasswordChanged(it)) },
                        placeholder = "Mật khẩu mới",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = uiState.newPasswordVisible,
                        onTogglePasswordVisibility = { onEvent(ProfileEvent.ToggleNewPasswordVisibility) }
                    )

                    // Xác nhận lại mật khẩu
                    AuthTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { onEvent(ProfileEvent.ConfirmPasswordChanged(it)) },
                        placeholder = "Nhập lại mật khẩu mới",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        isPasswordVisible = uiState.confirmPasswordVisible,
                        onTogglePasswordVisibility = { onEvent(ProfileEvent.ToggleConfirmPasswordVisibility) }
                    )


                    Button(
                        onClick = {
                            onEvent(ProfileEvent.UpdatePassword(
                                uiState.oldPassword,
                                uiState.newPassword,
                                uiState.confirmPassword))
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !uiState.isLoading && uiState.newPassword.isNotEmpty() && uiState.confirmPassword.isNotEmpty()
                    ) {
                        Text("Cập nhật mật khẩu")
                    }
                }
            }
        }

        HorizontalDivider()

        // Section: Vùng nguy hiểm
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vùng nguy hiểm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Text(
                "Một khi bạn xóa tài khoản, bạn sẽ không thể lấy lại dữ liệu của mình.",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedButton(
                onClick = { onEvent(ProfileEvent.DeleteAccountClicked) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Xóa tài khoản")
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MyMoneyTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(
                username = "Nguyễn Văn A",
                isLoading = false
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Profile Screen - Loading")
@Composable
fun ProfileScreenLoadingPreview() {
    MyMoneyTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(
                username = "Nguyễn Văn A",
                isLoading = true
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Profile Screen - Delete Dialog")
@Composable
fun ProfileScreenDeleteDialogPreview() {
    MyMoneyTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(
                username = "Nguyễn Văn A",
                showDeleteConfirmDialog = true
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}
