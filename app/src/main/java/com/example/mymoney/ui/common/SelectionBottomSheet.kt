// Thêm dòng này để dùng ModalBottomSheet,...(Material3 API experimental)
@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mymoney.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mymoney.ui.theme.MyMoneyTheme
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.CarRepair

@Composable
fun <T> SelectionBottomSheet (
    title: String,
    options: List<SelectionOption<T>>,
    selected: T,
    layout:  SelectionLayout,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when(layout) {
                SelectionLayout.LIST -> {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable{
                                    onSelected(option.value)
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option.title)

                            RadioButton(
                                selected = option.value == selected,
                                onClick = { onSelected(option.value) }
                            )
                        }
                    }
                }

                SelectionLayout.GRID -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        modifier = Modifier.fillMaxWidth()
                            .weight(1f, fill = false),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(options) { option ->
                            val isSelected = option.value == selected

                            CategoryGridItem(
                                option = option,
                                selected = isSelected,
                                onClick = {
                                    onSelected(option.value)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> CategoryGridItem(
    option: SelectionOption<T>,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor =
        if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant

    val contentColor =
        if (selected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {

            option.image?.let {
                Icon(
                    imageVector = it,
                    contentDescription = option.title,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = option.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


//  ── Previews ──
val sampleOptions = listOf(
    SelectionOption("Option 1", 1),
    SelectionOption("Option 2", 2),
    SelectionOption("Option 3", 3)
)

val sampleCategories = listOf(
    SelectionOption(
        title = "Ăn uống",
        value = 1,
        image = Icons.Default.Fastfood
    ),
    SelectionOption(
        title = "Mua sắm",
        value = 2,
        image = Icons.Default.ShoppingCart
    ),
    SelectionOption(
        title = "Nhà cửa",
        value = 3,
        image = Icons.Default.Home
    ),
    SelectionOption(
        title = "Bảo trì xe",
        value = 4,
        image = Icons.Default.CarRepair
    )
)


@Preview(showBackground = true)
@Composable
private fun SelectionBottomSheetLightPreview() {
    MyMoneyTheme {
        SelectionBottomSheet(
            title = "Chọn",
            options = sampleOptions,
            selected = 1,
            layout = SelectionLayout.LIST,
            onSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryBottomSheetPreview() {
    MyMoneyTheme {
        SelectionBottomSheet(
            title = "Chọn danh mục",
            options = sampleCategories,
            selected = 1,
            onSelected = {},
            onDismiss = {},
            layout = SelectionLayout.GRID,
        )
    }
}