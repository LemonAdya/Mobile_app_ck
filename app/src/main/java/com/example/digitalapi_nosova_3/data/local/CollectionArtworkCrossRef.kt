package com.example.digitalapi_nosova_3.data.local

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "collection_artworks",
    primaryKeys = ["collectionId", "artworkId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CollectionArtworkCrossRef(
    val collectionId: Long,
    val artworkId: Int,
    val title: String,
    val artistTitle: String?,
    val imageId: String?
)
