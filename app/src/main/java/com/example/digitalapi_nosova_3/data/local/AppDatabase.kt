package com.example.digitalapi_nosova_3.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ArtEntity::class,
        CollectionEntity::class,
        CollectionArtworkCrossRef::class,
        NoteEntity::class,
        HistoryEntity::class,
        CachedArtworkEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artDao(): ArtDao
    abstract fun collectionDao(): CollectionDao
    abstract fun noteDao(): NoteDao
    abstract fun historyDao(): HistoryDao
    abstract fun cachedArtworkDao(): CachedArtworkDao
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS collection_artworks")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS collection_artworks (
                collectionId INTEGER NOT NULL,
                artworkId INTEGER NOT NULL,
                title TEXT NOT NULL,
                artistTitle TEXT,
                imageId TEXT,
                PRIMARY KEY(collectionId, artworkId),
                FOREIGN KEY(collectionId) REFERENCES collections(id) ON DELETE CASCADE
            )
        """.trimIndent())
    }
}
