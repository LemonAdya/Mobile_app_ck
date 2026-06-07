package com.example.digitalapi_nosova_3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY viewedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 50): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY viewedAt DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clearAll()

    @Query("DELETE FROM history WHERE viewedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
