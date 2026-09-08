package com.nullify.utils

object CallScreeningEvaluator {

    private val KNOWN_UNKNOWNS = setOf(
        "UNKNOWN", "PRIVATE", "RESTRICTED", "ANONYMOUS", "PWW",
        "-1", "-2", "-3", "0", "NULL"
    )

    fun isUnknownNumberString(rawNumber: String): Boolean {
        if (rawNumber.isBlank()) return true
        return KNOWN_UNKNOWNS.contains(rawNumber.trim().uppercase())
    }

    enum class ScreeningResult {
        ALLOW,
        BLOCK,
        CHECK_DATABASE
    }

    fun evaluateFastPath(
        rawNumber: String,
        isIncoming: Boolean,
        presentationAllowed: Boolean,
        verificationFailed: Boolean = false,
    ): ScreeningResult {
        if (!isIncoming) return ScreeningResult.ALLOW
        if (verificationFailed) return ScreeningResult.BLOCK
        if (!presentationAllowed) return ScreeningResult.BLOCK
        if (rawNumber.isBlank() || isUnknownNumberString(rawNumber)) return ScreeningResult.BLOCK
        if (EcuadorPhoneUtils.isEmergencyNumber(rawNumber)) return ScreeningResult.ALLOW
        if (EcuadorPhoneUtils.normalizeForDatabase(rawNumber).isBlank()) return ScreeningResult.BLOCK

        return ScreeningResult.CHECK_DATABASE
    }
}
