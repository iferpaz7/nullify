package com.nullify.service

import android.net.Uri
import android.telecom.Call
import android.telecom.CallScreeningService
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

        val handle: Uri? = callDetails.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""

        if (rawNumber.isEmpty()) {
            blockCall(callDetails)
            return
        }

        if (EcuadorPhoneUtils.isEmergencyNumber(rawNumber)) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val normalizedIncoming = EcuadorPhoneUtils.normalizeForDatabase(rawNumber)

        val isAllowed = runBlocking(Dispatchers.IO) {
            val app = applicationContext as NullifyApp
            app.database.contactDao().isNumberAllowed(normalizedIncoming)
        }

        if (isAllowed) {
            respondToCall(callDetails, CallResponse.Builder().build())
        } else {
            blockCall(callDetails)
        }
    }

    private fun blockCall(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)
    }
}
