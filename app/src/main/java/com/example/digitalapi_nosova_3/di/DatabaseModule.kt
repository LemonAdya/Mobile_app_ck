package com.example.digitalapi_nosova_3.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.digitalapi_nosova_3.data.local.*
import com.example.digitalapi_nosova_3.data.preferences.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS collections (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    createdAt INTEGER NOT NULL
                )
            """)
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS collection_artworks (
                    collectionId INTEGER NOT NULL,
                    artworkId INTEGER NOT NULL,
                    PRIMARY KEY(collectionId, artworkId),
                    FOREIGN KEY(collectionId) REFERENCES collections(id) ON DELETE CASCADE
                )
            """)
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS notes (
                    artworkId INTEGER PRIMARY KEY NOT NULL,
                    text TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """)
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    artworkId INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    artistTitle TEXT,
                    imageId TEXT,
                    viewedAt INTEGER NOT NULL
                )
            """)
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS cached_artworks (
                    id INTEGER PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL,
                    artistTitle TEXT,
                    imageId TEXT,
                    description TEXT,
                    dateDisplay TEXT,
                    mediumDisplay TEXT,
                    cachedAt INTEGER NOT NULL,
                    imageLocalPath TEXT
                )
            """)
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "art_database"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideArtDao(database: AppDatabase): ArtDao = database.artDao()

    @Provides
    @Singleton
    fun provideCollectionDao(database: AppDatabase): CollectionDao = database.collectionDao()

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideHistoryDao(database: AppDatabase): HistoryDao = database.historyDao()

    @Provides
    @Singleton
    fun provideCachedArtworkDao(database: AppDatabase): CachedArtworkDao = database.cachedArtworkDao()

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }
}
