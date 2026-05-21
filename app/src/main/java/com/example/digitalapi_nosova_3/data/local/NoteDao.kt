package com.example.digitalapi_nosova_3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE artworkId = :artworkId")
    suspend fun getNoteByArtworkId(artworkId: Int): NoteEntity?

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE artworkId = :artworkId")
    suspend fun deleteByArtworkId(artworkId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM notes WHERE artworkId = :artworkId)")
    suspend fun hasNote(artworkId: Int): Boolean
}
