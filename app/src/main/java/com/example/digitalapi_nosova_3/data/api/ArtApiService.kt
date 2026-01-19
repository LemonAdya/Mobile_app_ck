package com.example.digitalapi_nosova_3.data.api

import com.example.digitalapi_nosova_3.data.model.ArtworkDetailResponse
import com.example.digitalapi_nosova_3.data.model.ArtworkResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ArtApiService {
    @GET("artworks")
    suspend fun getArtworks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,artist_title,image_id"
    ): ArtworkResponse

    @GET("artworks/search")
    suspend fun searchArtworks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "id,title,artist_title,image_id"
    ): ArtworkResponse

    @GET("artworks/{id}")
    suspend fun getArtworkDetails(
        @Path("id") id: Int,
        @Query("fields") fields: String = "id,title,artist_title,image_id,description,date_display,medium_display"
    ): ArtworkDetailResponse
}
