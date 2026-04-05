package com.example.digitalapi_nosova_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class ArtEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val artistDisplay: String?,
    val imageId: String?
)