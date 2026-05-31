package com.example.mymoney.ui.saving

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.saving.SavingViewModel
import com.example.mymoney.presentation.viewmodel.saving.SavingViewModelFactory
import com.example.mymoney.presentation.viewmodel.saving.saving.SavingEvent
import com.example.mymoney.presentation.viewmodel.saving.saving.SavingType
import com.example.mymoney.presentation.viewmodel.saving.saving.SavingUiState
import com.example.mymoney.ui.components.EmptyStateComposable
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Tiết kiệm – tab thứ 3 trong Bottom Navigation.
 */
@Composable
fun SavingScreen(
    userId: String = "",
) {
    val context = LocalContext.current
    val viewModel: SavingViewModel = viewModel(
        factory = SavingViewModelFactory(context, userId)
    )

    val uiState by viewModel.uiState.collectAsState()

    SavingContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun SavingContent(
    uiState: SavingUiState,
    onEvent: (SavingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── TOP AREA (toggle + buttons) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hiện đã hoàn thành",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.width(8.dp))

                Switch(
                    checked = uiState.isShowCompletedEnabled,
                    onCheckedChange = {
                        onEvent(SavingEvent.ToggleShowCompleted(it))
                    },
                    modifier = Modifier.scale(0.75f), // 👈 quan trọng nhất

                )
            }

            Spacer(Modifier.height(12.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(
                    onClick = { onEvent(SavingEvent.SelectedType(SavingType.ONE_TIME)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (uiState.selectedType == SavingType.ONE_TIME)
                            MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                ) {
                    Text(
                        "Một lần",
                        color =
                            if (uiState.selectedType == SavingType.ONE_TIME)
                                MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = { onEvent(SavingEvent.SelectedType(SavingType.RECURRING)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (uiState.selectedType == SavingType.RECURRING)
                            MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                ) {
                    Text(
                        "Định kỳ",
                        color =
                            if (uiState.selectedType == SavingType.RECURRING)
                                MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        EmptyStateComposable(
            "Hiện tại chưa có khoản tiết kiệm nào.\nBắt đầu thêm mục tiêu tiết kiệm của bạn ngay bây giờ"
        )

        // FAB
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            ExtendedFloatingActionButton(
                onClick = { /* TODO */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    text = "Thêm",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}


// ── Previews ──
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SavingScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        SavingContent(
            uiState = SavingUiState(),
            onEvent = {}
        )
    }
}
