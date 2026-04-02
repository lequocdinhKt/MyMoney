package com.example.mymoney.ui.main.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymoney.presentation.viewmodel.setting.SettingViewModel
import com.example.mymoney.ui.setting.SettingScreen
import kotlin.math.roundToInt

private val DRAWER_WIDTH = 300.dp

/** Blur tối đa (px) khi drawer mở hoàn toàn — giống iOS ~16–20px */
private const val MAX_BLUR_RADIUS = 18f

/**
 * Drawer overlay với 3 Z-layer độc lập + Backdrop Blur kiểu iOS.
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │  Layer 3 — Drawer Panel   (trượt từ trái theo X)           │
 * ├─────────────────────────────────────────────────────────────┤
 * │  Layer 2 — Scrim          (tối dần theo X)                 │
 * ├─────────────────────────────────────────────────────────────┤
 * │  Layer 1 — Main Content   (blur dần theo X) ← iOS effect   │
 * └─────────────────────────────────────────────────────────────┘
 *
 * Biến X ∈ [0f, 1f] điều khiển đồng thời cả 3 layer:
 *
 *   Layer 1 blurRadius  = X × 18px   (mờ dần khi drawer mở)
 *   Layer 2 scrimAlpha  = X × 0.5    (tối dần khi drawer mở)
 *   Layer 3 offsetX     = -300dp × (1 - X) (trượt từ trái vào)
 *
 * Yêu cầu: minSdk ≥ 31 (Android 12). Project dùng minSdk=33 ✅
 */
@Composable
fun MainDrawerOverlay(
    isOpen: Boolean,
    onClose: () -> Unit,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingViewModel: SettingViewModel = viewModel(
        factory = SettingViewModel.factory(context)
    )

    // ── X: biến duy nhất điều khiển toàn bộ 3 layer ──
    val drawerProgress = remember { Animatable(0f) }

    LaunchedEffect(isOpen) {
        drawerProgress.animateTo(
            targetValue = if (isOpen) 1f else 0f,
            animationSpec = tween(durationMillis = 320)
        )
    }

    val x = drawerProgress.value

    // Không render gì khi drawer đóng hoàn toàn
    if (x == 0f && !isOpen) return

    // ─────────────────────────────────────────────────────────────────
    // LAYER 2 — Scrim
    //
    // Công thức: alpha = X × 0.5
    //   X=0.0 → alpha=0.00 (trong suốt)
    //   X=0.5 → alpha=0.25 (tối 25%)
    //   X=1.0 → alpha=0.50 (tối 50%)
    // ─────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = x * 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose
            )
    )

    // ─────────────────────────────────────────────────────────────────
    // LAYER 3 — Drawer Panel
    //
    // Công thức: offsetX = -DRAWER_WIDTH × (1 - X)
    //   X=0.0 → offsetX = -300dp (khuất hoàn toàn bên trái)
    //   X=0.5 → offsetX = -150dp (hiện một nửa)
    //   X=1.0 → offsetX =    0dp (hiển thị đầy đủ)
    // ─────────────────────────────────────────────────────────────────
    val drawerWidthPx = with(LocalDensity.current) { DRAWER_WIDTH.toPx() }
    val offsetX = (-drawerWidthPx * (1f - x)).roundToInt()

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(DRAWER_WIDTH)
            .offset { IntOffset(x = offsetX, y = 0) }
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            SettingScreen(
                onItemClick = onClose,
                onSignOut = onSignOut,
                viewModel = settingViewModel
            )
        }
    }
}

/**
 * Modifier áp dụng Backdrop Blur kiểu iOS lên Layer 1 (Main Content).
 *
 * Cách dùng trong MainScreen:
 * ```kotlin
 * Box(modifier = Modifier.drawerBlur(drawerProgress)) {
 *     // Main content ở đây
 * }
 * ```
 *
 * @param progress  drawerProgress ∈ [0f, 1f] — 0 = không blur, 1 = blur tối đa
 * @param maxBlur   Blur radius tối đa tính bằng pixel (mặc định 18px, giống iOS)
 *
 * Công thức:
 *   blurRadius = progress × maxBlur
 *   progress=0.0 → blur=0px  (sắc nét hoàn toàn)
 *   progress=0.5 → blur=9px  (mờ vừa)
 *   progress=1.0 → blur=18px (mờ tối đa, giống kính mờ iOS)
 */
fun Modifier.drawerBlur(
    progress: Float,
    maxBlur: Float = MAX_BLUR_RADIUS
): Modifier {
    if (progress <= 0f) return this
    val blurRadius = (progress * maxBlur).coerceAtLeast(0.01f)
    return this.graphicsLayer {
        renderEffect = android.graphics.RenderEffect
            .createBlurEffect(blurRadius, blurRadius, android.graphics.Shader.TileMode.CLAMP)
            .asComposeRenderEffect()
    }
}
