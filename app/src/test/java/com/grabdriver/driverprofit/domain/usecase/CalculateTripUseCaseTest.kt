package com.grabdriver.driverprofit.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateTripUseCaseTest {

    private val calculateTripUseCase = CalculateTripUseCase()

    @Test
    fun testDeadheadTripWithZeroFare() {
        // วิ่งตีรถเปล่า: ค่ารอบ 0 บาท ระยะทาง 10 กม.
        val result = calculateTripUseCase(
            fare = 0.0,
            tip = 0.0,
            distanceKm = 10.0,
            dashKmL = 50.0,
            durationMinutes = 15,
            fuelPricePerLiter = 40.0,
            vehicleType = "MOTORCYCLE",
            idleLiterPerHour = 0.15
        )

        assertEquals(0.0, result.totalIncome, 0.001)
        assertTrue("Fuel cost should be positive", result.totalFuelCost > 0.0)
        assertTrue("Net profit should be negative (fuel expense only)", result.netProfit < 0.0)
        assertEquals(-result.totalFuelCost, result.netProfit, 0.001)
    }

    @Test
    fun testNormalTripWithProfit() {
        val result = calculateTripUseCase(
            fare = 100.0,
            tip = 20.0,
            distanceKm = 5.0,
            dashKmL = 50.0,
            durationMinutes = 10,
            fuelPricePerLiter = 40.0,
            vehicleType = "MOTORCYCLE",
            idleLiterPerHour = 0.15
        )

        assertEquals(120.0, result.totalIncome, 0.001)
        assertTrue(result.netProfit > 100.0)
    }

    @Test
    fun testIdleCalculationDisabled() {
        // วิ่ง 5 กม. หน่วงเวลา 60 นาที (ปกติควรติดไฟแดงมหาศาล)
        // เมื่อปิด idle calculation ค่าน้ำมันต้องคำนวณจากระยะทาง/หน้าปัดเท่านั้น
        val result = calculateTripUseCase(
            fare = 100.0,
            tip = 0.0,
            distanceKm = 5.0,
            dashKmL = 50.0,
            durationMinutes = 60,
            fuelPricePerLiter = 40.0,
            vehicleType = "MOTORCYCLE",
            idleLiterPerHour = 0.15,
            isIdleCalculationEnabled = false
        )

        // 5 km / 50 km/L = 0.1 Liter
        assertEquals(0.1, result.totalFuelLiters, 0.001)
        assertEquals(0, result.estimatedIdleMinutes)
        assertEquals(0.0, result.idleFuelLiters, 0.001)
        assertEquals(50.0, result.effectiveKmL, 0.001)
        assertEquals(4.0, result.totalFuelCost, 0.001) // 0.1 L * 40 baht
        assertEquals(96.0, result.netProfit, 0.001)
    }
}
