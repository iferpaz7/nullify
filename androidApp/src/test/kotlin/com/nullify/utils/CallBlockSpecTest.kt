package com.nullify.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallBlockSpecTest {

    @Test
    fun testDefaultBlockSpec_disallowsAndRejectsWithoutSilence() {
        val spec = defaultBlockSpec()

        assertTrue(spec.disallowCall)
        assertTrue(spec.rejectCall)
        // setSilenceCall(true) junto a disallow=true es contradictorio por docs:
        // "Setting this to true only makes sense when the call has not been disallowed"
        assertFalse(spec.silenceCall)
        assertFalse(spec.skipCallLog)
        assertFalse(spec.skipNotification)
    }

    @Test
    fun testResolveDatabaseResult_allowedNumber_returnsAllow() {
        assertEquals(
            CallScreeningEvaluator.ScreeningResult.ALLOW,
            CallScreeningEvaluator.resolveDatabaseResult(isAllowed = true)
        )
    }

    @Test
    fun testResolveDatabaseResult_unknownNumber_returnsBlock() {
        assertEquals(
            CallScreeningEvaluator.ScreeningResult.BLOCK,
            CallScreeningEvaluator.resolveDatabaseResult(isAllowed = false)
        )
    }
}
