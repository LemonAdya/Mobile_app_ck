package com.example.digitalapi_nosova_3.data.repository

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.local.ArtDao
import com.example.digitalapi_nosova_3.data.local.ArtEntity
import com.example.digitalapi_nosova_3.data.model.Artwork
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtRepository @Inject constructor(
    private val api: ArtApiService,
    private val dao: ArtDao
) {
    private var cachedArtworks: List<Artwork>? = null
    private var cachedIiifUrl: String? = null

    suspend fun getArtworks(forceRefresh: Boolean = false): Pair<List<Artwork>, String> {
        if (!forceRefresh && cachedArtworks != null) {
            return Pair(cachedArtworks!!, cachedIiifUrl ?: "")
        }
        val response = api.getArtworks()
        cachedArtworks = response.data
        cachedIiifUrl = response.config.iiifUrl
        return Pair(response.data, response.config.iiifUrl)
    }

    suspend fun searchArtworks(query: String): Pair<List<Artwork>, String> {
        val response = api.searchArtworks(query)
        return Pair(response.data, response.config.iiifUrl)
    }

    suspend fun getArtworkDetails(id: Int): Pair<Artwork, String> {
        val response = api.getArtworkDetails(id)
        return Pair(response.data, response.config.iiifUrl)
    }

    fun getFavoritesFlow(): Flow<List<ArtEntity>> = dao.getAllFavorites()
    suspend fun isFavorite(id: Int): Boolean = dao.isFavorite(id)

    suspend fun toggleFavorite(art: Artwork) {
        if (dao.isFavorite(art.id)) {
            dao.deleteById(art.id)
        } else {
            dao.insert(ArtEntity(art.id, art.title ?: "", art.artistTitle, art.imageId))
        }
    }
}
