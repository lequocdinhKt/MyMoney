package com.example.mymoney.ui.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import com.example.mymoney.presentation.viewmodel.statistics.CategoryStatsItem
import java.util.Locale

@Composable
fun DonutChart(
    data: List<CategoryStatsItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 50f
            val spacing = 2f // Gap giữa các đoạn
            
            var startAngle = -90f
            
            data.forEach { item ->
                val sweepAngle = (item.percentage / 100f) * 360f
                
                if (sweepAngle > 0) {
                    drawArc(
                        color = Color(android.graphics.Color.parseColor(item.color)),
                        startAngle = startAngle + (spacing / 2),
                        sweepAngle = sweepAngle - spacing,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                startAngle += sweepAngle
            }
        }
        
        // Nội dung ở tâm biểu đồ
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tổng cộng",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            val total = data.sumOf { it.amount }
            Text(
                text = if (total > 0) String.format(Locale("vi", "VN"), "%,.0fđ", total).replace(",", ".") else "0đ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
