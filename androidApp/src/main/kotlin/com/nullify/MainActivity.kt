package com.nullify

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nullify.sync.ContactSyncWorker
import com.nullify.ui.NullifyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requiredPermissions = mutableListOf(Manifest.permission.READ_CONTACTS).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private var isProtectionActive by mutableStateOf(false)

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            scheduleContactSync()
        }

    private val roleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // The role is granted only on RESULT_OK; refresh state either way so
            // the UI banner reflects reality instead of assuming success.
            isProtectionActive =
                result.resultCode == Activity.RESULT_OK && CallScreeningRole.isHeld(this)
        }

    private val viewModel: NullifyViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestCallScreeningRoleIfNeeded()
        requestPermissionsIfNeeded()

        setContent {
            NullifyApp(
                viewModel = viewModel,
                isProtectionActive = isProtectionActive,
                onRequestProtection = ::requestCallScreeningRoleIfNeeded,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // The user can grant/revoke the role in Settings at any time.
        isProtectionActive = CallScreeningRole.isHeld(this)
    }

    fun requestCallScreeningRoleIfNeeded() {
        isProtectionActive = CallScreeningRole.isHeld(this)
        if (isProtectionActive) return
        val intent = CallScreeningRole.createRequestIntent(this) ?: return
        roleLauncher.launch(intent)
    }

    private fun requestPermissionsIfNeeded() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        } else {
            scheduleContactSync()
        }
    }

    private fun scheduleContactSync() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val oneTimeSync = OneTimeWorkRequestBuilder<ContactSyncWorker>()
            .build()
        WorkManager.getInstance(this).enqueue(oneTimeSync)

        val periodicSync = PeriodicWorkRequestBuilder<ContactSyncWorker>(
            6, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "NullifyContactSync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSync
        )
    }
}
