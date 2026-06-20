package com.example.mymoney.ui.other

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.other.OtherViewModel
import com.example.mymoney.presentation.viewmodel.other.other.OtherEvent
import com.example.mymoney.presentation.viewmodel.other.other.OtherUiState
import com.example.mymoney.ui.theme.MyMoneyTheme

/**
 * Màn hình Khác – tab cuối cùng trong Bottom Navigation.
 * UI stateless: chỉ nhận state từ ViewModel, không chứa logic nghiệp vụ.
 */
@Composable
fun OtherScreen(
    modifier: Modifier = Modifier,
    onNavigateToAboutUs: () -> Unit = {},
    onNavigateToReportBug: () -> Unit = {},
    onNavigateToSupportUs: () -> Unit = {},
    viewModel: OtherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    OtherContent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is OtherEvent.AboutUsClicked -> onNavigateToAboutUs()
                is OtherEvent.ReportBugClicked -> onNavigateToReportBug()
                is OtherEvent.SupportUsClicked -> onNavigateToSupportUs()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier
    )
}

/**
 * Nội dung hiển thị của màn hình Khác.
 * Composable thuần tuý – không phụ thuộc ViewModel, dễ test và preview.
 */
@Composable
private fun OtherContent(
    uiState: OtherUiState,
    onEvent: (OtherEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        OtherItem(
            title = "Về chúng tôi",
            onClick = { onEvent(OtherEvent.AboutUsClicked) }
        )
        OtherItem(
            title = "Ủng hộ chúng tôi",
            onClick = { onEvent(OtherEvent.SupportUsClicked) }
        )
        OtherItem(
            title = "Gửi bug cho chúng tôi",
            onClick = { onEvent(OtherEvent.ReportBugClicked) }
        )
    }
}

@Composable
private fun OtherItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Previews ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OtherScreenLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        OtherContent(
            uiState = OtherUiState(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OtherScreenDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        OtherContent(
            uiState = OtherUiState(),
            onEvent = {}
        )
    }
}
