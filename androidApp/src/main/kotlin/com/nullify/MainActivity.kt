package com.nullify

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nullify.sync.ContactSyncWorker
import com.nullify.ui.NullifyViewModel
import com.nullify.ui.NullifyViewModelFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requiredPermissions = mutableListOf(Manifest.permission.READ_CONTACTS).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            scheduleContactSync()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        val app = application as NullifyApp
        val db = app.database
        val viewModel: NullifyViewModel by viewModels {
            NullifyViewModelFactory(db.contactDao(), db.callLogDao())
        }

        requestCallScreeningRole()
        requestPermissionsIfNeeded()

        setContent {
            val view = LocalView.current
            val statusBarColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.toArgb()
            if (!view.isInEditMode) {
                SideEffect {
                    @Suppress("DEPRECATION")
                    window.statusBarColor = statusBarColor
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                }
            }
            NullifyApp(viewModel = viewModel)
        }
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

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                    startActivity(intent)
                }
            }
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
