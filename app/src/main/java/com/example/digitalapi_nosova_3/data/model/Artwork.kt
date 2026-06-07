package com.example.digitalapi_nosova_3.data.model

import com.google.gson.annotations.SerializedName

data class ArtworkResponse(
    @SerializedName("data") val data: List<Artwork>,
    @SerializedName("config") val config: ApiConfig
)

data class ArtworkDetailResponse(
    @SerializedName("data") val data: Artwork,
    @SerializedName("config") val config: ApiConfig
)

data class Artwork(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("artist_title") val artistTitle: String?,
    @SerializedName("image_id") val imageId: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("date_display") val dateDisplay: String?,
    @SerializedName("medium_display") val mediumDisplay: String?
)

data class ApiConfig(
    @SerializedName("iiif_url") val iiifUrl: String
)