// Thêm dòng này để dùng ModalBottomSheet,...(Material3 API experimental)
@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.mymoney.ui.setting.common.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> SelectionBottomSheet (
    title: String,
    options: List<SelectionOption<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit
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
    }
}