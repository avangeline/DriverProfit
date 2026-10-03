package com.grabdriver.driverprofit.data.local.dao

import androidx.room.*
import com.grabdriver.driverprofit.data.local.entity.DailyFuelSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyFuelDao {
    @Query("SELECT * FROM daily_fuel_settings WHERE dateString = :dateString LIMIT 1")
    fun getFuelSettingForDate(dateString: String): Flow<DailyFuelSettingEntity?>

    @Query("SELECT * FROM daily_fuel_settings WHERE dateString = :dateString LIMIT 1")
    suspend fun getFuelSettingForDateDirect(dateString: String): DailyFuelSettingEntity?

    @Query("SELECT * FROM daily_fuel_settings ORDER BY dateString DESC LIMIT 1")
    suspend fun getLatestFuelSetting(): DailyFuelSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFuelSetting(setting: DailyFuelSettingEntity)
}
