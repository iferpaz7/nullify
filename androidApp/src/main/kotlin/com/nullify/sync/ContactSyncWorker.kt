package com.nullify.sync

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nullify.NullifyApp
import com.nullify.data.AllowedContact
import com.nullify.utils.ContactSource
import com.nullify.utils.ContactSyncPlanner
import com.nullify.utils.EcuadorPhoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext Result.success()
        }

        val app = applicationContext as NullifyApp
        val freshSystemContacts = mutableListOf<AllowedContact>()

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
                        freshSystemContacts.add(
                            AllowedContact(
                                normalizedNumber = normalized,
                                displayName = name,
                                source = ContactSource.SYSTEM,
                            )
                        )
                    }
                }
            }

            if (freshSystemContacts.isNotEmpty()) {
                // Delete stale SYSTEM entries (removed from phone contacts) while
                // preserving MANUAL and SEED entries untouched.
                val existing = app.contactRepository.getAllOnce()
                val plan = ContactSyncPlanner.planSync(
                    existing = existing,
                    freshSystemNumbers = freshSystemContacts.map { it.normalizedNumber }.toSet(),
                )
                if (plan.staleSystemNumbers.isNotEmpty()) {
                    app.contactRepository.deleteByNumbers(plan.staleSystemNumbers)
                }
                app.contactRepository.insertAll(freshSystemContacts)
                // Keep the screening hot-path cache in sync immediately.
                app.allowedNumberCache.replaceAll(
                    app.contactRepository.getAllOnce().map { it.normalizedNumber }
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
