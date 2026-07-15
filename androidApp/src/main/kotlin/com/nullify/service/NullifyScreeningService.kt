package com.nullify.service

import android.net.Uri
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.nullify.NullifyApp
import com.nullify.utils.EcuadorPhoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class NullifyScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val handle = callDetails.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""

        Log.i("NullifyScreening", "Incoming call from: $rawNumber")

        if (rawNumber.isEmpty()) {
            blockCall(callDetails, "private/unknown number")
            return
        }

        if (EcuadorPhoneUtils.isEmergencyNumber(rawNumber)) {
            Log.i("NullifyScreening", "Emergency number — allowing")
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val normalizedIncoming = EcuadorPhoneUtils.normalizeForDatabase(rawNumber)

        val isAllowed = runBlocking(Dispatchers.IO) {
            val app = applicationContext as NullifyApp
            app.database.contactDao().isNumberAllowed(normalizedIncoming)
        }

        if (isAllowed) {
            Log.i("NullifyScreening", "Number found in contacts — allowing")
            respondToCall(callDetails, CallResponse.Builder().build())
        } else {
            blockCall(callDetails, "unknown number")
        }
    }

    private fun blockCall(callDetails: Call.Details, reason: String) {
        Log.i("NullifyScreening", "Blocking $reason")
        respondToCall(callDetails, CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build())
    }
}
