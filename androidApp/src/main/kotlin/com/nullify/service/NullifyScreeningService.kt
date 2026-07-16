package com.nullify.service

import android.net.Uri
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.nullify.NullifyApp
import com.nullify.data.CallLogEntry
import com.nullify.utils.EcuadorPhoneUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NullifyScreeningService : CallScreeningService() {

    private val logScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val startTime = System.currentTimeMillis()

        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val handle = callDetails.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""

        Log.i("NullifyScreening", "Incoming call from: $rawNumber")

        if (rawNumber.isEmpty()) {
            val elapsed = System.currentTimeMillis() - startTime
            Log.i("NullifyScreening", "Decision in ${elapsed}ms — blocking private/unknown")
            blockCall(callDetails, "private/unknown number")
            logCall(rawNumber, "BLOCKED", "private/unknown number")
            return
        }

        if (EcuadorPhoneUtils.isEmergencyNumber(rawNumber)) {
            val elapsed = System.currentTimeMillis() - startTime
            Log.i("NullifyScreening", "Decision in ${elapsed}ms — emergency allowed")
            respondToCall(callDetails, CallResponse.Builder().build())
            logCall(rawNumber, "ALLOWED", "emergency number")
            return
        }

        val normalizedIncoming = EcuadorPhoneUtils.normalizeForDatabase(rawNumber)

        val app = applicationContext as NullifyApp
        val isAllowed = app.contactRepository.isNumberAllowed(normalizedIncoming)

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
        respondToCall(callDetails, CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build())
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
