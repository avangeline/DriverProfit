package com.grabdriver.driverprofit.ui.tripdetail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import com.grabdriver.driverprofit.ui.components.AutoSelectTextField
import com.grabdriver.driverprofit.ui.components.DatePickerModal
import com.grabdriver.driverprofit.ui.theme.CostOrange
import com.grabdriver.driverprofit.ui.theme.GrabGreen
import com.grabdriver.driverprofit.ui.theme.IdleAmber
import com.grabdriver.driverprofit.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    trip: TripRecordEntity,
    tripNumber: Int,
    onBack: () -> Unit,
    onDeleteTrip: () -> Unit,
    onSaveEdit: (
        fare: Double,
        tip: Double,
        distanceKm: Double,
        dashKmL: Double,
        durationMinutes: Int,
        dateString: String
    ) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val timeFormat = remember { SimpleDateFormat("d MMM yyyy HH:mm", Locale("th", "TH")) }
    val timeStr = timeFormat.format(Date(trip.timestamp))

    var isEditing by remember { mutableStateOf(false) }

    // ค่าในโหมดแก้ไข (เริ่มจากข้อมูลที่บันทึกไว้)
    var editDateString by remember(trip) { mutableStateOf(trip.dateString) }
    var showDatePickerModal by remember { mutableStateOf(false) }
    var fareText by remember(trip) { mutableStateOf(String.format(Locale.US, "%.0f", trip.fare)) }
    var tipText by remember(trip) { mutableStateOf(if (trip.tip > 0) String.format(Locale.US, "%.0f", trip.tip) else "") }
    var distanceText by remember(trip) { mutableStateOf(String.format(Locale.US, "%.1f", trip.distanceKm)) }
    var dashKmLText by remember(trip) { mutableStateOf(String.format(Locale.US, "%.1f", trip.dashKmL)) }
    var durationText by remember(trip) { mutableStateOf(trip.durationMinutes.toString()) }

    // เมตริกที่คำนวณสดเมื่อแก้ไข
    val fare = fareText.toDoubleOrNull() ?: trip.fare
    val tip = tipText.toDoubleOrNull() ?: 0.0
    val distance = distanceText.toDoubleOrNull() ?: trip.distanceKm
    val dashKmL = dashKmLText.toDoubleOrNull() ?: trip.dashKmL

    // คำนวณเมตริกจากข้อมูลที่บันทึก (ใช้ค่าสดเมื่อแก้ไข)
    val displayFare = if (isEditing) fare else trip.fare
    val displayTip = if (isEditing) tip else trip.tip
    val displayDistance = if (isEditing) distance else trip.distanceKm
    val displayEffectiveKmL = if (isEditing) {
        val movingFuel = if (dashKmL > 0) distance / dashKmL else 0.0
        val totalFuel = movingFuel + trip.idleFuelEstimate()
        if (totalFuel > 0) distance / totalFuel else dashKmL
    } else trip.effectiveKmL
    val displayFuelCost = if (isEditing) {
        val movingFuel = if (dashKmL > 0) distance / dashKmL else 0.0
        (movingFuel + trip.idleFuelEstimate()) * trip.fuelPricePerLiter
    } else trip.fuelCost
    val displayNetProfit = displayFare + displayTip - displayFuelCost
    val totalIncome = displayFare + displayTip

    // เมตริกที่คำนวณ
    val incomePerKm = if (displayDistance > 0) totalIncome / displayDistance else 0.0
    val fuelCostPerKm = if (displayDistance > 0) displayFuelCost / displayDistance else 0.0
    val profitRatio = if (displayFuelCost > 0) displayNetProfit / displayFuelCost else 0.0
    // กำไรเป็น % ต่อค่าน้ำมัน
    val profitPercent = if (displayFuelCost > 0) (displayNetProfit / displayFuelCost) * 100.0 else 0.0

    var showDeleteDialog by remember { mutableStateOf(false) }

    fun cancelEdit() {
        editDateString = trip.dateString
        fareText = String.format(Locale.US, "%.0f", trip.fare)
        tipText = if (trip.tip > 0) String.format(Locale.US, "%.0f", trip.tip) else ""
        distanceText = String.format(Locale.US, "%.1f", trip.distanceKm)
        dashKmLText = String.format(Locale.US, "%.1f", trip.dashKmL)
        durationText = trip.durationMinutes.toString()
        isEditing = false
    }

    BackHandler {
        if (isEditing) {
            cancelEdit()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        AnimatedContent(
                            targetState = isEditing,
                            transitionSpec = {
                                (slideInVertically(tween(200)) { -it / 2 } + fadeIn(tween(200))) togetherWith
                                (slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(180)))
                            },
                            label = "TripTitleAnim"
                        ) { editing ->
                            Text(
                                text = if (editing) "แก้ไขรอบวิ่ง #$tripNumber" else "รอบวิ่ง #$tripNumber",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) {
                            cancelEdit()
                        } else {
                            onBack()
                        }
                    }) {
                        AnimatedContent(
                            targetState = isEditing,
                            transitionSpec = {
                                (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.8f)) togetherWith
                                (fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.8f))
                            },
                            label = "NavIconAnim"
                        ) { editing ->
                            Icon(
                                imageVector = if (editing) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (editing) "ยกเลิกการแก้ไข" else "กลับ"
                            )
                        }
                    }
                },
                actions = {
                    AnimatedContent(
                        targetState = isEditing,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                        },
                        label = "TopBarActionsAnim"
                    ) { editing ->
                        if (editing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // ปุ่มลบรอบวิ่ง (แสดงเฉพาะในหน้าแก้ไข)
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "ลบรอบนี้",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }

                                Button(
                                    onClick = {
                                        val d = durationText.toIntOrNull() ?: trip.durationMinutes
                                        onSaveEdit(fare, tip, distance, dashKmL, d, editDateString)
                                        isEditing = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "บันทึก",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            // หน้ารายละเอียด/สรุปต่อรอบ แสดงเฉพาะปุ่มแก้ไข
                            IconButton(onClick = { isEditing = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "แก้ไข",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── ส่วนกรอกข้อมูล (เมื่อกดแก้ไข) ──────────────────────────────
            AnimatedVisibility(
                visible = isEditing,
                enter = expandVertically(tween(280)) + fadeIn(tween(240)),
                exit = shrinkVertically(tween(240)) + fadeOut(tween(200))
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "✏️ แก้ไขข้อมูลรอบวิ่ง",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // แถบเลือกวันที่รอบวิ่ง
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showDatePickerModal = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = GrabGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "วันที่: ${DateUtils.formatThaiDateWithDay(editDateString)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "เปลี่ยนวัน 📅",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GrabGreen
                                )
                            }
                        }

                        if (showDatePickerModal) {
                            DatePickerModal(
                                initialDateString = editDateString,
                                onDateSelected = { newDate ->
                                    editDateString = newDate
                                },
                                onDismiss = { showDatePickerModal = false }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AutoSelectTextField(
                                value = fareText,
                                onValueChange = { fareText = it },
                                label = { Text("ค่ารอบ (฿)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            AutoSelectTextField(
                                value = tipText,
                                onValueChange = { tipText = it },
                                label = { Text("ทิป (฿)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AutoSelectTextField(
                                value = distanceText,
                                onValueChange = { distanceText = it },
                                label = { Text("ระยะทาง (กม.)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            AutoSelectTextField(
                                value = dashKmLText,
                                onValueChange = { dashKmLText = it },
                                label = { Text("หน้าปัด (km/L)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        AutoSelectTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("เวลารวมในรอบ (นาที)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── กำไรสุทธิ (Big Number) ───────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "กำไร/ขาดทุนรอบนี้",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AnimatedContent(
                        targetState = displayNetProfit,
                        transitionSpec = {
                            (slideInVertically(tween(220)) { -it / 3 } + fadeIn(tween(200))) togetherWith
                            (slideOutVertically(tween(180)) { it / 3 } + fadeOut(tween(160)))
                        },
                        label = "DetailProfitAnim"
                    ) { profit ->
                        val profitColor = if (profit >= 0) GrabGreen else MaterialTheme.colorScheme.error
                        val profitSign = if (profit >= 0) "+" else ""
                        Text(
                            text = "$profitSign${String.format(Locale.US, "฿%.2f", profit)}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = profitColor
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // สัดส่วนกำไร: เป็น x เท่าของค่าน้ำมัน และเป็น % ต่อค่าน้ำมัน
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // กำไรเป็น X เท่าของค่าน้ำมัน
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (profitRatio > 0) GrabGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (displayFuelCost > 0) {
                                        if (profitRatio >= 0) {
                                            String.format(Locale.US, "%.1f×", profitRatio)
                                        } else {
                                            String.format(Locale.US, "-%.1f×", abs(profitRatio))
                                        }
                                    } else "-",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (profitRatio > 0) GrabGreen else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "กำไรต่อค่าน้ำมัน",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // กำไรเป็น % ต่อค่าน้ำมัน
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (profitPercent > 0) GrabGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val sign = if (profitPercent >= 0) "+" else ""
                                Text(
                                    text = if (displayFuelCost > 0) {
                                        "$sign${String.format(Locale.US, "%.1f", profitPercent)}%"
                                    } else "-",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (profitPercent > 0) GrabGreen else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "% กำไรต่อค่าน้ำมัน",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── ตารางรายละเอียดทั้งหมด ─────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = "รายละเอียดรอบวิ่ง",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    DetailRow("วันที่รอบวิ่ง", DateUtils.formatThaiDateWithDay(trip.dateString))
                    DetailRow("เวลาที่บันทึก", SimpleDateFormat("HH:mm น.", Locale.getDefault()).format(Date(trip.timestamp)))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow("ค่ารอบ", "฿${String.format(Locale.US, "%.2f", displayFare)}")
                    if (displayTip > 0) DetailRow("ทิป", "+฿${String.format(Locale.US, "%.2f", displayTip)}", valueColor = GrabGreen)
                    DetailRow("รายรับรวม", "฿${String.format(Locale.US, "%.2f", totalIncome)}", bold = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow("ระยะทาง", "${String.format(Locale.US, "%.2f", displayDistance)} กม.")
                    DetailRow("เวลาในรอบ", "${trip.durationMinutes} นาที")
                    if (trip.estimatedIdleMinutes > 0) {
                        DetailRow("จอดนิ่ง/รถติดโดยประมาณ", "~${trip.estimatedIdleMinutes} นาที", valueColor = IdleAmber)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow("หน้าปัดรถ (Dash km/L)", "${String.format(Locale.US, "%.1f", trip.dashKmL)} km/L")
                    DetailRow("อัตราสิ้นเปลืองจริง (Effective)", "${String.format(Locale.US, "%.1f", displayEffectiveKmL)} km/L", valueColor = GrabGreen)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow("ราคาน้ำมัน", "฿${String.format(Locale.US, "%.2f", trip.fuelPricePerLiter)}/ลิตร")
                    DetailRow("น้ำมันที่ใช้จริง", "${String.format(Locale.US, "%.3f", trip.totalFuelUsedLiters)} ลิตร")
                    DetailRow("ค่าน้ำมันรวม", "฿${String.format(Locale.US, "%.2f", displayFuelCost)}", valueColor = CostOrange)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    DetailRow("รายรับต่อกิโลเมตร", "฿${String.format(Locale.US, "%.2f", incomePerKm)}/กม.")
                    DetailRow("ค่าน้ำมันต่อกิโลเมตร", "฿${String.format(Locale.US, "%.2f", fuelCostPerKm)}/กม.", valueColor = CostOrange)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // กำไร/ขาดทุน สรุปท้ายสุด
                    DetailRow(
                        label = if (displayNetProfit >= 0) "✅ กำไรสุทธิ" else "❌ ขาดทุน",
                        value = "${if (displayNetProfit >= 0) "+" else ""}฿${String.format(Locale.US, "%.2f", displayNetProfit)}",
                        valueColor = if (displayNetProfit >= 0) GrabGreen else MaterialTheme.colorScheme.error,
                        bold = true
                    )
                    DetailRow(
                        label = "กำไรเทียบค่าน้ำมัน",
                        value = if (displayFuelCost > 0) {
                            val sign = if (profitPercent >= 0) "+" else ""
                            if (profitRatio >= 0) {
                                "ได้ ${String.format(Locale.US, "%.1f", profitRatio)}× ค่าน้ำมัน ($sign${String.format(Locale.US, "%.1f", profitPercent)}%)"
                            } else {
                                "ขาดทุน ${String.format(Locale.US, "%.1f", abs(profitPercent))}% ของค่าน้ำมัน"
                            }
                        } else "-",
                        valueColor = if (profitRatio > 0) GrabGreen else MaterialTheme.colorScheme.error
                    )
                }
            }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "ยืนยันการลบรอบวิ่ง #$tripNumber",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("คุณต้องการลบบันทึกรอบวิ่งนี้ใช่หรือไม่? ข้อมูลจะไม่สามารถกู้คืนได้")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteTrip()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ลบรอบนี้", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ฟังก์ชันช่วยประมาณน้ำมัน Idling จากข้อมูลที่บันทึก
private fun TripRecordEntity.idleFuelEstimate(): Double {
    return totalFuelUsedLiters - (if (dashKmL > 0) distanceKm / dashKmL else 0.0)
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
        )
    }
}
