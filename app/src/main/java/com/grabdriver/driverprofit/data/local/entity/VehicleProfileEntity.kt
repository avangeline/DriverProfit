package com.grabdriver.driverprofit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_profiles")
data class VehicleProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,                  // เช่น "Honda Wave 110i", "Honda PCX 160", "Toyota Yaris"
    val vehicleType: String,           // "MOTORCYCLE" หรือ "CAR"
    val defaultKmL: Double,            // อัตราสิ้นเปลืองมาตรฐานจากหน้าปัด เช่น 52.0 หรือ 18.0
    val idleLiterPerHour: Double,      // อัตรากินน้ำมันตอนจอดเดินเบา (ลิตร/ชม.) เช่น มอไซค์ 0.15, รถยนต์ 0.8
    val isSelected: Boolean = false
)
