package com.example.digitalapi_nosova_3.data.repository

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.model.Artwork

class ArtRepository(private val apiService: ArtApiService) {
    private var cachedArtworks: List<Artwork>? = null
    private var lastIiifUrl: String? = null

    suspend fun getArtworks(forceRefresh: Boolean = false): Pair<List<Artwork>, String> {
        val cached = cachedArtworks
        val url = lastIiifUrl
        if (!forceRefresh && cached != null && url != null) {
            return Pair(cached, url)
        }

        return try {
            val response = apiService.getArtworks()
            val data = response.data ?: emptyList()
            val iiif = response.config?.iiifUrl ?: ""

            cachedArtworks = data
            lastIiifUrl = iiif
            Pair(data, iiif)
        } catch (e: Exception) {
            Pair(emptyList(), "")
        }
    }

    suspend fun searchArtworks(query: String): Pair<List<Artwork>, String> {
        return try {
            val response = apiService.searchArtworks(query)
            Pair(response.data ?: emptyList(), response.config?.iiifUrl ?: "")
        } catch (e: Exception) {
            Pair(emptyList(), "")
        }
    }

    suspend fun getArtworkDetails(id: Int): Pair<Artwork?, String> {
        return try {
            val response = apiService.getArtworkDetails(id)
            Pair(response.data, response.config?.iiifUrl ?: "")
        } catch (e: Exception) {
            Pair(null, "")
        }
    }
}
