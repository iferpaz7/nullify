Aquí tienes la especificación técnica completa y consolidada para **Nullify**. He estructurado este documento con un enfoque de arquitectura limpia (*Clean Architecture*) y buenas prácticas de desarrollo móvil en Android para que puedas pasárselo directamente a cualquier modelo de IA (como Cursor, Claude, GPT-4o o Copilot) y generar el proyecto completo de manera impecable.

---

# Prompt de Generación de Código para la IA

```markdown
# SYSTEM PROMPT / INSTRUCTIONS FOR CODE GENERATION

You are an expert Senior Android Developer. Your task is to generate a fully functional, production-ready Android application named **Nullify** using Kotlin, Jetpack Compose, Room Database, and Android WorkManager.

## App Specifications: Nullify
- **Goal:** A 100% offline, privacy-first call blocker that intercepts and rejects any incoming call from numbers not saved in the user's contacts or local whitelist.
- **Privacy Design:** Absolutely NO internet permission. Local processing only. Zero telemetry.
- **Target OS:** Android 10 (API 29) to Android 14+ (API 34).
- **Core Technology:** `CallScreeningService` (for millisecond-level native blocking before the phone rings) and `Room` for sub-5ms caching.
- **Target Device:** Samsung Galaxy A15 (One UI optimized, handles aggressive background limitations by utilizing system-invoked services).

---

## 1. Project Configuration & Dependencies

### build.gradle.kts (Module: app)
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.yourdomain.nullify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yourdomain.nullify"
        minSdk = 29 // Required for CallScreeningService
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // WorkManager (Kotlin + Coroutines)
    implementation(libs.androidx.work.runtime.ktx)

    // Lifecycle & ViewModels
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

```

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="[http://schemas.android.com/apk/res/android](http://schemas.android.com/apk/res/android)"
    package="com.yourdomain.nullify">

    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Core Call Blocker Engine -->
        <service
            android:name=".service.NullifyScreeningService"
            android:permission="android.permission.BIND_SCREENING_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.telecom.CallScreeningService" />
            </intent-filter>
        </service>

        <!-- Connection Provider mock for SIM-less local testing (Debug only) -->
        <service
            android:name=".test.MockConnectionService"
            android:permission="android.permission.BIND_TELECOM_CONNECTION_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.telecom.ConnectionService" />
            </intent-filter>
        </service>

    </application>
</manifest>

```

---

## 2. Core Engine & Normalization (Ecuador Telephony Rules)

### File: `com/yourdomain/nullify/utils/EcuadorPhoneUtils.kt`

```kotlin
package com.yourdomain.nullify.utils

object EcuadorPhoneUtils {
    // Whitelist in-memory fast-path for Ecuador public safety
    private val EMERGENCY_SHORT_CODES = setOf(
        "911", // ECU 911 (General Emergencies)
        "102", // Firefighters
        "115", // Police Department
        "101", // Police legacy
        "131", // Red Cross
        "171"  // Public Health Ministry
    )

    /**
     * Identifies short emergency numbers quickly to avoid any disk/Room queries.
     */
    fun isEmergencyNumber(rawNumber: String): Boolean {
        val clean = rawNumber.replace(Regex("[^0-9]"), "")
        return EMERGENCY_SHORT_CODES.contains(clean)
    }

    /**
     * Sanitizes complex country code structures for local matching:
     * - Strips country code "+593"
     * - Strips leading regional "0"
     * - Keeps only the meaningful national identifier digits
     */
    fun normalizeForDatabase(rawNumber: String): String {
        val clean = rawNumber.replace(Regex("[^0-9]"), "")
        return when {
            clean.startsWith("593") -> clean.substring(3)
            clean.startsWith("0") -> clean.substring(1)
            else -> clean
        }
    }
}

```

---

## 3. Data & Storage Layer (Room Cache)

### File: `com/yourdomain/nullify/data/AllowedContact.kt`

```kotlin
package com.yourdomain.nullify.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "allowed_contacts")
data class AllowedContact(
    @PrimaryKey val normalizedNumber: String, // Unique local suffix
    val displayName: String
)

```

### File: `com/yourdomain/nullify/data/ContactDao.kt`

```kotlin
package com.yourdomain.nullify.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT EXISTS(SELECT * FROM allowed_contacts WHERE normalizedNumber = :number)")
    fun isNumberAllowed(number: String): Boolean

    @Query("SELECT * FROM allowed_contacts ORDER BY displayName ASC")
    fun getAllAllowedContactsFlow(): Flow<List<AllowedContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: AllowedContact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(contacts: List<AllowedContact>)

    @Delete
    suspend fun delete(contact: AllowedContact)

    @Query("DELETE FROM allowed_contacts")
    fun clearAll()
}

```

### File: `com/yourdomain/nullify/data/NullifyDatabase.kt`

```kotlin
package com.yourdomain.nullify.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourdomain.nullify.utils.EcuadorPhoneUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [AllowedContact::class], version = 1, exportSchema = false)
abstract class NullifyDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: NullifyDatabase? = null

        fun getInstance(context: Context): NullifyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NullifyDatabase::class.java,
                    "nullify_database"
                )
                .addCallback(DatabasePrepopulateCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabasePrepopulateCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getInstance(context).contactDao()
                    dao.insertAll(getEcuadorSystemWhitelist())
                }
            }

            private fun getEcuadorSystemWhitelist(): List<AllowedContact> {
                val officialEntities = listOf(
                    // --- BGR (Banco General Rumiñahui) ---
                    "1700600600" to "BGR Contact Center",
                    "023965006" to "BGR Canales de Atención",
                    "022509929" to "BGR Matriz Quito",
                    
                    // --- Banco Pichincha ---
                    "022999999" to "Banco Pichincha Canales",
                    
                    // --- Banco Guayaquil ---
                    "043730100" to "Banco Guayaquil Atención",
                    
                    // --- Produbanco ---
                    "1700123123" to "Produbanco Call Center"
                )

                return officialEntities.map { (number, label) ->
                    AllowedContact(
                        normalizedNumber = EcuadorPhoneUtils.normalizeForDatabase(number),
                        displayName = "[SISTEMA] $label"
                    )
                }
            }
        }
    }
}

```

---

## 4. Background Sync Component (WorkManager)

### File: `com/yourdomain/nullify/sync/ContactSyncWorker.kt`

```kotlin
package com.yourdomain.nullify.sync

import android.content.Context
import android.provider.ContactsContract
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourdomain.nullify.data.AllowedContact
import com.yourdomain.nullify.data.NullifyDatabase
import com.yourdomain.nullify.utils.EcuadorPhoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = NullifyDatabase.getInstance(applicationContext)
        val contactsList = mutableListOf<AllowedContact>()

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )

        try {
            applicationContext.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val rawNumber = cursor.getString(numberIdx) ?: continue
                    val name = cursor.getString(nameIdx) ?: "Contacto Desconocido"
                    
                    val normalized = EcuadorPhoneUtils.normalizeForDatabase(rawNumber)
                    if (normalized.isNotEmpty()) {
                        contactsList.add(AllowedContact(normalized, name))
                    }
                }
            }

            // Sync with DB
            if (contactsList.isNotEmpty()) {
                db.contactDao().clearAll()
                // Seed official emergency entities alongside real contacts
                db.contactDao().insertAll(contactsList)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

```

---

## 5. Call screening Core Service

### File: `com/yourdomain/nullify/service/NullifyScreeningService.kt`

```kotlin
package com.yourdomain.nullify.service

import android.telecom.Call
import android.telecom.CallScreeningService
import android.net.Uri
import com.yourdomain.nullify.data.NullifyDatabase
import com.yourdomain.nullify.utils.EcuadorPhoneUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers

class NullifyScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        // Evaluate incoming directions only
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            allowCall(callDetails)
            return
        }

        val handle: Uri? = callDetails.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""
        
        // Block restricted / private / hidden numbers directly
        if (rawNumber.isEmpty()) {
            blockCall(callDetails)
            return
        }

        // --- Fast-Path (Emergency verification bypasses Room) ---
        if (EcuadorPhoneUtils.isEmergencyNumber(rawNumber)) {
            allowCall(callDetails)
            return
        }

        // --- Core Validation Path ---
        val normalizedIncoming = EcuadorPhoneUtils.normalizeForDatabase(rawNumber)

        // Sychronous check within system-provided thread limit (5 seconds maximum)
        val isAllowed = runBlocking(Dispatchers.IO) {
            val db = NullifyDatabase.getInstance(applicationContext)
            db.contactDao().isNumberAllowed(normalizedIncoming)
        }

        if (isAllowed) {
            allowCall(callDetails)
        } else {
            blockCall(callDetails)
        }
    }

    private fun allowCall(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())
    }

    private fun blockCall(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)       // Immediately drops line
            .setSkipCallLog(false)     // Ensure users see missed blocking attempts in dialer log
            .setSkipNotification(false) // Trigger OS quiet block banner notification
            .build()
        respondToCall(callDetails, response)
    }
}

```

---

## 6. Presentation Layer (Jetpack Compose MVVM)

### File: `com/yourdomain/nullify/presentation/NullifyViewModel.kt`

```kotlin
package com.yourdomain.nullify.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yourdomain.nullify.data.AllowedContact
import com.yourdomain.nullify.data.ContactDao
import com.yourdomain.nullify.utils.EcuadorPhoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NullifyViewModel(private val contactDao: ContactDao) : ViewModel() {

    val whitelist: StateFlow<List<AllowedContact>> = contactDao.getAllAllowedContactsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addManualContact(name: String, number: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val normalized = EcuadorPhoneUtils.normalizeForDatabase(number)
            if (normalized.isNotEmpty()) {
                val newContact = AllowedContact(
                    normalizedNumber = normalized,
                    displayName = name.trim().ifEmpty { "Entidad Manual" }
                )
                contactDao.insert(newContact)
            }
        }
    }

    fun removeContact(contact: AllowedContact) {
        viewModelScope.launch(Dispatchers.IO) {
            contactDao.delete(contact)
        }
    }
}

class NullifyViewModelFactory(private val contactDao: ContactDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NullifyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NullifyViewModel(contactDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

```

### File: `com/yourdomain/nullify/presentation/WhitelistScreen.kt`

```kotlin
package com.yourdomain.nullify.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yourdomain.nullify.data.AllowedContact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistScreen(viewModel: NullifyViewModel) {
    val whitelistState by viewModel.whitelist.collectAsState()

    var nameInput by remember { mutableStateOf("") }
    var numberInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nullify: Firewall Telefónico") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Addition Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Exceptuar un Número de Forma Manual",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nombre o Identificador (Ej. BGR)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = numberInput,
                        onValueChange = { 
                            numberInput = it
                            showError = false 
                        },
                        label = { Text("Número de teléfono local") },
                        placeholder = { Text("Ej. 0998887777 o 023965006") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showError
                    )
                    if (showError) {
                        Text(
                            text = "Por favor, ingresa un número de teléfono válido",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (numberInput.isNotBlank()) {
                                viewModel.addManualContact(nameInput, numberInput)
                                nameInput = ""
                                numberInput = ""
                                showError = false
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Permitir Llamadas")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Lista de Exclusión Autorizada",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (whitelistState.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ningún número agregado manualmente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(whitelistState, key = { it.normalizedNumber }) { contact ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = contact.displayName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "ID Normalizado: ${contact.normalizedNumber}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                IconButton(onClick = { viewModel.removeContact(contact) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

```

---

## 7. App Bootstrap & System Registration

### File: `com/yourdomain/nullify/MainActivity.kt`

```kotlin
package com.yourdomain.nullify

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.*
import com.yourdomain.nullify.data.NullifyDatabase
import com.yourdomain.nullify.presentation.NullifyViewModel
import com.yourdomain.nullify.presentation.NullifyViewModelFactory
import com.yourdomain.nullify.presentation.WhitelistScreen
import com.yourdomain.nullify.sync.ContactSyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = NullifyDatabase.getInstance(applicationContext)
        val viewModel: NullifyViewModel by viewModels {
            NullifyViewModelFactory(db.contactDao())
        }

        // 1. Request OS Call Screening authorization
        requestCallScreeningRole()

        // 2. Schedule contact synchronization routine
        scheduleContactSync()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WhitelistScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                    startActivityForResult(intent, ROLE_REQUEST_CODE)
                }
            }
        }
    }

    private fun scheduleContactSync() {
        val syncRequest = PeriodicWorkRequestBuilder<ContactSyncWorker>(
            6, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "NullifyContactSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    companion object {
        private const val ROLE_REQUEST_CODE = 101
    }
}

```

---

## 8. SIM-less Testing Environment (Simulator Framework)

### File: `com/yourdomain/nullify/test/MockConnectionService.kt`

```kotlin
package com.yourdomain.nullify.test

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager

class MockConnectionService : ConnectionService() {
    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val connection = object : Connection() {}
        connection.setAddress(request?.address, TelecomManager.PRESENTATION_ALLOWED)
        connection.setInitializing()
        connection.setActive()
        return connection
    }
}

```

### Script de Simulación Local (Ejecutar en la terminal de desarrollo)

Para probar el comportamiento en tu Samsung A15 físico sin chip, conecta el celular vía USB y ejecuta:

```bash
# 1. Habilita el Mock Connection Provider en el subsistema de telecomunicaciones
adb shell telecom set-phone-account-enabled "com.yourdomain.nullify/.test.MockConnectionService,nullify_mock_account,0"

# 2. Inyecta una llamada simulada para un número en la Whitelist (Ej. Banco BGR)
# Debe ser aceptada de manera normal por Nullify sin colgar.
adb shell am start-activity -a android.intent.action.VIEW \
  -d "tel:023965006" \
  --ez EXTRA_INCOMING_CALL_ADDRESS true

# 3. Inyecta una llamada de un número spam/desconocido de Ecuador
# Nullify interceptará la llamada y colgará la línea antes de emitir sonido.
adb shell am start-activity -a android.intent.action.VIEW \
  -d "tel:0992345678" \
  --ez EXTRA_INCOMING_CALL_ADDRESS true

```

```

```