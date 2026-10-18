package com.nullify.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nullify.utils.ContactSource
import com.nullify.utils.EcuadorPhoneUtils

@Database(
    entities = [AllowedContact::class, CallLogEntry::class, ScreeningSettings::class],
    version = 4,
    exportSchema = false,
)
abstract class NullifyDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun callLogDao(): CallLogDao
    abstract fun settingsDao(): SettingsDao

    class DatabasePrepopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            for ((number, label) in getEcuadorSystemWhitelist()) {
                db.execSQL(
                    "INSERT OR REPLACE INTO allowed_contacts (normalizedNumber, displayName, source) VALUES (?, ?, ?)",
                    arrayOf(number, label, ContactSource.SEED)
                )
            }
            db.execSQL(
                "INSERT OR REPLACE INTO screening_settings (id, screeningEnabled) VALUES (0, 1)"
            )
        }

        private fun getEcuadorSystemWhitelist(): List<Pair<String, String>> {
            return listOf(
                "1700600600" to "BGR Contact Center",
                "023965006" to "BGR Canales de Atención",
                "022509929" to "BGR Matriz Quito",
                "022999999" to "Banco Pichincha Canales",
                "043730100" to "Banco Guayaquil Atención",
                "1700123123" to "Produbanco Call Center"
            ).map { (number, label) ->
                EcuadorPhoneUtils.normalizeForDatabase(number) to "[SISTEMA] $label"
            }
        }
    }
}
