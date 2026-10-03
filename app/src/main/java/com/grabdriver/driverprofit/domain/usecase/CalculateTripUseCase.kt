package com.grabdriver.driverprofit.domain.usecase

import com.grabdriver.driverprofit.domain.model.TripCalculationResult
import kotlin.math.max
import kotlin.math.roundToInt

class CalculateTripUseCase {
    operator fun invoke(
        fare: Double,
        tip: Double,
        distanceKm: Double,
        dashKmL: Double,
        durationMinutes: Int,
        fuelPricePerLiter: Double,
        vehicleType: String = "MOTORCYCLE",
        idleLiterPerHour: Double = 0.15
    ): TripCalculationResult {
        // 1. น้ำมันที่ใช้ขณะรถเคลื่อนที่ตามหน้าปัด
        val movingFuelLiters = if (dashKmL > 0.0) distanceKm / dashKmL else 0.0

        // 2. ประมาณการเวลาที่ใช้เดินทางปกติ (ความเร็วปกติในเมือง มอไซค์ ~35 km/h, รถยนต์ ~30 km/h)
        val normalMovingSpeedKmH = if (vehicleType == "MOTORCYCLE") 35.0 else 30.0
        val expectedMovingMinutes = if (normalMovingSpeedKmH > 0.0) {
            (distanceKm / normalMovingSpeedKmH) * 60.0
        } else 0.0

        // เวลาที่จอดแช่/รถติด/รอผู้โดยสาร (เวลารวม - เวลาที่ควรใช้วิ่ง)
        val estimatedIdleMinutes = max(0, durationMinutes - expectedMovingMinutes.roundToInt())

        // 3. ปริมาณน้ำมันที่เสียไปตอนจอดเดินเบา (Idling)
        val idleFuelLiters = (estimatedIdleMinutes.toDouble() / 60.0) * idleLiterPerHour

        // 4. ปริมาณน้ำมันจริงรวมทั้งรอบ
        val totalFuelLiters = movingFuelLiters + idleFuelLiters

        // 5. อัตราสิ้นเปลืองที่แท้จริง (Effective km/L)
        val effectiveKmL = if (totalFuelLiters > 0.0) {
            distanceKm / totalFuelLiters
        } else dashKmL

        // 6. ต้นทุนน้ำมันและกำไรสุทธิ
        val totalFuelCost = totalFuelLiters * fuelPricePerLiter
        val totalIncome = fare + tip
        val netProfit = totalIncome - totalFuelCost

        return TripCalculationResult(
            movingFuelLiters = movingFuelLiters,
            estimatedIdleMinutes = estimatedIdleMinutes,
            idleFuelLiters = idleFuelLiters,
            totalFuelLiters = totalFuelLiters,
            effectiveKmL = effectiveKmL,
            totalFuelCost = totalFuelCost,
            totalIncome = totalIncome,
            netProfit = netProfit
        )
    }
}
