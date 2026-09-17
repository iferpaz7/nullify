package com.nullify.utils

/**
 * Provenance of a whitelisted number. Lets contact sync clean up stale
 * system contacts without ever touching manual or seed entries.
 */
object ContactSource {
    const val SYSTEM = "system"
    const val MANUAL = "manual"
    const val SEED = "seed"
}
