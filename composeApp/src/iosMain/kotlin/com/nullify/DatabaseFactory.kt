package com.nullify

import androidx.room.Room
import com.nullify.data.NullifyDatabase

fun createNullifyDatabase(): NullifyDatabase {
    return Room.databaseBuilder<NullifyDatabase>(
        name = "nullify_database"
    )
        .addCallback(NullifyDatabase.DatabasePrepopulateCallback())
        .build()
}
