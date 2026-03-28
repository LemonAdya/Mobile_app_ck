package com.example.digitalapi_nosova_3.di

import android.content.Context
import androidx.room.Room
import com.example.digitalapi_nosova_3.data.local.AppDatabase
import com.example.digitalapi_nosova_3.data.local.ArtDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "art_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideArtDao(database: AppDatabase): ArtDao {
        return database.artDao()
    }
}
