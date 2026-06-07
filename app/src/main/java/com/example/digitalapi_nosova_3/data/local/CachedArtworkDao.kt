package com.example.digitalapi_nosova_3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedArtworkDao {
    @Query("SELECT * FROM cached_artworks WHERE id = :id")
    suspend fun getCachedArtwork(id: Int): CachedArtworkEntity?

    @Query("SELECT * FROM cached_artworks ORDER BY cachedAt DESC")
    fun getAllCachedArtworks(): Flow<List<CachedArtworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(artwork: CachedArtworkEntity)

    @Query("DELETE FROM cached_artworks WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM cached_artworks WHERE cachedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM cached_artworks")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cached_artworks")
    suspend fun getCachedCount(): Int
}
