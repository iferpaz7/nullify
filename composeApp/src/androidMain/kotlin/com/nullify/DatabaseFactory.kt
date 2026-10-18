package com.nullify

import android.content.Context
import androidx.room.Room
import com.nullify.data.MIGRATION_2_3
import com.nullify.data.MIGRATION_3_4
import com.nullify.data.NullifyDatabase

fun createNullifyDatabase(context: Context): NullifyDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        NullifyDatabase::class.java,
        "nullify_database"
    )
        .addCallback(NullifyDatabase.DatabasePrepopulateCallback())
        .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
        .fallbackToDestructiveMigration(false)
        .build()
}
