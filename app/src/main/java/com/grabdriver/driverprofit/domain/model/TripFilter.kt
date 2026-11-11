package com.grabdriver.driverprofit.domain.model

enum class TripFilter(val displayName: String) {
    ALL("ทั้งหมด"),
    PAID_ONLY("รอบปกติ"),
    DEADHEAD("รอบเปล่า")
}
