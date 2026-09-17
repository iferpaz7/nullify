package com.nullify.utils

import com.nullify.data.AllowedContact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactSyncPlannerTest {

    @Test
    fun testPlanSync_removesStaleSystemContacts() {
        val existing = listOf(
            AllowedContact("991234567", "Juan", ContactSource.SYSTEM),
            AllowedContact("999999999", "Ex Contacto", ContactSource.SYSTEM),
            AllowedContact("23965006", "Banco Manual", ContactSource.MANUAL),
            AllowedContact("700600600", "[SISTEMA] BGR", ContactSource.SEED),
        )
        val freshSystem = setOf("991234567")

        val plan = ContactSyncPlanner.planSync(existing, freshSystem)

        assertEquals(listOf("999999999"), plan.staleSystemNumbers)
        // Manual y seed jamás se tocan
        assertTrue(plan.numbersToPreserve.contains("23965006"))
        assertTrue(plan.numbersToPreserve.contains("700600600"))
    }

    @Test
    fun testPlanSync_noFreshSystem_keepsManualAndSeed() {
        val existing = listOf(
            AllowedContact("23965006", "Banco Manual", ContactSource.MANUAL),
            AllowedContact("700600600", "[SISTEMA] BGR", ContactSource.SEED),
        )

        val plan = ContactSyncPlanner.planSync(existing, emptySet())

        assertTrue(plan.staleSystemNumbers.isEmpty())
        assertEquals(2, plan.numbersToPreserve.size)
    }
}
