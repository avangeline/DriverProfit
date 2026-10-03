package com.grabdriver.driverprofit.util

import java.time.LocalDate
import java.time.chrono.ThaiBuddhistDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object DateUtils {

    private val thaiLocale = Locale("th", "TH")
    private val dateIsoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val thaiShortFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", thaiLocale)
    private val thaiFullFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", thaiLocale)
    private val thaiWithDayFormatter = DateTimeFormatter.ofPattern("EEEEที่ d MMM yyyy", thaiLocale)

    fun getTodayDateString(): String {
        return LocalDate.now().format(dateIsoFormatter)
    }

    fun getYesterdayDateString(): String {
        return LocalDate.now().minusDays(1).format(dateIsoFormatter)
    }

    fun addDays(dateString: String, days: Int): String {
        return try {
            val localDate = LocalDate.parse(dateString, dateIsoFormatter)
            localDate.plusDays(days.toLong()).format(dateIsoFormatter)
        } catch (e: Exception) {
            dateString
        }
    }

    fun isToday(dateString: String): Boolean {
        return dateString == getTodayDateString()
    }

    fun isPastDate(dateString: String): Boolean {
        return try {
            val localDate = LocalDate.parse(dateString, dateIsoFormatter)
            val today = LocalDate.now()
            localDate.isBefore(today)
        } catch (e: Exception) {
            false
        }
    }

    fun formatThaiDateShort(dateString: String): String {
        return try {
            val localDate = LocalDate.parse(dateString, dateIsoFormatter)
            val thaiDate = ThaiBuddhistDate.from(localDate)
            thaiDate.format(thaiShortFormatter)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatThaiDateFull(dateString: String): String {
        return try {
            val localDate = LocalDate.parse(dateString, dateIsoFormatter)
            val thaiDate = ThaiBuddhistDate.from(localDate)
            thaiDate.format(thaiFullFormatter)
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatThaiDateWithDay(dateString: String): String {
        return try {
            val localDate = LocalDate.parse(dateString, dateIsoFormatter)
            val thaiDate = ThaiBuddhistDate.from(localDate)
            thaiDate.format(thaiWithDayFormatter)
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * สร้าง timestamp (epoch millis) ที่ตรงกับ dateString ("yyyy-MM-dd")
     * โดยถ้าเป็นวันที่ปัจจุบัน ให้ใช้เวลา ณ ตอนนี้
     * ถ้าเป็นวันอื่น ให้ใช้เวลาปัจจุบันของวันนั้นๆ
     */
    fun createTimestampForDate(dateString: String, baseMillis: Long = System.currentTimeMillis()): Long {
        return try {
            val calTarget = Calendar.getInstance().apply { timeInMillis = baseMillis }
            val parts = dateString.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val day = parts[2].toInt()
                calTarget.set(Calendar.YEAR, year)
                calTarget.set(Calendar.MONTH, month)
                calTarget.set(Calendar.DAY_OF_MONTH, day)
                calTarget.timeInMillis
            } else {
                baseMillis
            }
        } catch (e: Exception) {
            baseMillis
        }
    }
}
