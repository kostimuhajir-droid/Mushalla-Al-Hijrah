package com.example

import com.example.data.local.SecurityHelper
import com.example.ui.components.formatRupiah
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testIntegrityHashConsistency() {
        val hash1 = SecurityHelper.generateIntegrityHash("MASUK", "Infaq Sholat Jumat", "3500000")
        val hash2 = SecurityHelper.generateIntegrityHash("MASUK", "Infaq Sholat Jumat", "3500000")
        val hashDifferent = SecurityHelper.generateIntegrityHash("KELUAR", "Listrik", "500000")

        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hashDifferent)
        assertTrue(hash1.length >= 8)
    }

    @Test
    fun testRupiahFormatting() {
        val formatted = formatRupiah(15000000L)
        assertTrue(formatted.contains("15.000.000"))
        assertTrue(formatted.startsWith("Rp"))
    }

    @Test
    fun testWakafCalculation() {
        val target = 75000000L
        val collected = 40000000L
        val percentage = (collected.toDouble() / target.toDouble() * 100).toInt()
        val remaining = (target - collected).coerceAtLeast(0L)

        assertEquals(53, percentage)
        assertEquals(35000000L, remaining)
    }
}
