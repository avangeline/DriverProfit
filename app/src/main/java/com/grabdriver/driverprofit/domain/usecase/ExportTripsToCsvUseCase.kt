package com.grabdriver.driverprofit.domain.usecase

import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportTripsToCsvUseCase {
    operator fun invoke(trips: List<TripRecordEntity>): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sb = StringBuilder()

        // ใส่ UTF-8 BOM เพื่อให้โปรแกรม Microsoft Excel เปิดไฟล์ภาษาไทยได้ถูกต้อง ไม่เป็นภาษาต่างดาว
        sb.append('\uFEFF')

        // Header คอลัมน์
        sb.append("วันที่,เวลา,รอบที่,ค่ารอบ(บาท),ทิป(บาท),รายได้รวม(บาท),ระยะทาง(กม.),เวลาในรอบ(นาที),เวลารถติด(นาที),หน้าปัด(km/L),อัตราสิ้นเปลืองจริง(km/L),ราคาน้ำมัน(บาท/L),ค่าน้ำมัน(บาท),กำไรสุทธิ(บาท)\n")

        val sortedTrips = trips.sortedBy { it.timestamp }
        var paidCount = 0
        sortedTrips.forEach { trip ->
            val timeStr = timeFormat.format(Date(trip.timestamp))
            val totalIncome = trip.fare + trip.tip
            val tripLabel = if (trip.fare == 0.0) "ตีรถเปล่า" else {
                paidCount++
                paidCount.toString()
            }

            sb.append("${trip.dateString},")
            sb.append("$timeStr,")
            sb.append("$tripLabel,")
            sb.append(String.format(Locale.US, "%.2f,", trip.fare))
            sb.append(String.format(Locale.US, "%.2f,", trip.tip))
            sb.append(String.format(Locale.US, "%.2f,", totalIncome))
            sb.append(String.format(Locale.US, "%.2f,", trip.distanceKm))
            sb.append("${trip.durationMinutes},")
            sb.append("${trip.estimatedIdleMinutes},")
            sb.append(String.format(Locale.US, "%.1f,", trip.dashKmL))
            sb.append(String.format(Locale.US, "%.1f,", trip.effectiveKmL))
            sb.append(String.format(Locale.US, "%.2f,", trip.fuelPricePerLiter))
            sb.append(String.format(Locale.US, "%.2f,", trip.fuelCost))
            sb.append(String.format(Locale.US, "%.2f\n", trip.netProfit))
        }

        return sb.toString()
    }
}
