package com.grabdriver.driverprofit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_records")
data class TripRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String,              // วันที่ "YYYY-MM-DD"
    
    // ข้อมูลที่คนขับกรอกต่อรอบ
    val fare: Double,                   // ค่ารอบ (บาท)
    val tip: Double = 0.0,              // ทิป (บาท)
    val distanceKm: Double,             // ระยะทาง (กม.)
    val dashKmL: Double,                // อัตราสิ้นเปลืองจากหน้าปัดของรอบนี้
    val durationMinutes: Int,           // เวลารวมของรอบนี้ (นาที)
    val fuelPricePerLiter: Double,      // ราคาน้ำมัน ณ วันที่บันทึก
    
    // ข้อมูลที่คำนวณและประมวลผล
    val estimatedIdleMinutes: Int,      // เวลาจอดนิ่ง/รถติดที่ประเมินได้ (นาที)
    val effectiveKmL: Double,           // อัตราสิ้นเปลืองแท้จริงหลังรวม Idling
    val totalFuelUsedLiters: Double,    // ปริมาณน้ำมันที่ใช้จริงทั้งหมด (ลิตร)
    val fuelCost: Double,               // ต้นทุนน้ำมันของรอบนี้ (บาท)
    val netProfit: Double,              // กำไรสุทธิของรอบนี้ (บาท)
    val vehicleId: Int                  // รหัสรุ่นรถที่ใช้
)
