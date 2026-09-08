package com.nullify.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EcuadorPhoneUtilsTest {

    @Test
    fun testIsEmergencyNumber() {
        assertTrue(EcuadorPhoneUtils.isEmergencyNumber("911"))
        assertTrue(EcuadorPhoneUtils.isEmergencyNumber("+593 911"))
        assertTrue(EcuadorPhoneUtils.isEmergencyNumber("102"))
        assertTrue(EcuadorPhoneUtils.isEmergencyNumber("115"))
        assertTrue(EcuadorPhoneUtils.isEmergencyNumber("101"))
        assertTrue(EcuadorPhoneUtils.isEmergencyNumber("131"))
        assertTrue(EcuadorPhoneUtils.isEmergencyNumber("171"))

        assertFalse(EcuadorPhoneUtils.isEmergencyNumber("0991234567"))
        assertFalse(EcuadorPhoneUtils.isEmergencyNumber("023965006"))
    }

    @Test
    fun testNormalizeForDatabase_landlinesAndMobiles() {
        // Mobile with 0
        assertEquals("991234567", EcuadorPhoneUtils.normalizeForDatabase("0991234567"))

        // Mobile with +593
        assertEquals("991234567", EcuadorPhoneUtils.normalizeForDatabase("+593991234567"))
        assertEquals("991234567", EcuadorPhoneUtils.normalizeForDatabase("+593 99 123 4567"))

        // Mobile with +593 and leading 0
        assertEquals("991234567", EcuadorPhoneUtils.normalizeForDatabase("+5930991234567"))

        // Landline Quito
        assertEquals("23965006", EcuadorPhoneUtils.normalizeForDatabase("023965006"))
        assertEquals("23965006", EcuadorPhoneUtils.normalizeForDatabase("+59323965006"))

        // Already normalized
        assertEquals("991234567", EcuadorPhoneUtils.normalizeForDatabase("991234567"))

        // Unknown / Non-numeric
        assertEquals("", EcuadorPhoneUtils.normalizeForDatabase("UNKNOWN"))
        assertEquals("", EcuadorPhoneUtils.normalizeForDatabase("PRIVATE"))
        assertEquals("", EcuadorPhoneUtils.normalizeForDatabase(""))
    }
}
