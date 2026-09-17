package com.nullify

import androidx.room.Room
import com.nullify.data.MIGRATION_2_3
import com.nullify.data.NullifyDatabase

fun createNullifyDatabase(): NullifyDatabase {
    return Room.databaseBuilder<NullifyDatabase>(
        name = "nullify_database"
    )
        .addCallback(NullifyDatabase.DatabasePrepopulateCallback())
        .addMigrations(MIGRATION_2_3)
        .build()
}
