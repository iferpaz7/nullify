package com.nullify.test

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.ComponentActivity

class SimulateCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val number = intent.getStringExtra("number") ?: "0999999999"
        Log.i("SimulateCall", "Simulating incoming call from: $number")

        val tm = getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        // Try the SIM's phone account directly.
        // On Samsung A15 (Android 16) the SIM is:
        //   com.android.phone / com.android.services.telephony.TelephonyConnectionService
        val simComponent = ComponentName(
            "com.android.phone",
            "com.android.services.telephony.TelephonyConnectionService"
        )
        val simHandle = PhoneAccountHandle(simComponent, "3") // SIM 1

        try {
            val extras = Bundle().apply {
                putString(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, number)
            }
            tm.addNewIncomingCall(simHandle, extras)
            Log.i("SimulateCall", "Incoming call triggered for $number")
        } catch (e: SecurityException) {
            Log.e("SimulateCall", "Permission denied: ${e.message}")
        } catch (e: IllegalArgumentException) {
            Log.e("SimulateCall", "Invalid account: ${e.message}")
        } catch (e: Exception) {
            Log.e("SimulateCall", "Failed: ${e.message}")
        }

        finish()
    }
}
