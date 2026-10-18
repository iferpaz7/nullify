package com.nullify.service

import android.net.Uri
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.telecom.TelecomManager
import android.util.Log
import com.nullify.NullifyApp
import com.nullify.data.CallLogEntry
import com.nullify.utils.CallScreeningEvaluator
import com.nullify.utils.EcuadorPhoneUtils
import com.nullify.utils.defaultBlockSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class NullifyScreeningService : CallScreeningService() {

    private val logScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val startTime = System.currentTimeMillis()

        val isIncoming = callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        val handle: Uri? = callDetails.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""
        val presentationAllowed = callDetails.handlePresentation == TelecomManager.PRESENTATION_ALLOWED
        val verificationFailed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            callDetails.callerNumberVerificationStatus == Connection.VERIFICATION_STATUS_FAILED
        } else {
            false
        }

        val screeningEnabled = isScreeningEnabled()
        val fastPathResult = CallScreeningEvaluator.evaluateFastPath(
            rawNumber = rawNumber,
            isIncoming = isIncoming,
            presentationAllowed = presentationAllowed,
            verificationFailed = verificationFailed,
            screeningEnabled = screeningEnabled,
        )

        when (fastPathResult) {
            CallScreeningEvaluator.ScreeningResult.ALLOW -> {
                respondToCall(callDetails, CallResponse.Builder().build())
                when {
                    isIncoming && !screeningEnabled ->
                        logCall(
                            if (rawNumber.isBlank()) "DESCONOCIDO" else rawNumber,
                            "ALLOWED",
                            "protección desactivada",
                        )
                    isIncoming && EcuadorPhoneUtils.isEmergencyNumber(rawNumber) ->
                        logCall(rawNumber, "ALLOWED", "emergency number")
                }
                return
            }
            CallScreeningEvaluator.ScreeningResult.BLOCK -> {
                val reason = when {
                    verificationFailed -> "spoofed number / verification failed"
                    !presentationAllowed -> "private/restricted presentation"
                    rawNumber.isBlank() || CallScreeningEvaluator.isUnknownNumberString(rawNumber) -> "private/unknown number"
                    else -> "invalid/unknown number"
                }
                val elapsed = System.currentTimeMillis() - startTime
                Log.i("NullifyScreening", "Decision in ${elapsed}ms — blocked ($reason)")
                blockCall(callDetails, reason)
                logCall(if (rawNumber.isBlank()) "DESCONOCIDO" else rawNumber, "BLOCKED", reason)
                return
            }
            CallScreeningEvaluator.ScreeningResult.CHECK_DATABASE -> {
                // Continue to database check below
            }
        }

        val normalizedIncoming = EcuadorPhoneUtils.normalizeForDatabase(rawNumber)
        val isAllowed = isNumberAllowed(normalizedIncoming)
        val verdict = CallScreeningEvaluator.resolveDatabaseResult(isAllowed)

        val elapsed = System.currentTimeMillis() - startTime
        if (verdict == CallScreeningEvaluator.ScreeningResult.ALLOW) {
            Log.i("NullifyScreening", "Decision in ${elapsed}ms — allowed (contact/whitelist)")
            respondToCall(callDetails, CallResponse.Builder().build())
            logCall(rawNumber, "ALLOWED", "in whitelist")
        } else {
            Log.i("NullifyScreening", "Decision in ${elapsed}ms — blocked (unknown)")
            blockCall(callDetails, "unknown number")
            logCall(rawNumber, "BLOCKED", "not in whitelist")
        }
    }

    /**
     * Synchronous hot path: in-memory cache first (no I/O, no 5s-timeout
     * risk). Falls back to Room only while the cache is still cold, and
     * fail-closes (blocks) on DB errors — this is a firewall.
     */
    private fun isNumberAllowed(normalizedNumber: String): Boolean {
        val app = applicationContext as NullifyApp
        if (!app.allowedNumberCache.isEmpty()) {
            return app.allowedNumberCache.isAllowed(normalizedNumber)
        }
        return try {
            runBlocking(Dispatchers.IO) {
                app.contactRepository.isNumberAllowed(normalizedNumber)
            }
        } catch (e: Exception) {
            Log.e("NullifyScreening", "Error querying database for call screening", e)
            false
        }
    }

    /**
     * Synchronous hot path for the master switch: in-memory cache first,
     * Room only while the cache is cold. Fail-closed (screening stays ON)
     * on DB errors.
     */
    private fun isScreeningEnabled(): Boolean {
        val app = applicationContext as NullifyApp
        val cache = app.screeningPolicyCache
        if (cache.isWarm()) {
            return cache.isScreeningEnabled()
        }
        return try {
            runBlocking(Dispatchers.IO) {
                app.settingsRepository.isScreeningEnabled()
            }
        } catch (e: Exception) {
            Log.e("NullifyScreening", "Error reading screening policy", e)
            true
        }
    }

    private fun blockCall(callDetails: Call.Details, reason: String) {
        // NOTE: setSilenceCall(true) must NOT accompany disallow=true
        // (docs: silence "only makes sense when the call has not been disallowed").
        val spec = defaultBlockSpec()
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(spec.disallowCall)
                .setRejectCall(spec.rejectCall)
                .setSkipCallLog(spec.skipCallLog)
                .setSkipNotification(spec.skipNotification)
                .build()
        )
    }

    private fun logCall(phoneNumber: String, result: String, reason: String) {
        logScope.launch {
            try {
                val app = applicationContext as NullifyApp
                app.callLogRepository.insert(
                    CallLogEntry(
                        phoneNumber = phoneNumber,
                        result = result,
                        reason = reason,
                        timestamp = System.currentTimeMillis(),
                    )
                )
            } catch (e: Exception) {
                Log.e("NullifyScreening", "Failed to log call", e)
            }
        }
    }
}
