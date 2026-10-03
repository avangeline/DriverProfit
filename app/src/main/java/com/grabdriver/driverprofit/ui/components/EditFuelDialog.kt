package com.grabdriver.driverprofit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grabdriver.driverprofit.ui.theme.GrabGreen
import java.util.Locale

@Composable
fun EditFuelDialog(
    currentPrice: Double,
    onDismiss: () -> Unit,
    onSavePrice: (Double) -> Unit
) {
    var priceText by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentPrice)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⛽ ปรับราคาน้ำมันวันนี้",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "ระบุราคาน้ำมันต่อลิตรที่เติมในวันนี้ เพื่อใช้คำนวณต้นทุนรอบวิ่งทั้งหมด:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("ราคาน้ำมัน (บาท/ลิตร)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    if (price != null && price > 0.0) {
                        onSavePrice(price)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen)
            ) {
                Text("บันทึกราคา")
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
