package com.nullify.utils

object EcuadorPhoneUtils {

    private val EMERGENCY_SHORT_CODES = setOf(
        "911", "102", "115", "101", "131", "171"
    )

    fun isEmergencyNumber(rawNumber: String): Boolean {
        val clean = rawNumber.replace(Regex("[^0-9]"), "")
        return EMERGENCY_SHORT_CODES.contains(clean)
    }

    fun normalizeForDatabase(rawNumber: String): String {
        val clean = rawNumber.replace(Regex("[^0-9]"), "")
        return when {
            clean.startsWith("593") -> clean.substring(3)
            clean.startsWith("0") -> clean.substring(1)
            else -> clean
        }
    }
}
