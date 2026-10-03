package com.grabdriver.driverprofit.domain.model

data class TripCalculationResult(
    val movingFuelLiters: Double,       // น้ำมันที่ใช้ขณะรถวิ่ง (L)
    val estimatedIdleMinutes: Int,      // เวลาที่เสียไปกับการจอด/รถติด (นาที)
    val idleFuelLiters: Double,         // น้ำมันที่เสียไปตอนจอดเดินเบา (L)
    val totalFuelLiters: Double,        // น้ำมันรวมทั้งรอบ (L)
    val effectiveKmL: Double,           // อัตราสิ้นเปลืองแท้จริง (km/L)
    val totalFuelCost: Double,          // ค่าน้ำมันรวมของรอบนี้ (บาท)
    val totalIncome: Double,            // รายได้รวม (ค่ารอบ + ทิป)
    val netProfit: Double               // กำไรสุทธิ (บาท)
)
