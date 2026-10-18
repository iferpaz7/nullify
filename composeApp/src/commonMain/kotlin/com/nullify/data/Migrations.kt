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

/**
 * v3 -> v4: adds the [ScreeningSettings] singleton table holding the
 * screening master switch. Seeded with screening enabled so existing users
 * keep blocking unknown callers by default.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `screening_settings` " +
                "(`id` INTEGER NOT NULL, `screeningEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "INSERT OR REPLACE INTO screening_settings (id, screeningEnabled) VALUES (0, 1)"
        )
    }
}
