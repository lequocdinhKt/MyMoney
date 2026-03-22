package com.example.mymoney.ui.screens.startScreens.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Composable tái sử dụng cho các trang onboarding.
 *
 * @param title Tiêu đề trang (AnnotatedString để hỗ trợ styled text)
 * @param description Mô tả ngắn bên dưới tiêu đề
 * @param imageRes Resource ID của hình minh hoạ
 * @param imageDescription Mô tả hình ảnh cho accessibility
 * @param currentPage Chỉ số trang hiện tại (0-based)
 * @param totalPages Tổng số trang onboarding
 * @param onNextClick Callback khi nhấn nút tiếp theo
 * @param modifier Modifier tuỳ chỉnh từ bên ngoài
 */
@Composable
fun OnboardingPageLayout(
    title: AnnotatedString,
    description: String,
    @DrawableRes imageRes: Int,
    imageDescription: String,
    currentPage: Int,
    totalPages: Int,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // ── Phần 1: Tiêu đề ──
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Phần 1b: Mô tả ──
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // ── Phần 2: Spacer đẩy ảnh xuống phía dưới ──
        // Chỉ có 1 Spacer phía trên ảnh để ảnh nằm sát bottomBar
        Spacer(modifier = Modifier.weight(1f))

        // ── Hình ảnh minh hoạ ──
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = imageDescription,
            modifier = Modifier.border(2.dp, color = Color.Cyan).size(320.dp)
        )

        // ── Phần 3: Chỉ báo trang + nút tiếp theo (dính ngay dưới ảnh) ──
        OnboardingBottomBar(
            currentPage = currentPage,
            totalPages = totalPages,
            onNextClick = onNextClick
        )

        Spacer(modifier = Modifier.height(64.dp))
    }
}

/**
 * Thanh điều hướng dưới cùng: page indicator dots + nút FAB mũi tên.
 */
@Composable
private fun OnboardingBottomBar(
    currentPage: Int,
    totalPages: Int,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, color = Color.Cyan)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PageIndicator(
            currentPage = currentPage,
            totalPages = totalPages
        )

        FloatingActionButton(
            onClick = onNextClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Tiếp theo"
            )
        }
    }
}

/**
 * Hiển thị các chấm tròn chỉ báo trang hiện tại.
 */
@Composable
private fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            // Chấm active = primary, inactive = surfaceVariant
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}
