package com.nullify.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "allowed_contacts")
data class AllowedContact(
    @PrimaryKey val normalizedNumber: String,
    val displayName: String
)
