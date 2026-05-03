package com.example.mymoney.ui.components.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mymoney.ui.theme.MyMoneyTheme

// ── Shimmer colour palettes ──

private val ShimmerLightColors = listOf(
    Color(0xFFE0E0E0),
    Color(0xFFF5F5F5),
    Color(0xFFEBEBEB),
    Color(0xFFF5F5F5),
    Color(0xFFE0E0E0),
)

private val ShimmerDarkColors = listOf(
    Color(0xFF2C2C2C),
    Color(0xFF3D3D3D),
    Color(0xFF454545),
    Color(0xFF3D3D3D),
    Color(0xFF2C2C2C),
)

// ── Core Modifier extension ──

/**
 * Áp dụng hiệu ứng shimmer (ánh sáng quét ngang) lên bất kỳ composable nào.
 * Dùng thuần Compose Animation, không cần thư viện ngoài.
 *
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .fillMaxWidth()
 *         .height(20.dp)
 *         .clip(RoundedCornerShape(4.dp))
 *         .shimmerEffect()
 * )
 * ```
 *
 * @param darkTheme Dùng palette tối khi ở dark mode
 */
fun Modifier.shimmerEffect(darkTheme: Boolean = false): Modifier = composed {
    val colors = if (darkTheme) ShimmerDarkColors else ShimmerLightColors

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1200f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    background(
        brush = Brush.linearGradient(
            colors = colors,
            start  = Offset(translateAnim - 500f, translateAnim - 500f),
            end    = Offset(translateAnim,         translateAnim)
        )
    )
}

// ── Pre-built bone composables ──

/**
 * Hộp xương bo góc – building block cơ bản cho mọi skeleton.
 *
 * @param modifier  Size được truyền từ bên ngoài
 * @param shape     Shape bo góc (mặc định 8dp)
 * @param darkTheme Sử dụng palette tối
 */
@Composable
fun ShimmerBox(
    modifier:  Modifier = Modifier,
    shape:     Shape    = RoundedCornerShape(8.dp),
    darkTheme: Boolean  = false
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmerEffect(darkTheme)
    )
}

/**
 * Xương dòng văn bản.
 *
 * @param modifier  Bắt buộc cung cấp width (hoặc fillMaxWidth)
 * @param height    Chiều cao dòng
 * @param fraction  Tỉ lệ fillMaxWidth (0f–1f); bị bỏ qua nếu modifier có width cố định
 * @param darkTheme Sử dụng palette tối
 */
@Composable
fun ShimmerLine(
    modifier:  Modifier = Modifier,
    height:    Dp       = 14.dp,
    fraction:  Float    = 1f,
    darkTheme: Boolean  = false
) {
    ShimmerBox(
        modifier  = modifier
            .fillMaxWidth(fraction)
            .height(height),
        shape     = RoundedCornerShape(4.dp),
        darkTheme = darkTheme
    )
}

/**
 * Xương hình tròn – dùng cho avatar, icon tròn.
 *
 * @param size      Đường kính vòng tròn
 * @param darkTheme Sử dụng palette tối
 */
@Composable
fun ShimmerCircle(
    size:      Dp      = 40.dp,
    darkTheme: Boolean = false
) {
    ShimmerBox(
        modifier  = Modifier.size(size),
        shape     = CircleShape,
        darkTheme = darkTheme
    )
}

/**
 * Xương thẻ Card bo góc 16dp – dùng cho các card ví, budget, saving.
 *
 * @param modifier  Size được truyền từ bên ngoài
 * @param darkTheme Sử dụng palette tối
 */
@Composable
fun ShimmerCard(
    modifier:  Modifier = Modifier,
    darkTheme: Boolean  = false
) {
    ShimmerBox(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        darkTheme = darkTheme
    )
}

// ── Previews ──

@Preview(showBackground = true, name = "Shimmer Primitives - Light")
@Composable
private fun ShimmerLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerCard(modifier = Modifier.fillMaxWidth().height(100.dp))
            ShimmerLine(height = 18.dp, fraction = 0.6f)
            ShimmerLine(height = 14.dp, fraction = 0.9f)
            ShimmerLine(height = 14.dp, fraction = 0.75f)
            ShimmerCircle(size = 48.dp)
        }
    }
}

@Preview(showBackground = true, name = "Shimmer Primitives - Dark")
@Composable
private fun ShimmerDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerCard(modifier = Modifier.fillMaxWidth().height(100.dp), darkTheme = true)
            ShimmerLine(height = 18.dp, fraction = 0.6f, darkTheme = true)
            ShimmerLine(height = 14.dp, fraction = 0.9f, darkTheme = true)
            ShimmerCircle(size = 48.dp, darkTheme = true)
        }
    }
}
