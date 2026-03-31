package com.example.mymoney.ui.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Mail
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Phần đăng nhập mạng xã hội tái sử dụng cho Sign In / Sign Up.
 *
 * Bao gồm:
 *   - Dải phân cách "OR" (hai đường kẻ + text ở giữa)
 *   - Hai nút Google và Facebook
 *
 * @param onGoogleClick   Callback khi nhấn nút Google
 * @param onFacebookClick Callback khi nhấn nút Facebook
 * @param modifier        Modifier tuỳ chỉnh
 */
@Composable
fun SocialLoginSection(
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Dải phân cách "OR" ──
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        )
        Text(
            text = "OR",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Nút Google + Facebook ──
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Nút Google
        OutlinedButton(
            onClick = onGoogleClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Mail,
                contentDescription = "Đăng nhập bằng Google",
                tint = Color(0xFFDB4437), // Google Red
                modifier = Modifier.size(24.dp),
            )
        }

        // Nút Facebook
        OutlinedButton(
            onClick = onFacebookClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Facebook,
                contentDescription = "Đăng nhập bằng Facebook",
                tint = Color(0xFF1877F2), // Facebook Blue
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
