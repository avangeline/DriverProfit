package com.grabdriver.driverprofit.data.local.dao

import androidx.room.*
import com.grabdriver.driverprofit.data.local.entity.VehicleProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicle_profiles ORDER BY id ASC")
    fun getAllVehicles(): Flow<List<VehicleProfileEntity>>

    @Query("SELECT * FROM vehicle_profiles ORDER BY id ASC")
    suspend fun getAllVehiclesDirect(): List<VehicleProfileEntity>

    @Query("SELECT * FROM vehicle_profiles WHERE isSelected = 1 LIMIT 1")
    fun getSelectedVehicle(): Flow<VehicleProfileEntity?>

    @Query("SELECT * FROM vehicle_profiles WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedVehicleDirect(): VehicleProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleProfileEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleProfileEntity)

    @Query("UPDATE vehicle_profiles SET isSelected = 0")
    suspend fun clearSelectedVehicles()

    @Query("UPDATE vehicle_profiles SET isSelected = 1 WHERE id = :vehicleId")
    suspend fun setSelectedVehicle(vehicleId: Int)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleProfileEntity)
}
