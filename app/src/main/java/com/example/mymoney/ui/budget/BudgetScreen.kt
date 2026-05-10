package com.example.mymoney.ui.budget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.budget.BudgetViewModel
import com.example.mymoney.presentation.viewmodel.budget.BudgetViewModelFactory
import com.example.mymoney.presentation.viewmodel.budget.budget.BudgetEvent
import com.example.mymoney.presentation.viewmodel.budget.budget.BudgetUiState
import com.example.mymoney.ui.components.EmptyStateComposable
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Ngân sách – tab thứ 2 trong Bottom Navigation.
 */
@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    userId: String = "",
    onNavigateToBudgetManual: () -> Unit = {},
//    onNavigateToBudgetAI: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModelFactory(context, userId)
    )
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(BudgetEvent.AddBudgetDismissed)
        }
    }

    BudgetContent(
        uiState = uiState,
        modifier = modifier,
        onEvent = viewModel::onEvent,
        onNavigateToBudgetManual = onNavigateToBudgetManual,
//        onNavigateToBudgetAI = onNavigateToBudgetAI
    )
}

/**
 * Nội dung hiển thị của màn hình Ngân sách.
 */
@Composable
private fun BudgetContent(
    uiState: BudgetUiState,
    onEvent: (BudgetEvent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToBudgetManual: () -> Unit = {},
//    onNavigateToBudgetAI: () -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        EmptyStateComposable("Hiện tại chưa có ngân sách nào được tạo.\n" +
                "Bắt đầu thêm ngân sách của bạn ngay bây giờ")


        // ── Overlay ──
        AnimatedVisibility(
            visible = uiState.showAddBudget,
            enter = fadeIn(),
            exit =  fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable {
                        onEvent(BudgetEvent.AddBudgetDismissed)
                    }
            )
        }

        // FAB + option
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.End
        ) {

            // Ngân sách với AI
            AnimatedVisibility(
                visible = uiState.showAddBudget,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                SmallFabItem(
                    text = "Ngân sách với AI",
                    onClick = {
//                        onEvent(BudgetEvent.AddBudgetDismissed)
//                        onNavigateToBudgetAI()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Thêm thủ công
            AnimatedVisibility(
                visible = uiState.showAddBudget,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                SmallFabItem(
                    text = "Thêm thủ công",
                    onClick = {
                        onEvent(BudgetEvent.AddBudgetDismissed)
                        onNavigateToBudgetManual()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FAB chính
            ExtendedFloatingActionButton(
                onClick = {
                    if (uiState.showAddBudget) {
                        onEvent(BudgetEvent.AddBudgetDismissed)
                    } else {
                        onEvent(BudgetEvent.AddBudgetClicked)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (uiState.showAddBudget)
                            Icons.Default.Close
                        else
                            Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text( text = if (uiState.showAddBudget) "Đóng" else "Thêm",
                        style = MaterialTheme.typography.titleMedium )
                }
            )
        }
    }
}

@Composable
fun SmallFabItem(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = text)
        }
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BudgetScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        BudgetContent(
            uiState = BudgetUiState(),
            onEvent = {},
            onNavigateToBudgetManual = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BudgetScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        BudgetContent(uiState = BudgetUiState(),
        onEvent = {},
        onNavigateToBudgetManual = {}
        )
    }
}