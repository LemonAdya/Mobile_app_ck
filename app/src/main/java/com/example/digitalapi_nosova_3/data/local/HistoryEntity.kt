package com.example.digitalapi_nosova_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val artworkId: Int,
    val title: String,
    val artistTitle: String?,
    val imageId: String?,
    val viewedAt: Long
)
