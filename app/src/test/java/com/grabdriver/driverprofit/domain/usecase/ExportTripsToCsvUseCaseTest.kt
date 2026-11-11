package com.grabdriver.driverprofit.domain.usecase

import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportTripsToCsvUseCaseTest {

    private val exportUseCase = ExportTripsToCsvUseCase()

    @Test
    fun testExportWithDeadheadTrip() {
        val trips = listOf(
            TripRecordEntity(
                id = 1,
                timestamp = 1000L,
                dateString = "2026-11-11",
                fare = 100.0,
                tip = 0.0,
                distanceKm = 5.0,
                dashKmL = 50.0,
                durationMinutes = 10,
                fuelPricePerLiter = 38.5,
                estimatedIdleMinutes = 0,
                effectiveKmL = 50.0,
                totalFuelUsedLiters = 0.1,
                fuelCost = 3.85,
                netProfit = 96.15,
                vehicleId = 1
            ),
            TripRecordEntity(
                id = 2,
                timestamp = 2000L,
                dateString = "2026-11-11",
                fare = 0.0, // ตีรถเปล่า
                tip = 0.0,
                distanceKm = 10.0,
                dashKmL = 50.0,
                durationMinutes = 15,
                fuelPricePerLiter = 38.5,
                estimatedIdleMinutes = 0,
                effectiveKmL = 50.0,
                totalFuelUsedLiters = 0.2,
                fuelCost = 7.70,
                netProfit = -7.70,
                vehicleId = 1
            ),
            TripRecordEntity(
                id = 3,
                timestamp = 3000L,
                dateString = "2026-11-11",
                fare = 80.0,
                tip = 10.0,
                distanceKm = 4.0,
                dashKmL = 50.0,
                durationMinutes = 8,
                fuelPricePerLiter = 38.5,
                estimatedIdleMinutes = 0,
                effectiveKmL = 50.0,
                totalFuelUsedLiters = 0.08,
                fuelCost = 3.08,
                netProfit = 86.92,
                vehicleId = 1
            )
        )

        val csv = exportUseCase(trips)
        // Check that trip 1 is labeled 1
        assertTrue(csv.contains(",1,100.00,"))
        // Check that deadhead trip is labeled ตีรถเปล่า
        assertTrue(csv.contains(",ตีรถเปล่า,0.00,"))
        // Check that trip 3 is labeled 2 (excluding deadhead from count)
        assertTrue(csv.contains(",2,80.00,"))
    }
}
