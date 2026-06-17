package com.example.mymoney.ui.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymoney.presentation.viewmodel.statistics.TrendStatsItem

@Composable
fun BarChart(
    data: List<TrendStatsItem>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxAmount = data.maxOf { it.amount }.coerceAtLeast(1.0)
    
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = 30.dp.toPx()
                val spacing = (size.width - (barWidth * data.size)) / (data.size + 1)
                
                data.forEachIndexed { index, item ->
                    val barHeight = (item.amount / maxAmount).toFloat() * size.height
                    val x = spacing + index * (barWidth + spacing)
                    val y = size.height - barHeight
                    
                    drawRoundRect(
                        color = if (item.isSelected) Color(0xFFE57373) else Color(0xFFFFCDD2),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach { item ->
                Text(
                    item.label,
                    fontSize = 12.sp,
                    color = if (item.isSelected) Color(0xFFE57373) else Color.Gray,
                    fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
