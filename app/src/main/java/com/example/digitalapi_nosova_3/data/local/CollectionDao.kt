package com.example.digitalapi_nosova_3.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollectionById(id: Long): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: CollectionEntity): Long

    @Update
    suspend fun update(collection: CollectionEntity)

    @Delete
    suspend fun delete(collection: CollectionEntity)

    @Query("SELECT artworkId FROM collection_artworks WHERE collectionId = :collectionId")
    fun getArtworkIdsInCollection(collectionId: Long): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addArtworkToCollection(crossRef: CollectionArtworkCrossRef)

    @Query("DELETE FROM collection_artworks WHERE collectionId = :collectionId AND artworkId = :artworkId")
    suspend fun removeArtworkFromCollection(collectionId: Long, artworkId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM collection_artworks WHERE collectionId = :collectionId AND artworkId = :artworkId)")
    suspend fun isArtworkInCollection(collectionId: Long, artworkId: Int): Boolean
}
