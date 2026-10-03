package com.grabdriver.driverprofit.data.local.dao

import androidx.room.*
import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trip_records WHERE dateString = :dateString ORDER BY timestamp DESC")
    fun getTripsForDate(dateString: String): Flow<List<TripRecordEntity>>

    @Query("SELECT * FROM trip_records WHERE dateString = :dateString ORDER BY timestamp DESC")
    suspend fun getTripsForDateDirect(dateString: String): List<TripRecordEntity>

    @Query("SELECT * FROM trip_records ORDER BY timestamp DESC")
    fun getAllTrips(): Flow<List<TripRecordEntity>>

    @Query("SELECT * FROM trip_records ORDER BY timestamp DESC")
    suspend fun getAllTripsDirect(): List<TripRecordEntity>

    @Query("SELECT * FROM trip_records ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestTrip(): TripRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripRecordEntity): Long

    @Update
    suspend fun updateTrip(trip: TripRecordEntity)

    @Delete
    suspend fun deleteTrip(trip: TripRecordEntity)

    @Query("DELETE FROM trip_records WHERE id = :tripId")
    suspend fun deleteTripById(tripId: Long)

    @Query("UPDATE trip_records SET tip = :newTip, netProfit = (fare + :newTip) - fuelCost WHERE id = :tripId")
    suspend fun updateTripTip(tripId: Long, newTip: Double)
}
