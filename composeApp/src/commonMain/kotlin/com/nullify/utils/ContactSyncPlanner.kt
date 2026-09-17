package com.nullify.utils

import com.nullify.data.AllowedContact

data class SyncPlan(
    /** SYSTEM numbers present in DB but gone from the phone's contacts. */
    val staleSystemNumbers: List<String>,
    /** Numbers that must survive the sync (manual + seed + fresh system). */
    val numbersToPreserve: Set<String>,
)

/**
 * Pure sync planner: stale SYSTEM contacts are deleted, MANUAL and SEED
 * entries are never touched.
 */
object ContactSyncPlanner {

    fun planSync(
        existing: List<AllowedContact>,
        freshSystemNumbers: Set<String>,
    ): SyncPlan {
        val stale = existing
            .filter { it.source == ContactSource.SYSTEM && it.normalizedNumber !in freshSystemNumbers }
            .map { it.normalizedNumber }
        val preserve = existing
            .filter { it.source != ContactSource.SYSTEM || it.normalizedNumber in freshSystemNumbers }
            .map { it.normalizedNumber }
            .toSet() + freshSystemNumbers
        return SyncPlan(
            staleSystemNumbers = stale,
            numbersToPreserve = preserve,
        )
    }
}
