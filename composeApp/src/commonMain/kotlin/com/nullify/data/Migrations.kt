package com.nullify.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nullify.utils.ContactSource

/**
 * v2 -> v3: adds the [AllowedContact.source] provenance column so contact
 * sync can delete stale system contacts without touching manual/seed ones.
 * Existing rows predate provenance tracking; they all came from contact
 * sync, so they default to SYSTEM.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE allowed_contacts ADD COLUMN source TEXT NOT NULL DEFAULT '${ContactSource.SYSTEM}'"
        )
    }
}
