package com.nullify.utils

object EcuadorPhoneUtils {

    private val EMERGENCY_SHORT_CODES = setOf(
        "911", "102", "115", "101", "131", "171"
    )

    fun isEmergencyNumber(rawNumber: String): Boolean {
        val normalized = normalizeForDatabase(rawNumber)
        val clean = rawNumber.replace(Regex("[^0-9]"), "")
        return EMERGENCY_SHORT_CODES.contains(normalized) || EMERGENCY_SHORT_CODES.contains(clean)
    }

    fun normalizeForDatabase(rawNumber: String): String {
        var clean = rawNumber.replace(Regex("[^0-9]"), "")
        if (clean.startsWith("593")) {
            clean = clean.substring(3)
        }
        if (clean.startsWith("0")) {
            clean = clean.substring(1)
        }
        return clean
    }
}
