package com.grabdriver.driverprofit.domain.model

data class DailySummary(
    val dateString: String,
    val totalIncome: Double = 0.0,
    val totalFuelCost: Double = 0.0,
    val totalNetProfit: Double = 0.0,
    val totalDistanceKm: Double = 0.0,
    val totalTripsCount: Int = 0,
    val averageEffectiveKmL: Double = 0.0,
    val totalIdleMinutes: Int = 0
)
