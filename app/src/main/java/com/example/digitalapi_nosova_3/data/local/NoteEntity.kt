package com.example.digitalapi_nosova_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val artworkId: Int,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long
)
