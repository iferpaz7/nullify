package com.nullify.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AllowedNumberCacheTest {

    @Test
    fun testEmptyCache_blocksEverything() {
        val cache = AllowedNumberCache()
        assertFalse(cache.isAllowed("991234567"))
        assertEquals(0, cache.size())
    }

    @Test
    fun testReplaceAll_allowsCachedNumbers() {
        val cache = AllowedNumberCache()
        cache.replaceAll(listOf("991234567", "23965006"))

        assertTrue(cache.isAllowed("991234567"))
        assertTrue(cache.isAllowed("23965006"))
        assertFalse(cache.isAllowed("0999999999"))
        assertEquals(2, cache.size())
    }

    @Test
    fun testReplaceAll_removesStaleNumbers() {
        val cache = AllowedNumberCache()
        cache.replaceAll(listOf("991234567", "23965006"))
        cache.replaceAll(listOf("991234567"))

        assertTrue(cache.isAllowed("991234567"))
        assertFalse(cache.isAllowed("23965006"))
        assertEquals(1, cache.size())
    }
}
