package com.nullify.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nullify.utils.ContactSource

@Entity(tableName = "allowed_contacts")
data class AllowedContact(
    @PrimaryKey val normalizedNumber: String,
    val displayName: String,
    @ColumnInfo(defaultValue = "'system'") val source: String = ContactSource.SYSTEM,
)
