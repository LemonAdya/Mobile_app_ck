package com.example.digitalapi_nosova_3.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<ArtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(art: ArtEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean
}