package com.nullify

import android.content.Context
import androidx.room.Room
import com.nullify.data.NullifyDatabase

fun createNullifyDatabase(context: Context): NullifyDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        NullifyDatabase::class.java,
        "nullify_database"
    )
        .addCallback(NullifyDatabase.DatabasePrepopulateCallback())
        .fallbackToDestructiveMigration(false)
        .build()
}
