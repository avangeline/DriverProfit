package com.grabdriver.driverprofit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_fuel_settings")
data class DailyFuelSettingEntity(
    @PrimaryKey
    val dateString: String,             // รูปแบบ "YYYY-MM-DD" เช่น "2026-10-03"
    val fuelPricePerLiter: Double,      // ราคาน้ำมันของวันนั้น เช่น 38.50
    val fuelType: String = "Gasohol 95",
    val updatedAt: Long = System.currentTimeMillis()
)
