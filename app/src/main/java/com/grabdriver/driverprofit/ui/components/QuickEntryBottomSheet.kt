package com.grabdriver.driverprofit.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.grabdriver.driverprofit.util.DateUtils
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grabdriver.driverprofit.domain.model.TripCalculationResult
import com.grabdriver.driverprofit.domain.usecase.CalculateTripUseCase
import com.grabdriver.driverprofit.ui.theme.CostOrange
import com.grabdriver.driverprofit.ui.theme.GrabGreen
import com.grabdriver.driverprofit.ui.theme.IdleAmber
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEntryBottomSheet(
    defaultDashKmL: Double,
    fuelPricePerLiter: Double,
    vehicleType: String,
    idleLiterPerHour: Double,
    initialDateString: String = DateUtils.getTodayDateString(),
    onDismiss: () -> Unit,
    onSaveTrip: (
        fare: Double,
        tip: Double,
        distanceKm: Double,
        dashKmL: Double,
        durationMinutes: Int,
        dateString: String
    ) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val grabCollectFocusRequester = remember { FocusRequester() }
    val calculateUseCase = remember { CalculateTripUseCase() }

    var selectedDateString by remember { mutableStateOf(initialDateString) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    var fareText by remember { mutableStateOf("") }
    var distanceText by remember { mutableStateOf("") }
    val initialDashKmL = remember(defaultDashKmL) {
        if (defaultDashKmL > 0) String.format(Locale.US, "%.1f", defaultDashKmL) else "50.0"
    }
    var lastValidDashKmL by remember { mutableStateOf(initialDashKmL) }
    var dashKmLText by remember { mutableStateOf(initialDashKmL) }
    var durationText by remember { mutableStateOf("") }
    var tipText by remember { mutableStateOf("") }
    var showCashTipCalculator by remember { mutableStateOf(false) }
    var grabCollectText by remember { mutableStateOf("") }
    var cashReceivedText by remember { mutableStateOf("") }

    // แปลงตัวเลขเพื่อคำนวณสดแบบ Live Preview
    val fare = fareText.toDoubleOrNull() ?: 0.0
    val distance = distanceText.toDoubleOrNull() ?: 0.0
    val dashKmL = dashKmLText.toDoubleOrNull() ?: defaultDashKmL
    val duration = durationText.toIntOrNull() ?: 0
    val tip = tipText.toDoubleOrNull() ?: 0.0

    val liveCalc: TripCalculationResult = remember(fare, tip, distance, dashKmL, duration, fuelPricePerLiter) {
        calculateUseCase(
            fare = fare,
            tip = tip,
            distanceKm = distance,
            dashKmL = dashKmL,
            durationMinutes = duration,
            fuelPricePerLiter = fuelPricePerLiter,
            vehicleType = vehicleType,
            idleLiterPerHour = idleLiterPerHour
        )
    }

    val isInputValid = fare > 0.0 && distance > 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ บันทึกรอบวิ่งด่วน",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ราคาน้ำมัน ฿${String.format(Locale.US, "%.2f", fuelPricePerLiter)}/L",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // แถบเลือกวันที่ของรอบวิ่ง (สำหรับบันทึกย้อนหลังได้)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (DateUtils.isToday(selectedDateString)) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else GrabGreen.copy(alpha = 0.12f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDatePickerDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "วันที่",
                            tint = if (DateUtils.isToday(selectedDateString)) MaterialTheme.colorScheme.onSurfaceVariant else GrabGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (DateUtils.isToday(selectedDateString)) {
                                    "วันที่: วันนี้ (${DateUtils.formatThaiDateShort(selectedDateString)})"
                                } else {
                                    "วันที่: ${DateUtils.formatThaiDateWithDay(selectedDateString)}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (DateUtils.isToday(selectedDateString)) MaterialTheme.colorScheme.onSurface else GrabGreen
                            )
                            if (!DateUtils.isToday(selectedDateString)) {
                                Text(
                                    text = "⏳ กำลังบันทึกย้อนหลัง",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GrabGreen
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "เปลี่ยนวัน 📅",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GrabGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (showDatePickerDialog) {
                DatePickerModal(
                    initialDateString = selectedDateString,
                    onDateSelected = { newDate ->
                        selectedDateString = newDate
                    },
                    onDismiss = { showDatePickerDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // แถวที่ 1: ค่ารอบ & ระยะทาง (ช่องจำเป็น - แตะแล้วไฮไลต์ทั้งหมด)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AutoSelectTextField(
                    value = fareText,
                    onValueChange = { fareText = it },
                    label = { Text("ค่ารอบ (บาท) *") },
                    placeholder = { Text("เช่น 120") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                AutoSelectTextField(
                    value = distanceText,
                    onValueChange = { distanceText = it },
                    label = { Text("ระยะทาง (กม.) *") },
                    placeholder = { Text("เช่น 6.5") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // แถวที่ 2: หน้าปัดรถ (km/L) & เวลาที่ใช้ในรอบ (นาที)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AutoSelectTextField(
                    value = dashKmLText,
                    onValueChange = { newVal ->
                        dashKmLText = newVal
                        val num = newVal.toDoubleOrNull()
                        if (num != null && num > 0.0) {
                            lastValidDashKmL = newVal
                        }
                    },
                    label = { Text("หน้าปัด (km/L)") },
                    leadingIcon = {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .clickable {
                                        val curr = dashKmLText.toDoubleOrNull() ?: lastValidDashKmL.toDoubleOrNull() ?: 50.0
                                        val next = String.format(Locale.US, "%.1f", kotlin.math.max(1.0, curr - 0.1))
                                        dashKmLText = next
                                        lastValidDashKmL = next
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("−", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .clickable {
                                        val curr = dashKmLText.toDoubleOrNull() ?: lastValidDashKmL.toDoubleOrNull() ?: 50.0
                                        val next = String.format(Locale.US, "%.1f", curr + 0.1)
                                        dashKmLText = next
                                        lastValidDashKmL = next
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("+", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    },
                    onBlur = {
                        val num = dashKmLText.toDoubleOrNull()
                        if (dashKmLText.isBlank() || num == null || num <= 0.0) {
                            dashKmLText = lastValidDashKmL
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                AutoSelectTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("เวลารวม (นาที)") },
                    placeholder = { Text("เช่น 20") },
                    leadingIcon = {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ปุ่มเพิ่มทิป + cash tip calculator
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Material UI–style deletable chip
                // ไม่มีทิป → ปุ่ม "💵 เพิ่มทิป"   |   มีทิป → pill chip [ทิป ฿20  ●✕]
                if (tip > 0.0) {
                    // ── Deletable chip (pill shape เดียว กด body = เปิด calculator) ──
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = GrabGreen,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .clickable { showCashTipCalculator = !showCashTipCalculator }
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "ทิป ฿${tip.toInt()}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            // วงกลม delete — nested clickable แยกจาก chip body
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                    ) {
                                        tipText = ""
                                        grabCollectText = ""
                                        cashReceivedText = ""
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "ยกเลิกทิป",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // ── ปุ่มปกติ: เพิ่มทิป ──
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (showCashTipCalculator) GrabGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .clickable { showCashTipCalculator = !showCashTipCalculator }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "💵", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "เพิ่มทิป",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (showCashTipCalculator) GrabGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                LaunchedEffect(showCashTipCalculator) {
                    if (showCashTipCalculator) {
                        delay(150)
                        grabCollectFocusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }

                AnimatedVisibility(
                    visible = showCashTipCalculator,
                    enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                    exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
                ) {
                    val collectAmount = grabCollectText.toDoubleOrNull() ?: 0.0
                    val receivedAmount = cashReceivedText.toDoubleOrNull() ?: 0.0
                    val cashTip = kotlin.math.max(0.0, receivedAmount - collectAmount)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💵 คำนวณทิปเงินสด (ลูกค้าจ่ายเกิน)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GrabGreen
                                )
                                IconButton(
                                    onClick = { showCashTipCalculator = false },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "ปิด",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AutoSelectTextField(
                                    value = grabCollectText,
                                    onValueChange = { grabCollectText = it },
                                    label = { Text("ยอดเก็บตามแอป (฿)") },
                                    placeholder = { Text("เช่น 85") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(grabCollectFocusRequester)
                                )
                                AutoSelectTextField(
                                    value = cashReceivedText,
                                    onValueChange = { cashReceivedText = it },
                                    label = { Text("ลูกค้าจ่ายจริง (฿)") },
                                    placeholder = { Text("เช่น 100") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // chip +N สำหรับกดสะสมยอดลูกค้าจ่ายจริง (MUI pill style)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // แถว 1: +1  +5  +10  +20  +50
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "จ่าย:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(44.dp)
                                    )
                                    listOf(1, 5, 10, 20, 50).forEach { amount ->
                                        Surface(
                                            shape = RoundedCornerShape(50.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50.dp))
                                                .clickable {
                                                    val current = cashReceivedText.toDoubleOrNull() ?: 0.0
                                                    cashReceivedText = (current + amount).toInt().toString()
                                                }
                                        ) {
                                            Text(
                                                text = "+$amount",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                // แถว 2: +100  +500  +1000  [ล้าง]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Spacer(modifier = Modifier.width(44.dp))
                                    listOf(100, 500, 1000).forEach { amount ->
                                        Surface(
                                            shape = RoundedCornerShape(50.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50.dp))
                                                .clickable {
                                                    val current = cashReceivedText.toDoubleOrNull() ?: 0.0
                                                    cashReceivedText = (current + amount).toInt().toString()
                                                }
                                        ) {
                                            Text(
                                                text = "+$amount",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    // ปุ่มล้าง — pill สีแดงอ่อน
                                    Surface(
                                        shape = RoundedCornerShape(50.dp),
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .clickable { cashReceivedText = "" }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "ล้าง",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Text(
                                                text = "ล้าง",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }

                            // ผลการคำนวณและปุ่มยืนยัน
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (cashTip > 0.0) {
                                        "🎉 ได้ทิป: +฿${cashTip.toInt()} ($cashReceivedText - $grabCollectText)"
                                    } else if (receivedAmount > 0.0 && collectAmount > 0.0) {
                                        "จ่ายพอดี / ไม่มีทิป"
                                    } else {
                                        "ระบุยอดเพื่อคำนวณทิป"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cashTip > 0.0) GrabGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = {
                                        if (cashTip > 0.0) {
                                            tipText = cashTip.toInt().toString()
                                        }
                                        showCashTipCalculator = false
                                    },
                                    enabled = cashTip > 0.0,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "ใช้ทิปนี้",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // การ์ดพรีวิวผลลัพธ์คำนวณสด (Live Calculation Preview Card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 ผลคำนวณสดรอบนี้",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedContent(
                            targetState = liveCalc.netProfit,
                            transitionSpec = {
                                (slideInVertically(tween(200)) { -it / 3 } + fadeIn(tween(200))) togetherWith
                                (slideOutVertically(tween(180)) { it / 3 } + fadeOut(tween(150)))
                            },
                            label = "LiveCalcNetProfitAnim"
                        ) { profit ->
                            Text(
                                text = String.format(Locale.US, "กำไรสุทธิ: +฿%.2f", profit),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GrabGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• กินน้ำมันจริง: ${String.format(Locale.US, "%.1f", liveCalc.effectiveKmL)} km/L",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• ค่าน้ำมัน: ฿${String.format(Locale.US, "%.2f", liveCalc.totalFuelCost)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CostOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (liveCalc.estimatedIdleMinutes > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "• จอดติดไฟแดง/รอ: ~ ${liveCalc.estimatedIdleMinutes} นาที (ใช้น้ำมันเดินเบา ${String.format(Locale.US, "%.2f", liveCalc.idleFuelLiters)} ลิตร)",
                            style = MaterialTheme.typography.bodySmall,
                            color = IdleAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ปุ่มบันทึกใหญ่ กว้างเต็มจอ กดง่ายด้วยนิ้วโป้ง
            Button(
                onClick = {
                    if (isInputValid) {
                        onSaveTrip(fare, tip, distance, dashKmL, duration, selectedDateString)
                        onDismiss()
                    }
                },
                enabled = isInputValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GrabGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                val isToday = DateUtils.isToday(selectedDateString)
                val btnText = if (isToday) "บันทึกรอบนี้ทันที" else "บันทึกรอบ (${DateUtils.formatThaiDateShort(selectedDateString)})"
                Text(
                    text = btnText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
