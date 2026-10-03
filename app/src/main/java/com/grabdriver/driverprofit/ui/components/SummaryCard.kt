package com.grabdriver.driverprofit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grabdriver.driverprofit.domain.model.DailySummary
import com.grabdriver.driverprofit.ui.theme.CostOrange
import com.grabdriver.driverprofit.ui.theme.GrabGreen
import com.grabdriver.driverprofit.ui.theme.GrabGreenLight
import java.util.Locale

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.grabdriver.driverprofit.util.DateUtils

@Composable
fun SummaryCard(
    summary: DailySummary,
    modifier: Modifier = Modifier
) {
    val isToday = DateUtils.isToday(summary.dateString)
    val titleText = if (isToday) "กำไรสุทธิวันนี้ (Net Profit)" else "กำไรสุทธิ (${DateUtils.formatThaiDateShort(summary.dateString)})"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = titleText,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(200))
                },
                label = "SummaryTitleAnim"
            ) { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ตัวเลขกำไรสีเขียวขนาดใหญ่ ชัดเจน มองง่าย พร้อม animation การเปลี่ยนตัวเลข
            AnimatedContent(
                targetState = summary.totalNetProfit,
                transitionSpec = {
                    (slideInVertically(tween(250)) { -it / 3 } + fadeIn(tween(250))) togetherWith
                    (slideOutVertically(tween(200)) { it / 3 } + fadeOut(tween(180)))
                },
                label = "ProfitNumberAnim"
            ) { profit ->
                Text(
                    text = String.format(Locale.US, "฿ %.2f", profit),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (profit >= 0.0) GrabGreen else MaterialTheme.colorScheme.error,
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // แถวที่ 1: รายได้รวม | ค่าน้ำมันรวม
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "รายได้รวม (ค่ารอบ+ทิป)",
                    value = String.format(Locale.US, "฿%.2f", summary.totalIncome),
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                MetricItem(
                    label = "ค่าน้ำมันรวม",
                    value = String.format(Locale.US, "฿%.2f", summary.totalFuelCost),
                    valueColor = CostOrange,
                    isEnd = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // แถวที่ 2: จำนวนรอบ | ระยะทาง | สิ้นเปลืองเฉลี่ยจริง
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "จำนวนรอบ",
                    value = "${summary.totalTripsCount} รอบ",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                MetricItem(
                    label = "ระยะทางรวม",
                    value = String.format(Locale.US, "%.1f km", summary.totalDistanceKm),
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    isCenter = true
                )
                MetricItem(
                    label = "กินจริงเฉลี่ย",
                    value = if (summary.averageEffectiveKmL > 0.0) {
                        String.format(Locale.US, "%.1f km/L", summary.averageEffectiveKmL)
                    } else "-",
                    valueColor = GrabGreen,
                    isEnd = true
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    valueColor: Color,
    isEnd: Boolean = false,
    isCenter: Boolean = false
) {
    Column(
        horizontalAlignment = when {
            isEnd -> Alignment.End
            isCenter -> Alignment.CenterHorizontally
            else -> Alignment.Start
        }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(150))
            },
            label = "MetricValueAnim"
        ) { targetVal ->
            Text(
                text = targetVal,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}
