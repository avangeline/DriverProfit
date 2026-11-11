package com.grabdriver.driverprofit.domain

import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import com.grabdriver.driverprofit.domain.model.TripFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripFilterTest {

    private fun createTrip(id: Long, fare: Double, timestamp: Long): TripRecordEntity {
        return TripRecordEntity(
            id = id,
            timestamp = timestamp,
            dateString = "2026-11-11",
            fare = fare,
            tip = 0.0,
            distanceKm = 5.0,
            dashKmL = 40.0,
            durationMinutes = 15,
            fuelPricePerLiter = 38.5,
            estimatedIdleMinutes = 2,
            effectiveKmL = 35.0,
            totalFuelUsedLiters = 0.15,
            fuelCost = 5.77,
            netProfit = fare - 5.77,
            vehicleId = 1
        )
    }

    @Test
    fun `default filter should be PAID_ONLY`() {
        assertEquals(TripFilter.PAID_ONLY, TripFilter.valueOf("PAID_ONLY"))
    }

    @Test
    fun `TripFilter display names are correct`() {
        assertEquals("เฉพาะรอบวิ่งที่ได้เงิน", TripFilter.PAID_ONLY.displayName)
        assertEquals("ทั้งหมด", TripFilter.ALL.displayName)
        assertEquals("รอบตีรถเปล่า", TripFilter.DEADHEAD.displayName)
    }

    @Test
    fun `filter trips with PAID_ONLY returns only trips with fare greater than 0`() {
        val trips = listOf(
            createTrip(id = 1, fare = 65.0, timestamp = 1000),
            createTrip(id = 2, fare = 0.0, timestamp = 2000),
            createTrip(id = 3, fare = 120.0, timestamp = 3000),
            createTrip(id = 4, fare = 0.0, timestamp = 4000)
        )

        val filtered = trips.filter {
            when (TripFilter.PAID_ONLY) {
                TripFilter.PAID_ONLY -> it.fare > 0.0
                TripFilter.ALL -> true
                TripFilter.DEADHEAD -> it.fare == 0.0
            }
        }

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.fare > 0.0 })
        assertEquals(listOf(1L, 3L), filtered.map { it.id })
    }

    @Test
    fun `filter trips with DEADHEAD returns only trips with fare equal to 0`() {
        val trips = listOf(
            createTrip(id = 1, fare = 65.0, timestamp = 1000),
            createTrip(id = 2, fare = 0.0, timestamp = 2000),
            createTrip(id = 3, fare = 120.0, timestamp = 3000),
            createTrip(id = 4, fare = 0.0, timestamp = 4000)
        )

        val filtered = trips.filter {
            when (TripFilter.DEADHEAD) {
                TripFilter.PAID_ONLY -> it.fare > 0.0
                TripFilter.ALL -> true
                TripFilter.DEADHEAD -> it.fare == 0.0
            }
        }

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.fare == 0.0 })
        assertEquals(listOf(2L, 4L), filtered.map { it.id })
    }

    @Test
    fun `filter trips with ALL returns all trips`() {
        val trips = listOf(
            createTrip(id = 1, fare = 65.0, timestamp = 1000),
            createTrip(id = 2, fare = 0.0, timestamp = 2000),
            createTrip(id = 3, fare = 120.0, timestamp = 3000)
        )

        val filtered = trips.filter {
            when (TripFilter.ALL) {
                TripFilter.PAID_ONLY -> it.fare > 0.0
                TripFilter.ALL -> true
                TripFilter.DEADHEAD -> it.fare == 0.0
            }
        }

        assertEquals(3, filtered.size)
        assertEquals(listOf(1L, 2L, 3L), filtered.map { it.id })
    }

    @Test
    fun `trip numbering correctly ignores deadhead trips and is stable`() {
        val trips = listOf(
            createTrip(id = 1, fare = 50.0, timestamp = 1000),
            createTrip(id = 2, fare = 0.0, timestamp = 2000),
            createTrip(id = 3, fare = 70.0, timestamp = 3000),
            createTrip(id = 4, fare = 0.0, timestamp = 4000),
            createTrip(id = 5, fare = 90.0, timestamp = 5000)
        )

        fun calculatePaidTripNumber(trip: TripRecordEntity): Int {
            return if (trip.fare > 0.0) {
                trips.filter { it.fare > 0.0 && (it.timestamp < trip.timestamp || (it.timestamp == trip.timestamp && it.id <= trip.id)) }.size
            } else 0
        }

        assertEquals(1, calculatePaidTripNumber(trips[0]))
        assertEquals(0, calculatePaidTripNumber(trips[1]))
        assertEquals(2, calculatePaidTripNumber(trips[2]))
        assertEquals(0, calculatePaidTripNumber(trips[3]))
        assertEquals(3, calculatePaidTripNumber(trips[4]))
    }
}
