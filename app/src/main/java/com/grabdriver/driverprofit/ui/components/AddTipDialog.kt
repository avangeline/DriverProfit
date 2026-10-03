package com.grabdriver.driverprofit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import com.grabdriver.driverprofit.ui.theme.GrabGreen
import java.util.Locale

@Composable
fun AddTipDialog(
    trip: TripRecordEntity,
    tripNumber: Int,
    onDismiss: () -> Unit,
    onSaveTip: (newTip: Double) -> Unit
) {
    var tipInputText by remember {
        mutableStateOf(if (trip.tip > 0) String.format(Locale.US, "%.0f", trip.tip) else "")
    }

    val currentTip = tipInputText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = GrabGreen
                )
                Text(
                    text = "เพิ่มทิปย้อนหลัง (รอบ #$tripNumber)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "กรณีลูกค้ากดให้ทิปผ่านแอปตามหลัง หรือให้เงินสดเพิ่มหลังจากจบรอบแล้ว:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ข้อมูลรอบ
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ค่ารอบ: ฿${String.format(Locale.US, "%.0f", trip.fare)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (trip.tip > 0) "ทิปเดิม: ฿${String.format(Locale.US, "%.0f", trip.tip)}" else "ยังไม่มีทิป",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (trip.tip > 0) GrabGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ปุ่มลัดทิปยอดนิยมใน Grab (กดสะสมยอดได้)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "เลือกยอดทิปที่ลูกค้ากดให้ (กดสะสมได้):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 5, 10, 20, 50).forEach { amount ->
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50.dp))
                                    .clickable {
                                        val current = tipInputText.toDoubleOrNull() ?: 0.0
                                        tipInputText = (current + amount).toInt().toString()
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "+$amount",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // หรือกรอกตัวเลขทิปเอง (แตะแล้วไฮไลต์ทั้งหมด)
                AutoSelectTextField(
                    value = tipInputText,
                    onValueChange = { tipInputText = it },
                    label = { Text("ระบุยอดทิป (บาท)") },
                    placeholder = { Text("เช่น 25") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    trailingIcon = {
                        if (tipInputText.isNotEmpty()) {
                            IconButton(onClick = { tipInputText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "ล้าง", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (currentTip > 0.0) {
                    Text(
                        text = "กำไรสุทธิรอบนี้จะเพิ่มเป็น ฿${String.format(Locale.US, "%.2f", (trip.fare + currentTip) - trip.fuelCost)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrabGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveTip(currentTip)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen)
            ) {
                Text("บันทึกทิป")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
