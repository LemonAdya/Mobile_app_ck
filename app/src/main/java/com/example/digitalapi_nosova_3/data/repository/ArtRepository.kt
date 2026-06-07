package com.example.digitalapi_nosova_3.data.repository

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.local.*
import com.example.digitalapi_nosova_3.data.model.Artwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtRepository @Inject constructor(
    private val api: ArtApiService,
    private val artDao: ArtDao,
    private val collectionDao: CollectionDao,
    private val noteDao: NoteDao,
    private val historyDao: HistoryDao,
    private val cachedArtworkDao: CachedArtworkDao
) {
    private var cachedIiifUrl: String = "https://www.artic.edu/iiif/2"

    suspend fun getArtworks(
        forceRefresh: Boolean = false,
        ttlDays: Int = 7
    ): Pair<List<Artwork>, String> {
        val cachedList = cachedArtworkDao.getAllCachedArtworks().first()
        val expiryTime = System.currentTimeMillis() - (ttlDays * 24 * 60 * 60 * 1000L)

        if (!forceRefresh && cachedList.isNotEmpty()) {
            val isValid = cachedList.all { it.cachedAt >= expiryTime }
            if (isValid) {
                return Pair(cachedList.map { it.toArtwork() }, cachedIiifUrl)
            }
        }

        return try {
            val response = api.getArtworks()
            cachedIiifUrl = response.config.iiifUrl
            response.data.forEach { artwork ->
                cacheArtwork(artwork, null)
            }
            Pair(response.data, response.config.iiifUrl)
        } catch (e: Exception) {
            if (cachedList.isNotEmpty()) {
                Pair(cachedList.map { it.toArtwork() }, cachedIiifUrl)
            } else {
                throw e
            }
        }
    }

    suspend fun searchArtworks(query: String): Pair<List<Artwork>, String> {
        return try {
            val response = api.searchArtworks(query)
            cachedIiifUrl = response.config.iiifUrl
            response.data.forEach { artwork ->
                cacheArtwork(artwork, null)
            }
            Pair(response.data, response.config.iiifUrl)
        } catch (e: Exception) {
            val cached = cachedArtworkDao.getAllCachedArtworks().first()
            val filtered = cached.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.artistTitle?.contains(query, ignoreCase = true) == true
            }
            Pair(filtered.map { it.toArtwork() }, cachedIiifUrl)
        }
    }

    suspend fun getArtworkDetails(id: Int): Pair<Artwork, String> {
        val cached = cachedArtworkDao.getCachedArtwork(id)

        return try {
            val response = api.getArtworkDetails(id)
            cachedIiifUrl = response.config.iiifUrl
            cacheArtwork(response.data, null)
            Pair(response.data, response.config.iiifUrl)
        } catch (e: Exception) {
            if (cached != null) {
                Pair(cached.toArtwork(), cachedIiifUrl)
            } else {
                throw e
            }
        }
    }

    fun getFavoritesFlow(): Flow<List<ArtEntity>> = artDao.getAllFavorites()
    
    fun getFavoriteArtworks(): Flow<List<Artwork>> {
        return artDao.getAllFavorites().map { favorites ->
            favorites.mapNotNull { fav ->
                cachedArtworkDao.getCachedArtwork(fav.id)?.toArtwork()
            }
        }
    }
    
    suspend fun isFavorite(id: Int): Boolean = artDao.isFavorite(id)

    suspend fun toggleFavorite(art: Artwork) {
        if (artDao.isFavorite(art.id)) {
            artDao.deleteById(art.id)
        } else {
            artDao.insert(ArtEntity(art.id, art.title ?: "", art.artistTitle, art.imageId))
        }
    }

    fun getAllCollections(): Flow<List<CollectionEntity>> = collectionDao.getAllCollections()

    suspend fun getCollectionById(id: Long): CollectionEntity? = collectionDao.getCollectionById(id)

    suspend fun createCollection(name: String, description: String?): Long {
        return collectionDao.insert(
            CollectionEntity(name = name, description = description, createdAt = System.currentTimeMillis())
        )
    }

    suspend fun updateCollection(collection: CollectionEntity) = collectionDao.update(collection)

    suspend fun deleteCollection(collection: CollectionEntity) = collectionDao.delete(collection)

    fun getArtworkIdsInCollection(collectionId: Long): Flow<List<Int>> =
        collectionDao.getArtworkIdsInCollection(collectionId)

    fun getArtworksInCollection(collectionId: Long): Flow<List<Artwork>> {
        return collectionDao.getArtworksInCollection(collectionId).map { crossRefs ->
            crossRefs.map { crossRef ->
                Artwork(
                    id = crossRef.artworkId,
                    title = crossRef.title,
                    artistTitle = crossRef.artistTitle,
                    imageId = crossRef.imageId,
                    description = null,
                    dateDisplay = null,
                    mediumDisplay = null
                )
            }
        }
    }

    suspend fun addArtworkToCollection(collectionId: Long, artwork: Artwork) {
        collectionDao.addArtworkToCollection(
            CollectionArtworkCrossRef(
                collectionId = collectionId,
                artworkId = artwork.id,
                title = artwork.title ?: "",
                artistTitle = artwork.artistTitle,
                imageId = artwork.imageId
            )
        )
    }

    suspend fun removeArtworkFromCollection(collectionId: Long, artworkId: Int) {
        collectionDao.removeArtworkFromCollection(collectionId, artworkId)
    }

    suspend fun isArtworkInCollection(collectionId: Long, artworkId: Int): Boolean =
        collectionDao.isArtworkInCollection(collectionId, artworkId)

    suspend fun getNoteByArtworkId(artworkId: Int): NoteEntity? = noteDao.getNoteByArtworkId(artworkId)

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun saveNote(artworkId: Int, text: String) {
        val now = System.currentTimeMillis()
        val existing = noteDao.getNoteByArtworkId(artworkId)
        if (existing != null) {
            noteDao.update(existing.copy(text = text, updatedAt = now))
        } else {
            noteDao.insert(NoteEntity(artworkId, text, now, now))
        }
    }

    suspend fun deleteNote(artworkId: Int) = noteDao.deleteByArtworkId(artworkId)

    suspend fun hasNote(artworkId: Int): Boolean = noteDao.hasNote(artworkId)

    fun getRecentHistory(limit: Int = 50): Flow<List<HistoryEntity>> = historyDao.getRecentHistory(limit)

    fun getAllHistory(): Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun addToHistory(artwork: Artwork) {
        historyDao.insert(
            HistoryEntity(
                artworkId = artwork.id,
                title = artwork.title ?: "",
                artistTitle = artwork.artistTitle,
                imageId = artwork.imageId,
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistory() = historyDao.clearAll()

    fun getAllCachedArtworks(): Flow<List<CachedArtworkEntity>> = cachedArtworkDao.getAllCachedArtworks()

    suspend fun cacheArtwork(artwork: Artwork, imageLocalPath: String?) {
        cachedArtworkDao.insert(
            CachedArtworkEntity(
                id = artwork.id,
                title = artwork.title ?: "",
                artistTitle = artwork.artistTitle,
                imageId = artwork.imageId,
                description = artwork.description,
                dateDisplay = artwork.dateDisplay,
                mediumDisplay = artwork.mediumDisplay,
                cachedAt = System.currentTimeMillis(),
                imageLocalPath = imageLocalPath
            )
        )
    }

    suspend fun clearExpiredCache(ttlDays: Int) {
        val expiryTime = System.currentTimeMillis() - (ttlDays * 24 * 60 * 60 * 1000L)
        cachedArtworkDao.deleteOlderThan(expiryTime)
    }

    suspend fun clearAllCache() = cachedArtworkDao.clearAll()

    suspend fun getCachedCount(): Int = cachedArtworkDao.getCachedCount()

    suspend fun syncAllArtworks() {
        try {
            val response = api.getArtworks()
            cachedIiifUrl = response.config.iiifUrl
            response.data.forEach { artwork ->
                cacheArtwork(artwork, null)
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

private fun CachedArtworkEntity.toArtwork(): Artwork {
    return Artwork(
        id = id,
        title = title,
        artistTitle = artistTitle,
        imageId = imageId,
        description = description,
        dateDisplay = dateDisplay,
        mediumDisplay = mediumDisplay
    )
}
