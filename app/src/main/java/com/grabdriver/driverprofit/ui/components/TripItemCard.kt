package com.grabdriver.driverprofit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import com.grabdriver.driverprofit.ui.theme.CostOrange
import com.grabdriver.driverprofit.ui.theme.GrabGreen
import com.grabdriver.driverprofit.ui.theme.IdleAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripItemCard(
    trip: TripRecordEntity,
    tripNumber: Int,
    onAddOrEditTip: () -> Unit,
    onViewDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(trip.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onViewDetail() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // แถวบน: รอบที่, เวลา, กำไรสุทธิ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GrabGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "#$tripNumber",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GrabGreen
                        )
                    }
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = String.format(Locale.US, "+฿%.2f", trip.netProfit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = GrabGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // แถวกลาง: ค่ารอบ, ทิป (แตะเพื่อเพิ่ม/แก้), ระยะทาง, เวลาที่ใช้
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ค่ารอบ: ฿${String.format(Locale.US, "%.0f", trip.fare)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    // ปุ่มกดเพิ่มทิปย้อนหลัง (ถ้าลูกค้าเพิ่งกดให้ในแอป Grab)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (trip.tip > 0) GrabGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAddOrEditTip() }
                    ) {
                        Text(
                            text = if (trip.tip > 0) "ทิป ฿${String.format(Locale.US, "%.0f", trip.tip)} ✏️" else "+ ทิป",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (trip.tip > 0) GrabGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${String.format(Locale.US, "%.1f", trip.distanceKm)} km (${trip.durationMinutes} นาที)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // แถวล่าง: รายละเอียดค่าน้ำมัน & อัตราสิ้นเปลือง
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "หน้าปัด ${String.format(Locale.US, "%.1f", trip.dashKmL)} → กินจริง ${String.format(Locale.US, "%.1f", trip.effectiveKmL)} km/L",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "ค่าน้ำมัน ฿${String.format(Locale.US, "%.2f", trip.fuelCost)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CostOrange,
                    fontWeight = FontWeight.Medium
                )
            }

            if (trip.estimatedIdleMinutes > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "⏱️ จอดติด/รอ ~ ${trip.durationMinutes - ((trip.distanceKm / 35.0) * 60).toInt().coerceAtLeast(0)} นาที",
                    style = MaterialTheme.typography.labelSmall,
                    color = IdleAmber,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "แตะเพื่อดูรายละเอียด / แก้ไข ❯",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
