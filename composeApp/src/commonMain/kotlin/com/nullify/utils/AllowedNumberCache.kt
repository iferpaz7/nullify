package com.nullify.utils

/**
 * In-memory snapshot of allowed numbers for the call-screening hot path.
 *
 * CallScreeningService must answer within 5 seconds or the OS ignores the
 * response (fail-open: phone rings). Hitting Room on every call risks blowing
 * that budget on cold start, so the service reads this cache synchronously
 * and only falls back to the database when the cache is still empty.
 */
class AllowedNumberCache {

    @Volatile
    private var allowed: Set<String> = emptySet()

    fun replaceAll(numbers: Collection<String>) {
        allowed = numbers.toSet()
    }

    fun isAllowed(normalizedNumber: String): Boolean =
        allowed.contains(normalizedNumber)

    fun isEmpty(): Boolean = allowed.isEmpty()

    fun size(): Int = allowed.size
}
