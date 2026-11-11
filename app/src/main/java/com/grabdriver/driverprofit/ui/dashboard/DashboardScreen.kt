package com.grabdriver.driverprofit.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import com.grabdriver.driverprofit.domain.model.TripFilter
import com.grabdriver.driverprofit.ui.components.*
import com.grabdriver.driverprofit.ui.theme.CostOrange
import com.grabdriver.driverprofit.ui.theme.GrabGreen
import com.grabdriver.driverprofit.ui.tripdetail.TripDetailScreen
import com.grabdriver.driverprofit.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dailySummary by viewModel.dailySummary.collectAsState()
    val trips by viewModel.trips.collectAsState()
    val tripFilter by viewModel.tripFilter.collectAsState()
    val filteredTrips by viewModel.filteredTrips.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()
    val fuelPrice by viewModel.fuelPrice.collectAsState()
    val defaultDashKmL by viewModel.defaultDashKmL.collectAsState()
    val selectedDateString by viewModel.selectedDateString.collectAsState()
    val isIdleCalculationEnabled by viewModel.isIdleCalculationEnabled.collectAsState()
    val isToday = selectedDateString == viewModel.todayDateString

    var showQuickEntrySheet by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showEditFuelDialog by remember { mutableStateOf(false) }
    var showVehicleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var tripForAddTip by remember { mutableStateOf<TripRecordEntity?>(null) }
    // state สำหรับหน้ารายละเอียด/แก้ไขรอบ
    var tripForDetailId by remember { mutableStateOf<Long?>(null) }
    var lastDetailTrip by remember { mutableStateOf<TripRecordEntity?>(null) }

    val currentDetailTrip = trips.firstOrNull { it.id == tripForDetailId }
    LaunchedEffect(currentDetailTrip) {
        if (currentDetailTrip != null) {
            lastDetailTrip = currentDetailTrip
        }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // เมื่อเปลี่ยนวันที่ที่เลือก ให้เลื่อนกลับไปด้านบนสุด (item 0)
    LaunchedEffect(selectedDateString) {
        listState.scrollToItem(0)
    }

    AnimatedContent(
        targetState = tripForDetailId,
        transitionSpec = {
            if (targetState != null) {
                // ไปหน้ารายละเอียดรอบ (Slide in จากขวา)
                (slideInHorizontally(tween(300)) { fullWidth -> fullWidth } + fadeIn(tween(300))) togetherWith
                (slideOutHorizontally(tween(250)) { fullWidth -> -fullWidth / 3 } + fadeOut(tween(200)))
            } else {
                // ถอยกลับมาหน้าหลัก (Slide ออกไปทางขวา)
                (slideInHorizontally(tween(250)) { fullWidth -> -fullWidth / 3 } + fadeIn(tween(250))) togetherWith
                (slideOutHorizontally(tween(300)) { fullWidth -> fullWidth } + fadeOut(tween(200)))
            }
        },
        label = "DashboardScreenNavigationAnim"
    ) { detailId ->
        val activeTrip = if (detailId != null) (trips.firstOrNull { it.id == detailId } ?: lastDetailTrip) else null
        if (detailId != null && activeTrip != null) {
            val isDeadhead = activeTrip.fare == 0.0
            val paidTripNumber = if (!isDeadhead) {
                trips.filter { it.fare > 0.0 && (it.timestamp < activeTrip.timestamp || (it.timestamp == activeTrip.timestamp && it.id <= activeTrip.id)) }.size
            } else 0
            val tripLabel = if (isDeadhead) "รอบตีรถเปล่า" else "รอบ #$paidTripNumber"

            TripDetailScreen(
                trip = activeTrip,
                tripNumber = paidTripNumber,
                isIdleCalculationEnabled = isIdleCalculationEnabled,
                onBack = { tripForDetailId = null },
                onDeleteTrip = {
                    viewModel.deleteTrip(activeTrip)
                    Toast.makeText(context, "ลบ$tripLabel เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                    tripForDetailId = null
                },
                onSaveEdit = { fare, tip, distanceKm, dashKmL, durationMinutes, dateString ->
                    viewModel.updateTrip(
                        original = activeTrip,
                        fare = fare,
                        tip = tip,
                        distanceKm = distanceKm,
                        dashKmL = dashKmL,
                        durationMinutes = durationMinutes,
                        dateString = dateString
                    )
                    Toast.makeText(context, "แก้ไขข้อมูล$tripLabel สำเร็จ!", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // ปุ่มบันทึกด่วนขนาดใหญ่ ชัดเจน เหมาะกับคนขับ
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { showQuickEntrySheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrabGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isToday) "บันทึกรอบใหม่ (ด่วน)" else "บันทึกรอบ (${DateUtils.formatThaiDateShort(selectedDateString)})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "dashboard_header") {
                Spacer(modifier = Modifier.height(4.dp))
                // แถวบน: ชื่อแอป และ ปุ่มกลับไปวันนี้ (กรณีดูย้อนหลัง)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DriverProfit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = GrabGreen
                            )
                        }
                        Text(
                            text = if (isToday) "วันนี้, ${DateUtils.formatThaiDateFull(selectedDateString)}" else DateUtils.formatThaiDateFull(selectedDateString),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!isToday) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = GrabGreen.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { viewModel.selectToday() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Today,
                                        contentDescription = "กลับไปวันนี้",
                                        tint = GrabGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "กลับไปวันนี้",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GrabGreen
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "ตั้งค่า",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // แถบเลื่อนวันก่อนหน้า-ถัดไป พร้อมปุ่มเปิดปฏิทิน
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isToday) MaterialTheme.colorScheme.surface else GrabGreen.copy(alpha = 0.08f)
                    ),
                    border = if (!isToday) BorderStroke(1.dp, GrabGreen.copy(alpha = 0.3f)) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.selectPreviousDay() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "วันก่อนหน้า",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // ปุ่มกดเปิดปฏิทิน
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isToday) GrabGreen.copy(alpha = 0.12f) else GrabGreen,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showDatePickerDialog = true }
                                .animateContentSize()
                        ) {
                            AnimatedContent(
                                targetState = selectedDateString,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        (slideInHorizontally(tween(220)) { it / 2 } + fadeIn(tween(200))) togetherWith
                                        (slideOutHorizontally(tween(200)) { -it / 2 } + fadeOut(tween(180)))
                                    } else {
                                        (slideInHorizontally(tween(220)) { -it / 2 } + fadeIn(tween(200))) togetherWith
                                        (slideOutHorizontally(tween(200)) { it / 2 } + fadeOut(tween(180)))
                                    }
                                },
                                label = "DateTextAnim"
                            ) { dateStr ->
                                val dateIsToday = DateUtils.isToday(dateStr)
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "ปฏิทิน",
                                        tint = if (dateIsToday) GrabGreen else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = DateUtils.formatThaiDateWithDay(dateStr) + if (dateIsToday) " (วันนี้)" else "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dateIsToday) GrabGreen else Color.White
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.selectNextDay() },
                            enabled = !isToday,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "วันถัดไป",
                                tint = if (!isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                            )
                        }
                    }
                }
            }

            // แถบเลือกรถ & ราคาน้ำมันวันนี้
            item(key = "daily_fuel_banner") {
                DailyFuelBanner(
                    selectedVehicle = selectedVehicle,
                    fuelPrice = fuelPrice,
                    onVehicleClick = { showVehicleDialog = true },
                    onFuelPriceClick = { showEditFuelDialog = true },
                    onExportClick = {
                        if (trips.isEmpty()) {
                            Toast.makeText(context, "ยังไม่มีข้อมูลรอบวิ่งของวันที่ ${DateUtils.formatThaiDateShort(selectedDateString)}", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.exportCsvAndShare(context)
                        }
                    }
                )
            }

            // การ์ดสรุปยอดกำไรสุทธิ รายได้ ค่าน้ำมัน
            item(key = "summary_card") {
                SummaryCard(summary = dailySummary)
            }

            // หัวข้อรายการรอบของวันที่เลือก
            item(key = "trips_header") {
                val paidCount = dailySummary.totalTripsCount
                val deadheadCount = trips.count { it.fare == 0.0 }
                val dateLabel = if (isToday) "วันนี้" else "วันที่ ${DateUtils.formatThaiDateShort(selectedDateString)}"
                val headerText = if (deadheadCount > 0) {
                    "รายการรอบของ$dateLabel ($paidCount รอบ • ตีรถเปล่า $deadheadCount)"
                } else {
                    "รายการรอบของ$dateLabel ($paidCount รอบ)"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = headerText,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                        },
                        label = "TripsHeaderAnim"
                    ) { headerTitle ->
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // แถบ Filter รายการรอบ: ทั้งหมด / เฉพาะรอบปกติ / รอบเปล่า (เริ่มต้นที่เฉพาะรอบปกติ)
            item(key = "trip_filter_bar") {
                val totalCount = trips.size
                val paidCount = trips.count { it.fare > 0.0 }
                val deadheadCount = trips.count { it.fare == 0.0 }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TripFilter.values().forEach { filter ->
                        val isSelected = tripFilter == filter
                        val (filterTitle, count) = when (filter) {
                            TripFilter.ALL -> "ทั้งหมด" to totalCount
                            TripFilter.PAID_ONLY -> "รอบปกติ" to paidCount
                            TripFilter.DEADHEAD -> "รอบเปล่า" to deadheadCount
                        }

                        val activeColor = if (filter == TripFilter.DEADHEAD) CostOrange else GrabGreen

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTripFilter(filter) },
                            label = {
                                Text(
                                    text = "$filterTitle ($count)",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeColor.copy(alpha = 0.15f),
                                selectedLabelColor = activeColor,
                                selectedLeadingIconColor = activeColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = activeColor,
                                selectedBorderWidth = 1.5.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            if (filteredTrips.isEmpty()) {
                item(key = "empty_trips_state") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (trips.isEmpty()) {
                                Text(
                                    text = if (isToday) "🛵 ยังไม่มีบันทึกรอบวิ่งในวันนี้" else "🛵 ไม่มีบันทึกรอบวิ่งในวันที่ ${DateUtils.formatThaiDateShort(selectedDateString)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isToday) "กดปุ่ม 'บันทึกรอบใหม่ (ด่วน)' ด้านล่างเพื่อเริ่มบันทึก" else "กดปุ่มด้านล่างเพื่อบันทึกรอบย้อนหลังของวันนี้",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            } else {
                                val totalCount = trips.size
                                val paidCount = trips.count { it.fare > 0.0 }
                                val deadheadCount = trips.count { it.fare == 0.0 }

                                when (tripFilter) {
                                    TripFilter.PAID_ONLY -> {
                                        Text(
                                            text = "ไม่มีรอบปกติ",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "มีรอบตีรถเปล่าบันทึกไว้ $deadheadCount รอบ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.setTripFilter(TripFilter.DEADHEAD) },
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("ดูรอบตีรถเปล่า ($deadheadCount)", color = CostOrange)
                                            }
                                            Button(
                                                onClick = { viewModel.setTripFilter(TripFilter.ALL) },
                                                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("ดูทั้งหมด ($totalCount)")
                                            }
                                        }
                                    }
                                    TripFilter.DEADHEAD -> {
                                        Text(
                                            text = "ไม่มีรอบตีรถเปล่า",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "มีรอบวิ่งปกติบันทึกไว้ $paidCount รอบ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { viewModel.setTripFilter(TripFilter.PAID_ONLY) },
                                                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("ดูรอบปกติ ($paidCount)")
                                            }
                                            OutlinedButton(
                                                onClick = { viewModel.setTripFilter(TripFilter.ALL) },
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("ดูทั้งหมด ($totalCount)")
                                            }
                                        }
                                    }
                                    TripFilter.ALL -> {
                                        Text(
                                            text = "🛵 ไม่มีข้อมูลรอบวิ่ง",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(
                    items = filteredTrips,
                    key = { trip -> trip.id },
                    contentType = { "trip_card" }
                ) { trip ->
                    val isDeadhead = trip.fare == 0.0
                    val paidTripNumber = if (!isDeadhead) {
                        trips.filter { it.fare > 0.0 && (it.timestamp < trip.timestamp || (it.timestamp == trip.timestamp && it.id <= trip.id)) }.size
                    } else 0

                    TripItemCard(
                        trip = trip,
                        tripNumber = paidTripNumber,
                        onAddOrEditTip = { tripForAddTip = trip },
                        onViewDetail = { tripForDetailId = trip.id }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
}

    // Modal BottomSheet บันทึกด่วน
    if (showQuickEntrySheet) {
        QuickEntryBottomSheet(
            defaultDashKmL = defaultDashKmL,
            fuelPricePerLiter = fuelPrice,
            vehicleType = selectedVehicle?.vehicleType ?: "MOTORCYCLE",
            idleLiterPerHour = selectedVehicle?.idleLiterPerHour ?: 0.15,
            isIdleCalculationEnabled = isIdleCalculationEnabled,
            initialDateString = selectedDateString,
            onDismiss = { showQuickEntrySheet = false },
            onSaveTrip = { fare, tip, distance, dashKmL, duration, dateString ->
                viewModel.saveTrip(fare, tip, distance, dashKmL, duration, dateString)
                val msg = if (dateString == viewModel.todayDateString) "บันทึกรอบวิ่งสำเร็จ!" else "บันทึกรอบของวันที่ ${DateUtils.formatThaiDateShort(dateString)} สำเร็จ!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (dateString == selectedDateString) {
                    coroutineScope.launch {
                        listState.scrollToItem(0)
                    }
                }
            }
        )
    }

    // Dialog ปรับราคาน้ำมันประจำวัน
    if (showEditFuelDialog) {
        EditFuelDialog(
            currentPrice = fuelPrice,
            onDismiss = { showEditFuelDialog = false },
            onSavePrice = { newPrice ->
                viewModel.updateFuelPrice(newPrice)
                Toast.makeText(context, "อัปเดตราคาน้ำมันสำเร็จ", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog จัดการเลือกรุ่นรถ
    if (showVehicleDialog) {
        VehicleSettingDialog(
            vehicles = allVehicles,
            selectedVehicleId = selectedVehicle?.id ?: 1,
            onDismiss = { showVehicleDialog = false },
            onSelectVehicle = { id ->
                viewModel.selectVehicle(id)
            },
            onAddNewVehicle = { name, type, kmL, idle ->
                viewModel.addNewVehicle(name, type, kmL, idle)
                Toast.makeText(context, "เพิ่มรุ่นรถ $name สำเร็จ", Toast.LENGTH_SHORT).show()
            },
            onUpdateVehicle = { id, name, type, kmL, idle ->
                viewModel.updateVehicle(id, name, type, kmL, idle)
                Toast.makeText(context, "แก้ไขรุ่นรถ $name สำเร็จ", Toast.LENGTH_SHORT).show()
            },
            onDeleteVehicle = { vehicle ->
                viewModel.deleteVehicle(vehicle)
                Toast.makeText(context, "ลบรุ่นรถ ${vehicle.name} สำเร็จ", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog เพิ่ม/แก้ไขทิปย้อนหลัง (เมื่อลูกค้าเพิ่งให้ทิปผ่านแอป)
    tripForAddTip?.let { trip ->
        val tripIndex = trips.indexOfFirst { it.id == trip.id }
        val tripNumber = if (tripIndex != -1) trips.size - tripIndex else 1
        AddTipDialog(
            trip = trip,
            tripNumber = tripNumber,
            onDismiss = { tripForAddTip = null },
            onSaveTip = { newTip ->
                viewModel.updateTripTip(trip.id, newTip)
                Toast.makeText(context, "อัปเดตทิปรอบ #$tripNumber เรียบร้อย!", Toast.LENGTH_SHORT).show()
                tripForAddTip = null
            }
        )
    }


    // Dialog ปฏิทินเลือกวันที่
    if (showDatePickerDialog) {
        DatePickerModal(
            initialDateString = selectedDateString,
            onDateSelected = { newDate ->
                viewModel.selectDate(newDate)
            },
            onDismiss = { showDatePickerDialog = false }
        )
    }

    // Dialog ตั้งค่าการใช้งานแอป (เปิด-ปิด การคำนวณ Idle)
    if (showSettingsDialog) {
        AppSettingsDialog(
            isIdleCalculationEnabled = isIdleCalculationEnabled,
            onToggleIdleCalculation = { enabled ->
                viewModel.setIdleCalculationEnabled(enabled)
            },
            onOpenVehicleSettings = { showVehicleDialog = true },
            onOpenFuelSettings = { showEditFuelDialog = true },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
