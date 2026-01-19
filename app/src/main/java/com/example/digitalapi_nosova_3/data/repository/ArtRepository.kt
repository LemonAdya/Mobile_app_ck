package com.example.digitalapi_nosova_3.data.repository

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.model.Artwork

class ArtRepository(private val apiService: ArtApiService) {
    private var cachedArtworks: List<Artwork>? = null
    private var lastIiifUrl: String? = null

    suspend fun getArtworks(forceRefresh: Boolean = false): Pair<List<Artwork>, String> {
        if (!forceRefresh && cachedArtworks != null) return Pair(cachedArtworks!!, lastIiifUrl!!)
        val response = apiService.getArtworks()
        cachedArtworks = response.data
        lastIiifUrl = response.config.iiifUrl
        return Pair(response.data, response.config.iiifUrl)
    }

    suspend fun searchArtworks(query: String) = apiService.searchArtworks(query).let { Pair(it.data, it.config.iiifUrl) }
    suspend fun getArtworkDetails(id: Int) = apiService.getArtworkDetails(id).let { Pair(it.data, it.config.iiifUrl) }
}
