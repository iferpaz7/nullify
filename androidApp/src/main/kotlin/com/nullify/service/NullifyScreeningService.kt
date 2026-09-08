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

        val fastPathResult = CallScreeningEvaluator.evaluateFastPath(
            rawNumber = rawNumber,
            isIncoming = isIncoming,
            presentationAllowed = presentationAllowed,
            verificationFailed = verificationFailed,
        )

        when (fastPathResult) {
            CallScreeningEvaluator.ScreeningResult.ALLOW -> {
                respondToCall(callDetails, CallResponse.Builder().build())
                if (EcuadorPhoneUtils.isEmergencyNumber(rawNumber)) {
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
        val isAllowed = try {
            runBlocking(Dispatchers.IO) {
                val app = applicationContext as NullifyApp
                app.contactRepository.isNumberAllowed(normalizedIncoming)
            }
        } catch (e: Exception) {
            Log.e("NullifyScreening", "Error querying database for call screening", e)
            false
        }

        val elapsed = System.currentTimeMillis() - startTime
        if (isAllowed) {
            Log.i("NullifyScreening", "Decision in ${elapsed}ms — allowed (contact/whitelist)")
            respondToCall(callDetails, CallResponse.Builder().build())
            logCall(rawNumber, "ALLOWED", "in whitelist")
        } else {
            Log.i("NullifyScreening", "Decision in ${elapsed}ms — blocked (unknown)")
            blockCall(callDetails, "unknown number")
            logCall(rawNumber, "BLOCKED", "not in whitelist")
        }
    }

    private fun blockCall(callDetails: Call.Details, reason: String) {
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSilenceCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
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
