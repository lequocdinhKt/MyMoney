package com.example.mymoney.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoney.R
import com.example.mymoney.ui.navigation.BottomTab
import com.example.mymoney.ui.theme.MyMoneyTheme

// ── Kích thước cố định theo tỉ lệ subtract.xml (480 x 108) ──
private val BottomBarHeight = 80.dp    // Chiều cao vùng hiển thị thanh bar
private val FabSize = 56.dp            // Kích thước nút FAB hình tròn
private val FabOffset = (-28).dp       // Đẩy FAB lên trên để lọt vào chỗ lõm

/**
 * Custom Bottom Navigation Bar sử dụng subtract.xml làm hình nền.
 *
 * Cấu trúc: Box chồng lớp
 *   1. Image (subtract.xml) — hình nền có chỗ lõm giữa
 *   2. Row — 4 nút điều hướng: [Home] [Budget] <khoảng trống> [Saving] [Other]
 *   3. FAB — nút Add (+) nổi lên khớp vào chỗ lõm
 *
 * @param currentRoute Route hiện tại đang được chọn
 * @param onTabSelected Callback khi người dùng nhấn vào một tab
 * @param onAddClick Callback khi người dùng nhấn nút Add (+)
 * @param modifier Modifier tùy chỉnh
 */
@Composable
fun CustomBottomBar(
    currentRoute: String?,
    onTabSelected: (BottomTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Màu icon khi được chọn và không được chọn
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ── 1. Hình nền subtract.xml (có chỗ lõm giữa) ──
        Image(
            painter = painterResource(id = R.drawable.subtract),
            contentDescription = null, // Hình trang trí, không cần mô tả
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomBarHeight),
            contentScale = ContentScale.FillBounds,
            // Tô màu nền thanh bar theo theme (trắng cho light mode)
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface)
        )

        // ── 2. Row chứa 4 nút điều hướng ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomBarHeight)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cụm trái: Home + Budget
            BottomTab.all.take(2).forEach { tab ->
                BottomNavItem(
                    tab = tab,
                    isSelected = currentRoute == tab.route,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Khoảng trống giữa — nhường chỗ cho FAB
            Spacer(modifier = Modifier.width(64.dp))

            // Cụm phải: Saving + Other
            BottomTab.all.drop(2).forEach { tab ->
                BottomNavItem(
                    tab = tab,
                    isSelected = currentRoute == tab.route,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── 3. FAB nút Add (+) — nằm giữa, nổi lên trên chỗ lõm ──
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = FabOffset)
                .size(FabSize),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Thêm giao dịch",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Một item đơn lẻ trong Bottom Navigation.
 * Hiển thị icon + label, đổi màu khi được chọn.
 *
 * @param tab Tab tương ứng (Home / Budget / Saving / Other)
 * @param isSelected Tab này có đang được chọn hay không
 * @param selectedColor Màu khi tab được chọn
 * @param unselectedColor Màu khi tab không được chọn
 * @param onClick Callback khi nhấn vào tab
 * @param modifier Modifier tùy chỉnh
 */
@Composable
private fun BottomNavItem(
    tab: BottomTab,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tintColor = if (isSelected) selectedColor else unselectedColor

    Column(
        modifier = modifier
            .clickable(
                // Tắt hiệu ứng ripple mặc định để giao diện sạch hơn
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = tintColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = tab.label,
            color = tintColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

// ── Previews ──

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun CustomBottomBarLightPreview() {
    MyMoneyTheme(darkTheme = false) {
        CustomBottomBar(
            currentRoute = BottomTab.Home.route,
            onTabSelected = {},
            onAddClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun CustomBottomBarDarkPreview() {
    MyMoneyTheme(darkTheme = true) {
        CustomBottomBar(
            currentRoute = BottomTab.Saving.route,
            onTabSelected = {},
            onAddClick = {}
        )
    }
}
