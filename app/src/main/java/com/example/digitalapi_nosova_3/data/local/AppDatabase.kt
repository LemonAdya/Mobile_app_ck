package com.example.digitalapi_nosova_3.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ArtEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artDao(): ArtDao
}
