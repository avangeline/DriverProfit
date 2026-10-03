package com.grabdriver.driverprofit.util

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun testAddDays() {
        val date = "2026-10-03"
        assertEquals("2026-10-04", DateUtils.addDays(date, 1))
        assertEquals("2026-10-02", DateUtils.addDays(date, -1))
        assertEquals("2026-09-30", DateUtils.addDays("2026-10-01", -1))
    }

    @Test
    fun testIsTodayAndYesterday() {
        val today = DateUtils.getTodayDateString()
        val yesterday = DateUtils.getYesterdayDateString()
        assertTrue(DateUtils.isToday(today))
        assertFalse(DateUtils.isToday(yesterday))
        assertTrue(DateUtils.isPastDate(yesterday))
    }

    @Test
    fun testThaiDateFormatting() {
        val date = "2026-10-03"
        val formatted = DateUtils.formatThaiDateShort(date)
        // 2026 CE = 2569 BE
        assertTrue(formatted.contains("2569"))
        assertTrue(formatted.contains("ต.ค."))
    }
}
