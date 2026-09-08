package com.nullify.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CallScreeningEvaluatorTest {

    @Test
    fun testIsUnknownNumberString() {
        assertTrue(CallScreeningEvaluator.isUnknownNumberString(""))
        assertTrue(CallScreeningEvaluator.isUnknownNumberString("   "))
        assertTrue(CallScreeningEvaluator.isUnknownNumberString("UNKNOWN"))
        assertTrue(CallScreeningEvaluator.isUnknownNumberString("unknown"))
        assertTrue(CallScreeningEvaluator.isUnknownNumberString("PRIVATE"))
        assertTrue(CallScreeningEvaluator.isUnknownNumberString("RESTRICTED"))
        assertTrue(CallScreeningEvaluator.isUnknownNumberString("ANONYMOUS"))
        assertTrue(CallScreeningEvaluator.isUnknownNumberString("-1"))
        assertTrue(CallScreeningEvaluator.isUnknownNumberString("0"))
    }

    @Test
    fun testEvaluateFastPath_OutgoingCallsAllowed() {
        val result = CallScreeningEvaluator.evaluateFastPath(
            rawNumber = "0991234567",
            isIncoming = false,
            presentationAllowed = true,
        )
        assertEquals(CallScreeningEvaluator.ScreeningResult.ALLOW, result)
    }

    @Test
    fun testEvaluateFastPath_VerificationFailedBlocked() {
        val result = CallScreeningEvaluator.evaluateFastPath(
            rawNumber = "0991234567",
            isIncoming = true,
            presentationAllowed = true,
            verificationFailed = true,
        )
        assertEquals(CallScreeningEvaluator.ScreeningResult.BLOCK, result)
    }

    @Test
    fun testEvaluateFastPath_RestrictedPresentationBlocked() {
        val result = CallScreeningEvaluator.evaluateFastPath(
            rawNumber = "0991234567",
            isIncoming = true,
            presentationAllowed = false,
        )
        assertEquals(CallScreeningEvaluator.ScreeningResult.BLOCK, result)
    }

    @Test
    fun testEvaluateFastPath_UnknownStringBlocked() {
        val result = CallScreeningEvaluator.evaluateFastPath(
            rawNumber = "PRIVATE",
            isIncoming = true,
            presentationAllowed = true,
        )
        assertEquals(CallScreeningEvaluator.ScreeningResult.BLOCK, result)
    }

    @Test
    fun testEvaluateFastPath_EmergencyNumberAllowed() {
        val result = CallScreeningEvaluator.evaluateFastPath(
            rawNumber = "911",
            isIncoming = true,
            presentationAllowed = true,
        )
        assertEquals(CallScreeningEvaluator.ScreeningResult.ALLOW, result)
    }

    @Test
    fun testEvaluateFastPath_ValidNumberChecksDatabase() {
        val result = CallScreeningEvaluator.evaluateFastPath(
            rawNumber = "0991234567",
            isIncoming = true,
            presentationAllowed = true,
        )
        assertEquals(CallScreeningEvaluator.ScreeningResult.CHECK_DATABASE, result)
    }
}
