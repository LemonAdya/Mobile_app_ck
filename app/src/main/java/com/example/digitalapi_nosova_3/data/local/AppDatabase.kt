package com.example.digitalapi_nosova_3.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ArtEntity::class,
        CollectionEntity::class,
        CollectionArtworkCrossRef::class,
        NoteEntity::class,
        HistoryEntity::class,
        CachedArtworkEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artDao(): ArtDao
    abstract fun collectionDao(): CollectionDao
    abstract fun noteDao(): NoteDao
    abstract fun historyDao(): HistoryDao
    abstract fun cachedArtworkDao(): CachedArtworkDao
}
