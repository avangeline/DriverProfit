package com.grabdriver.driverprofit.data.repository

import com.grabdriver.driverprofit.data.local.entity.DailyFuelSettingEntity
import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import com.grabdriver.driverprofit.data.local.entity.VehicleProfileEntity
import com.grabdriver.driverprofit.domain.model.DailySummary
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun getTripsForDate(dateString: String): Flow<List<TripRecordEntity>>
    suspend fun getTripsForDateDirect(dateString: String): List<TripRecordEntity>
    fun getAllTrips(): Flow<List<TripRecordEntity>>
    suspend fun getAllTripsDirect(): List<TripRecordEntity>
    suspend fun insertTrip(trip: TripRecordEntity): Long
    suspend fun updateTrip(trip: TripRecordEntity)
    suspend fun deleteTrip(trip: TripRecordEntity)
    suspend fun deleteTripById(tripId: Long)
    suspend fun updateTripTip(tripId: Long, newTip: Double)
    suspend fun getLatestTrip(): TripRecordEntity?

    fun getSelectedVehicle(): Flow<VehicleProfileEntity?>
    suspend fun getSelectedVehicleDirect(): VehicleProfileEntity?
    fun getAllVehicles(): Flow<List<VehicleProfileEntity>>
    suspend fun getAllVehiclesDirect(): List<VehicleProfileEntity>
    suspend fun setSelectedVehicle(vehicleId: Int)
    suspend fun insertVehicle(vehicle: VehicleProfileEntity): Long
    suspend fun updateVehicle(vehicle: VehicleProfileEntity)
    suspend fun deleteVehicle(vehicle: VehicleProfileEntity)

    fun getFuelSettingForDate(dateString: String): Flow<DailyFuelSettingEntity?>
    suspend fun getFuelSettingForDateDirect(dateString: String): DailyFuelSettingEntity?
    suspend fun getLatestFuelSetting(): DailyFuelSettingEntity?
    suspend fun updateFuelPriceForDate(dateString: String, price: Double)

    fun getDailySummary(dateString: String): Flow<DailySummary>
}
