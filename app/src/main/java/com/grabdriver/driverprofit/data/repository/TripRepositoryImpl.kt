package com.grabdriver.driverprofit.data.repository

import com.grabdriver.driverprofit.data.local.AppDatabase
import com.grabdriver.driverprofit.data.local.entity.DailyFuelSettingEntity
import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import com.grabdriver.driverprofit.data.local.entity.VehicleProfileEntity
import com.grabdriver.driverprofit.domain.model.DailySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepositoryImpl(
    private val database: AppDatabase
) : TripRepository {

    private val tripDao = database.tripDao()
    private val vehicleDao = database.vehicleDao()
    private val dailyFuelDao = database.dailyFuelDao()

    override fun getTripsForDate(dateString: String): Flow<List<TripRecordEntity>> {
        return tripDao.getTripsForDate(dateString)
    }

    override suspend fun getTripsForDateDirect(dateString: String): List<TripRecordEntity> {
        return tripDao.getTripsForDateDirect(dateString)
    }

    override fun getAllTrips(): Flow<List<TripRecordEntity>> {
        return tripDao.getAllTrips()
    }

    override suspend fun getAllTripsDirect(): List<TripRecordEntity> {
        return tripDao.getAllTripsDirect()
    }

    override suspend fun insertTrip(trip: TripRecordEntity): Long {
        return tripDao.insertTrip(trip)
    }

    override suspend fun updateTrip(trip: TripRecordEntity) {
        tripDao.updateTrip(trip)
    }

    override suspend fun deleteTrip(trip: TripRecordEntity) {
        tripDao.deleteTrip(trip)
    }

    override suspend fun deleteTripById(tripId: Long) {
        tripDao.deleteTripById(tripId)
    }

    override suspend fun updateTripTip(tripId: Long, newTip: Double) {
        tripDao.updateTripTip(tripId, newTip)
    }

    override suspend fun getLatestTrip(): TripRecordEntity? {
        return tripDao.getLatestTrip()
    }

    override fun getSelectedVehicle(): Flow<VehicleProfileEntity?> {
        return vehicleDao.getSelectedVehicle()
    }

    override suspend fun getSelectedVehicleDirect(): VehicleProfileEntity? {
        return vehicleDao.getSelectedVehicleDirect()
    }

    override fun getAllVehicles(): Flow<List<VehicleProfileEntity>> {
        return vehicleDao.getAllVehicles()
    }

    override suspend fun getAllVehiclesDirect(): List<VehicleProfileEntity> {
        return vehicleDao.getAllVehiclesDirect()
    }

    override suspend fun setSelectedVehicle(vehicleId: Int) {
        vehicleDao.clearSelectedVehicles()
        vehicleDao.setSelectedVehicle(vehicleId)
    }

    override suspend fun insertVehicle(vehicle: VehicleProfileEntity): Long {
        return vehicleDao.insertVehicle(vehicle)
    }

    override suspend fun updateVehicle(vehicle: VehicleProfileEntity) {
        vehicleDao.updateVehicle(vehicle)
    }

    override suspend fun deleteVehicle(vehicle: VehicleProfileEntity) {
        val wasSelected = vehicle.isSelected || vehicleDao.getSelectedVehicleDirect()?.id == vehicle.id
        vehicleDao.deleteVehicle(vehicle)
        if (wasSelected) {
            val remaining = vehicleDao.getAllVehiclesDirect()
            if (remaining.isNotEmpty()) {
                vehicleDao.setSelectedVehicle(remaining.first().id)
            }
        }
    }

    override fun getFuelSettingForDate(dateString: String): Flow<DailyFuelSettingEntity?> {
        return dailyFuelDao.getFuelSettingForDate(dateString)
    }

    override suspend fun getFuelSettingForDateDirect(dateString: String): DailyFuelSettingEntity? {
        return dailyFuelDao.getFuelSettingForDateDirect(dateString)
    }

    override suspend fun getLatestFuelSetting(): DailyFuelSettingEntity? {
        return dailyFuelDao.getLatestFuelSetting()
    }

    override suspend fun updateFuelPriceForDate(dateString: String, price: Double) {
        val existing = dailyFuelDao.getFuelSettingForDateDirect(dateString)
        val updated = existing?.copy(
            fuelPricePerLiter = price,
            updatedAt = System.currentTimeMillis()
        ) ?: DailyFuelSettingEntity(
            dateString = dateString,
            fuelPricePerLiter = price,
            updatedAt = System.currentTimeMillis()
        )
        dailyFuelDao.insertOrUpdateFuelSetting(updated)
    }

    override fun getDailySummary(dateString: String): Flow<DailySummary> {
        return tripDao.getTripsForDate(dateString).map { trips ->
            var income = 0.0
            var fuelCost = 0.0
            var netProfit = 0.0
            var distance = 0.0
            var totalFuelUsed = 0.0
            var idleMinutes = 0

            trips.forEach { trip ->
                income += (trip.fare + trip.tip)
                fuelCost += trip.fuelCost
                netProfit += trip.netProfit
                distance += trip.distanceKm
                totalFuelUsed += trip.totalFuelUsedLiters
                idleMinutes += trip.estimatedIdleMinutes
            }

            val avgEffectiveKmL = if (totalFuelUsed > 0.0) distance / totalFuelUsed else 0.0

            DailySummary(
                dateString = dateString,
                totalIncome = income,
                totalFuelCost = fuelCost,
                totalNetProfit = netProfit,
                totalDistanceKm = distance,
                totalTripsCount = trips.size,
                averageEffectiveKmL = avgEffectiveKmL,
                totalIdleMinutes = idleMinutes
            )
        }
    }
}
