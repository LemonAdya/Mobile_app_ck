package com.example.digitalapi_nosova_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_artworks")
data class CachedArtworkEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val artistTitle: String?,
    val imageId: String?,
    val description: String?,
    val dateDisplay: String?,
    val mediumDisplay: String?,
    val cachedAt: Long,
    val imageLocalPath: String?
)
