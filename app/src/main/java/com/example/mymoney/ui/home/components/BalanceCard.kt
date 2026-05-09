package com.example.mymoney.ui.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import com.example.mymoney.presentation.viewmodel.home.home.WalletItem
import com.example.mymoney.ui.theme.MyMoneyTheme
import java.util.Collections

/**
 * Khu vực đầu trang chủ gồm các thẻ ví cuộn ngang và thẻ "+" thêm ví.
 *
 * Gestures:
 *  - Nhấn đơn       : chọn ví (onSelectWallet)
 *  - Nhấn đúp       : mở màn hình chỉnh sửa ví (onEditWallet)
 *  - Ấn giữ + kéo   : sắp xếp lại ví; khi nhả tay → onReorderWallets với thứ tự mới
 *
 * Loading Indicator:
 *  - Khi người dùng chọn ví, thẻ đó sẽ hiển thị spinner để indicate dữ liệu đang được load
 *  - Spinner tự động biến mất khi dữ liệu load xong
 *
 * @param wallets           Danh sách ví từ database
 * @param selectedWalletId  ID của ví hiện được chọn
 * @param onSelectWallet    Callback khi nhấn ví (chọn ví)
 * @param onAddClick        Callback khi nhấn nút "+" (thêm ví mới)
 * @param onEditWallet      Callback khi nhấn đúp thẻ ví (chỉnh sửa)
 * @param onReorderWallets  Callback khi drag & drop kết thúc, nhận danh sách id theo thứ tự mới
 * @param modifier          Modifier tùy chỉnh
 */
@Composable
fun BalanceSection(
    wallets: List<WalletItem>,
    selectedWalletId: Long,
    onSelectWallet: (walletId: Long) -> Unit,
    onAddClick: () -> Unit,
    onEditWallet: (walletId: Long) -> Unit,
    onReorderWallets: (orderedIds: List<Long>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Danh sách nội bộ để hoán đổi vị trí khi kéo thả
    val localWallets = remember { mutableStateListOf<WalletItem>() }

    // Trạng thái scroll – dùng để reset về card đầu tiên khi danh sách ví thay đổi
    val listState = rememberLazyListState()

    // Trạng thái kéo thả
    var isDragging by remember { mutableStateOf(false) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    // Bước di chuyển mỗi ô: chiều rộng thẻ (200dp) + khoảng cách (12dp)
    val density = LocalDensity.current
    val cardStepPx = with(density) { 212.dp.toPx() }

    // Đồng bộ localWallets với DB, nhưng không làm gián đoạn kéo thả.
    // Sau khi cập nhật, luôn cuộn về card đầu tiên để ưu tiên hiển thị từ đầu.
    LaunchedEffect(wallets) {
        if (!isDragging) {
            localWallets.clear()
            localWallets.addAll(wallets)
            listState.scrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(items = localWallets, key = { _, w -> w.id }) { index, wallet ->
            WalletCard(
                wallet          = wallet,
                isSelected      = wallet.id == selectedWalletId,
                isDragging      = index == draggingIndex,
                dragOffsetX     = if (index == draggingIndex) dragOffsetX else 0f,
                onClick         = { onSelectWallet(wallet.id) },
                onDoubleTap     = { onEditWallet(wallet.id) },
                onDragStart     = {
                    isDragging = true
                    draggingIndex = index
                    dragOffsetX = 0f
                },
                onDrag          = { delta ->
                    dragOffsetX += delta
                    val threshold = cardStepPx / 2
                    when {
                        dragOffsetX > threshold && draggingIndex < localWallets.size - 1 -> {
                            Collections.swap(localWallets, draggingIndex, draggingIndex + 1)
                            draggingIndex++
                            dragOffsetX -= cardStepPx
                        }
                        dragOffsetX < -threshold && draggingIndex > 0 -> {
                            Collections.swap(localWallets, draggingIndex, draggingIndex - 1)
                            draggingIndex--
                            dragOffsetX += cardStepPx
                        }
                    }
                },
                onDragEnd       = {
                    isDragging = false
                    draggingIndex = -1
                    dragOffsetX = 0f
                    onReorderWallets(localWallets.map { it.id })
                }
            )
        }
        item(key = "add_wallet") {
            AddWalletCard(onClick = onAddClick)
        }
    }
}

// ── Thẻ ví đơn ──

@Composable
private fun WalletCard(
    wallet: WalletItem,
    isSelected: Boolean,
    isDragging: Boolean,
    dragOffsetX: Float,
    onClick: () -> Unit,
    onDoubleTap: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (deltaX: Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = runCatching {
        Color(wallet.color.toColorInt())
    }.getOrElse { MaterialTheme.colorScheme.primary }

    Card(
        modifier = modifier
            .width(200.dp)
            .height(100.dp)
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationX = dragOffsetX
                scaleX = if (isDragging) 1.06f else 1f
                scaleY = if (isDragging) 1.06f else 1f
            }
            .then(
                if (isSelected)
                    Modifier.border(3.dp, Color.White, RoundedCornerShape(16.dp))
                else
                    Modifier
            )
            // Nhấn đơn → chọn ví; Nhấn đúp → chỉnh sửa ví
            .pointerInput(onClick, onDoubleTap) {
                detectTapGestures(
                    onTap       = { onClick() },
                    onDoubleTap = { onDoubleTap() }
                )
            }
            // Ấn giữ → bắt đầu kéo thả sắp xếp
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart  = { onDragStart() },
                    onDrag       = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x)
                    },
                    onDragEnd    = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 12.dp else if (isSelected) 8.dp else 4.dp,
            pressedElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = wallet.name, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(2.dp))
            Text(text = "Số dư:", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(text = wallet.formattedBalance, style = MaterialTheme.typography.titleLarge, color = Color.White)
        }
    }
}

// ── Nút thêm ví "+": thẻ cùng kích thước ──

@Composable
private fun AddWalletCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier
            .size(100.dp),
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector        = Icons.Default.Add,
                contentDescription = "Thêm ví",
                modifier           = Modifier.size(36.dp),
                tint               = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// ── Previews ──

@Preview(showBackground = true)
@Composable
private fun BalanceSectionOneWalletPreview() {
    MyMoneyTheme(darkTheme = false) {
        BalanceSection(
            wallets           = listOf(WalletItem(1L, "Ví chính", "1.000.000 vnđ", "#0088F0")),
            selectedWalletId  = 1L,
            onSelectWallet    = {},
            onAddClick        = {},
            onEditWallet      = {},
            onReorderWallets  = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceSectionMultiWalletPreview() {
    MyMoneyTheme(darkTheme = false) {
        BalanceSection(
            wallets = listOf(
                WalletItem(1L, "Ví chính",  "1.000.000 vnđ", "#0088F0"),
                WalletItem(2L, "Tiết kiệm", "5.000.000 vnđ", "#FF8C00"),
                WalletItem(3L, "Dự phòng",  "2.500.000 vnđ", "#6A5ACD"),
            ),
            selectedWalletId  = 2L,
            onSelectWallet    = {},
            onAddClick        = {},
            onEditWallet      = {},
            onReorderWallets  = {}
        )
    }
}
