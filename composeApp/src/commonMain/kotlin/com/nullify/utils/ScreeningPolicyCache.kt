package com.nullify.utils

/**
 * In-memory snapshot of the screening master switch for the call-screening
 * hot path.
 *
 * `CallScreeningService.onScreenCall` must answer within 5 seconds or the OS
 * ignores the response (fail-open: the call rings). Reading Room on every
 * call risks blowing that budget on cold start, so the service reads this
 * cache synchronously and only falls back to the database while it is still
 * cold.
 *
 * Fails closed: until a real value arrives, screening is considered enabled.
 */
class ScreeningPolicyCache {

    @Volatile
    private var enabled: Boolean = true

    @Volatile
    private var warm: Boolean = false

    fun update(screeningEnabled: Boolean) {
        enabled = screeningEnabled
        warm = true
    }

    fun isScreeningEnabled(): Boolean = enabled

    /** False until the first value has been mirrored from the database. */
    fun isWarm(): Boolean = warm
}
