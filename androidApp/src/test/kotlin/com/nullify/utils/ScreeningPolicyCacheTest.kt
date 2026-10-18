package com.nullify.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreeningPolicyCacheTest {

    @Test
    fun testFreshCache_isColdAndFailClosed() {
        val cache = ScreeningPolicyCache()

        // Before Room emits, the service must assume protection is ON
        // (fail-closed firewall), and must know the value is not yet warm
        // so it can fall back to the database.
        assertFalse(cache.isWarm())
        assertTrue(cache.isScreeningEnabled())
    }

    @Test
    fun testUpdate_disablesScreeningAndMarksWarm() {
        val cache = ScreeningPolicyCache()

        cache.update(screeningEnabled = false)

        assertTrue(cache.isWarm())
        assertFalse(cache.isScreeningEnabled())
    }

    @Test
    fun testUpdate_reEnablesScreening() {
        val cache = ScreeningPolicyCache()

        cache.update(screeningEnabled = false)
        cache.update(screeningEnabled = true)

        assertTrue(cache.isScreeningEnabled())
    }
}
