package com.grabdriver.driverprofit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grabdriver.driverprofit.ui.theme.GrabGreen
import com.grabdriver.driverprofit.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    initialDateString: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialUtcMillis = remember(initialDateString) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.parse(initialDateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    val maxUtcMillis = remember {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val todayDate = sdf.parse(DateUtils.getTodayDateString())
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                if (todayDate != null) time = todayDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis() + 86400000L
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialUtcMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= maxUtcMillis
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = millis
                        }
                        val year = cal.get(Calendar.YEAR)
                        val month = cal.get(Calendar.MONTH) + 1
                        val day = cal.get(Calendar.DAY_OF_MONTH)
                        val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
                        onDateSelected(formattedDate)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("ตกลง", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
            title = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDateSelected(DateUtils.getTodayDateString())
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = GrabGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("วันนี้", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            onDateSelected(DateUtils.getYesterdayDateString())
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("เมื่อวาน", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            headline = {
                Text(
                    text = datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = millis
                        }
                        val year = cal.get(Calendar.YEAR)
                        val month = cal.get(Calendar.MONTH) + 1
                        val day = cal.get(Calendar.DAY_OF_MONTH)
                        val fDate = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
                        DateUtils.formatThaiDateWithDay(fDate)
                    } ?: "เลือกวันในปฏิทิน",
                    modifier = Modifier.padding(start = 20.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = GrabGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}
