package com.example.digitalapi_nosova_3.di

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideArtApi(): ArtApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.artic.edu/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArtApiService::class.java)
    }
}