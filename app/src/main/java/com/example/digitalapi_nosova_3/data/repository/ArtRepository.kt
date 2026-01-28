package com.example.digitalapi_nosova_3.data.repository

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.model.Artwork

class ArtRepository(private val apiService: ArtApiService) {
    private var cachedArtworks: List<Artwork>? = null
    private var lastIiifUrl: String? = null

    suspend fun getArtworks(forceRefresh: Boolean = false): Pair<List<Artwork>, String> {
        if (!forceRefresh && cachedArtworks != null && lastIiifUrl != null) {
            return Pair(cachedArtworks!!, lastIiifUrl!!)
        }

        val response = apiService.getArtworks()
        val artworks = response.data
        val iiifUrl = response.config.iiifUrl

        cachedArtworks = artworks
        lastIiifUrl = iiifUrl

        return Pair(artworks, iiifUrl)
    }

    suspend fun searchArtworks(query: String): Pair<List<Artwork>, String> {
        val response = apiService.searchArtworks(query = query)
        val artworks = response.data
        val iiifUrl = response.config.iiifUrl
        return Pair(artworks, iiifUrl)
    }

    suspend fun getArtworkDetails(id: Int): Pair<Artwork, String> {
        val response = apiService.getArtworkDetails(id = id)
        val artwork = response.data
        val iiifUrl = response.config.iiifUrl
        return Pair(artwork, iiifUrl)
    }
}