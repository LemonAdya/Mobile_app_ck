package com.example.digitalapi_nosova_3.di

import android.content.Context
import com.example.digitalapi_nosova_3.data.sync.SyncScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideSyncScheduler(@ApplicationContext context: Context): SyncScheduler {
        return SyncScheduler(context)
    }
}
