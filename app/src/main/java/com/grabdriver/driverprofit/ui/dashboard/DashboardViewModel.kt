package com.grabdriver.driverprofit.ui.dashboard

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import com.grabdriver.driverprofit.data.local.entity.VehicleProfileEntity
import com.grabdriver.driverprofit.data.repository.TripRepository
import com.grabdriver.driverprofit.domain.model.DailySummary
import com.grabdriver.driverprofit.domain.usecase.CalculateTripUseCase
import com.grabdriver.driverprofit.domain.usecase.ExportTripsToCsvUseCase
import com.grabdriver.driverprofit.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val repository: TripRepository
) : ViewModel() {

    private val calculateTripUseCase = CalculateTripUseCase()
    private val exportTripsToCsvUseCase = ExportTripsToCsvUseCase()

    val todayDateString: String = DateUtils.getTodayDateString()

    // วันที่กำลังเลือกดูอยู่บน Dashboard (เริ่มต้นเป็นวันนี้)
    private val _selectedDateString = MutableStateFlow(todayDateString)
    val selectedDateString: StateFlow<String> = _selectedDateString.asStateFlow()

    val selectedVehicle: StateFlow<VehicleProfileEntity?> = repository.getSelectedVehicle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allVehicles: StateFlow<List<VehicleProfileEntity>> = repository.getAllVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailySummary: StateFlow<DailySummary> = _selectedDateString
        .flatMapLatest { date -> repository.getDailySummary(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummary(todayDateString))

    @OptIn(ExperimentalCoroutinesApi::class)
    val trips: StateFlow<List<TripRecordEntity>> = _selectedDateString
        .flatMapLatest { date -> repository.getTripsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val fuelPrice: StateFlow<Double> = _selectedDateString
        .flatMapLatest { date ->
            repository.getFuelSettingForDate(date).map { setting ->
                setting?.fuelPricePerLiter
                    ?: repository.getLatestFuelSetting()?.fuelPricePerLiter
                    ?: 38.50
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 38.50)

    val defaultDashKmL: StateFlow<Double> = combine(trips, selectedVehicle) { tripList, vehicle ->
        if (tripList.isNotEmpty()) {
            tripList.first().dashKmL
        } else {
            vehicle?.defaultKmL ?: 50.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50.0)

    fun selectDate(dateString: String) {
        _selectedDateString.value = dateString
    }

    fun selectPreviousDay() {
        _selectedDateString.value = DateUtils.addDays(_selectedDateString.value, -1)
    }

    fun selectNextDay() {
        val next = DateUtils.addDays(_selectedDateString.value, 1)
        if (DateUtils.isPastDate(next) || next == todayDateString) {
            _selectedDateString.value = next
        }
    }

    fun selectToday() {
        _selectedDateString.value = todayDateString
    }

    fun isSelectedDateToday(): Boolean {
        return _selectedDateString.value == todayDateString
    }

    fun saveTrip(
        fare: Double,
        tip: Double,
        distanceKm: Double,
        dashKmL: Double,
        durationMinutes: Int,
        targetDateString: String = _selectedDateString.value
    ) {
        viewModelScope.launch {
            val vehicle = selectedVehicle.value ?: repository.getSelectedVehicleDirect()

            // ดึงราคาน้ำมันของวันที่บันทึก (ถ้ายังไม่มีการตั้ง ให้ใช้ราคาปัจจุบัน/ล่าสุด)
            val fuelPriceForDate = repository.getFuelSettingForDateDirect(targetDateString)?.fuelPricePerLiter
                ?: repository.getLatestFuelSetting()?.fuelPricePerLiter
                ?: fuelPrice.value

            val vehicleType = vehicle?.vehicleType ?: "MOTORCYCLE"
            val idleRate = vehicle?.idleLiterPerHour ?: 0.15

            val result = calculateTripUseCase(
                fare = fare,
                tip = tip,
                distanceKm = distanceKm,
                dashKmL = dashKmL,
                durationMinutes = durationMinutes,
                fuelPricePerLiter = fuelPriceForDate,
                vehicleType = vehicleType,
                idleLiterPerHour = idleRate
            )

            val tripTimestamp = DateUtils.createTimestampForDate(targetDateString)

            val tripEntity = TripRecordEntity(
                timestamp = tripTimestamp,
                dateString = targetDateString,
                fare = fare,
                tip = tip,
                distanceKm = distanceKm,
                dashKmL = dashKmL,
                durationMinutes = durationMinutes,
                fuelPricePerLiter = fuelPriceForDate,
                estimatedIdleMinutes = result.estimatedIdleMinutes,
                effectiveKmL = result.effectiveKmL,
                totalFuelUsedLiters = result.totalFuelLiters,
                fuelCost = result.totalFuelCost,
                netProfit = result.netProfit,
                vehicleId = vehicle?.id ?: 1
            )

            repository.insertTrip(tripEntity)

            // สลับมุมมองไปยังวันที่บันทึก เพื่อให้ผู้ใช้เห็นข้อมูลที่เพิ่งใส่ทันที
            _selectedDateString.value = targetDateString
        }
    }

    fun deleteTrip(trip: TripRecordEntity) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
        }
    }

    fun updateTrip(
        original: TripRecordEntity,
        fare: Double,
        tip: Double,
        distanceKm: Double,
        dashKmL: Double,
        durationMinutes: Int,
        dateString: String = original.dateString
    ) {
        viewModelScope.launch {
            val vehicle = allVehicles.value.firstOrNull { it.id == original.vehicleId }
                ?: selectedVehicle.value
                ?: repository.getSelectedVehicleDirect()

            val vehicleType = vehicle?.vehicleType ?: "MOTORCYCLE"
            val idleRate = vehicle?.idleLiterPerHour ?: 0.15

            val fuelPriceForDate = if (dateString != original.dateString) {
                repository.getFuelSettingForDateDirect(dateString)?.fuelPricePerLiter
                    ?: repository.getLatestFuelSetting()?.fuelPricePerLiter
                    ?: original.fuelPricePerLiter
            } else {
                original.fuelPricePerLiter
            }

            val result = calculateTripUseCase(
                fare = fare,
                tip = tip,
                distanceKm = distanceKm,
                dashKmL = dashKmL,
                durationMinutes = durationMinutes,
                fuelPricePerLiter = fuelPriceForDate,
                vehicleType = vehicleType,
                idleLiterPerHour = idleRate
            )

            val updatedTimestamp = if (dateString != original.dateString) {
                DateUtils.createTimestampForDate(dateString, original.timestamp)
            } else {
                original.timestamp
            }

            repository.updateTrip(
                original.copy(
                    timestamp = updatedTimestamp,
                    dateString = dateString,
                    fare = fare,
                    tip = tip,
                    distanceKm = distanceKm,
                    dashKmL = dashKmL,
                    durationMinutes = durationMinutes,
                    fuelPricePerLiter = fuelPriceForDate,
                    estimatedIdleMinutes = result.estimatedIdleMinutes,
                    effectiveKmL = result.effectiveKmL,
                    totalFuelUsedLiters = result.totalFuelLiters,
                    fuelCost = result.totalFuelCost,
                    netProfit = result.netProfit
                )
            )

            if (dateString != _selectedDateString.value) {
                _selectedDateString.value = dateString
            }
        }
    }

    fun updateTripTip(tripId: Long, newTip: Double) {
        viewModelScope.launch {
            repository.updateTripTip(tripId, newTip)
        }
    }

    fun updateFuelPrice(price: Double) {
        viewModelScope.launch {
            repository.updateFuelPriceForDate(_selectedDateString.value, price)
        }
    }

    fun selectVehicle(vehicleId: Int) {
        viewModelScope.launch {
            repository.setSelectedVehicle(vehicleId)
        }
    }

    fun addNewVehicle(name: String, type: String, defaultKmL: Double, idleRate: Double) {
        viewModelScope.launch {
            val newId = repository.insertVehicle(
                VehicleProfileEntity(
                    name = name,
                    vehicleType = type,
                    defaultKmL = defaultKmL,
                    idleLiterPerHour = idleRate,
                    isSelected = true
                )
            )
            repository.setSelectedVehicle(newId.toInt())
        }
    }

    fun updateVehicle(id: Int, name: String, type: String, defaultKmL: Double, idleRate: Double) {
        viewModelScope.launch {
            val isCurrent = selectedVehicle.value?.id == id
            repository.updateVehicle(
                VehicleProfileEntity(
                    id = id,
                    name = name,
                    vehicleType = type,
                    defaultKmL = defaultKmL,
                    idleLiterPerHour = idleRate,
                    isSelected = isCurrent
                )
            )
        }
    }

    fun deleteVehicle(vehicle: VehicleProfileEntity) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
        }
    }

    fun exportCsvAndShare(context: Context) {
        viewModelScope.launch {
            val date = _selectedDateString.value
            val allTrips = repository.getTripsForDateDirect(date)
            if (allTrips.isEmpty()) return@launch

            val csvContent = exportTripsToCsvUseCase(allTrips)
            val fileName = "Grab_Trip_Report_$date.csv"
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { fos ->
                fos.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "รายงานค่ารอบ Grab วันที่ $date")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "แชร์รายงานค่ารอบและค่าน้ำมัน"))
        }
    }
}

class DashboardViewModelFactory(
    private val repository: TripRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
