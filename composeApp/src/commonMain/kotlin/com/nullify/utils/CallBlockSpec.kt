package com.nullify.utils

/**
 * Platform-agnostic description of how to reject a call.
 *
 * Per CallScreeningService.CallResponse.Builder docs, setSilenceCall(true)
 * "only makes sense when the call has not been disallowed", so a real
 * block must be disallow + reject WITHOUT silence.
 */
data class CallBlockSpec(
    val disallowCall: Boolean = true,
    val rejectCall: Boolean = true,
    val silenceCall: Boolean = false,
    val skipCallLog: Boolean = false,
    val skipNotification: Boolean = false,
)

fun defaultBlockSpec(): CallBlockSpec = CallBlockSpec()
