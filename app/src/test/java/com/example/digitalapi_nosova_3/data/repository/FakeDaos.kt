package com.example.digitalapi_nosova_3.data.repository

import com.example.digitalapi_nosova_3.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeArtDao : ArtDao {
    private val favorites = MutableStateFlow<List<ArtEntity>>(emptyList())

    override fun getAllFavorites(): Flow<List<ArtEntity>> = favorites
    override suspend fun insert(art: ArtEntity) { favorites.value = favorites.value + art }
    override suspend fun deleteById(id: Int) { favorites.value = favorites.value.filter { it.id != id } }
    override suspend fun isFavorite(id: Int): Boolean = favorites.value.any { it.id == id }
}

class FakeCollectionDao : CollectionDao {
    private val collections = MutableStateFlow<List<CollectionEntity>>(emptyList())
    private val crossRefs = mutableListOf<CollectionArtworkCrossRef>()

    override fun getAllCollections(): Flow<List<CollectionEntity>> = collections
    override suspend fun getCollectionById(id: Long): CollectionEntity? = collections.value.find { it.id == id }
    override suspend fun insert(collection: CollectionEntity): Long {
        val newId = (collections.value.maxOfOrNull { it.id } ?: 0L) + 1
        collections.value = collections.value + collection.copy(id = newId)
        return newId
    }
    override suspend fun update(collection: CollectionEntity) {
        collections.value = collections.value.map { if (it.id == collection.id) collection else it }
    }
    override suspend fun delete(collection: CollectionEntity) {
        collections.value = collections.value.filter { it.id != collection.id }
    }
    override fun getArtworkIdsInCollection(collectionId: Long): Flow<List<Int>> =
        MutableStateFlow(crossRefs.filter { it.collectionId == collectionId }.map { it.artworkId })
    override suspend fun addArtworkToCollection(crossRef: CollectionArtworkCrossRef) {
        if (crossRef !in crossRefs) crossRefs.add(crossRef)
    }
    override suspend fun removeArtworkFromCollection(collectionId: Long, artworkId: Int) {
        crossRefs.removeAll { it.collectionId == collectionId && it.artworkId == artworkId }
    }
    override suspend fun isArtworkInCollection(collectionId: Long, artworkId: Int): Boolean =
        crossRefs.any { it.collectionId == collectionId && it.artworkId == artworkId }
}

class FakeNoteDao : NoteDao {
    private val notes = mutableMapOf<Int, NoteEntity>()

    override suspend fun getNoteByArtworkId(artworkId: Int): NoteEntity? = notes[artworkId]
    override fun getAllNotes(): Flow<List<NoteEntity>> = MutableStateFlow(notes.values.toList())
    override suspend fun insert(note: NoteEntity) { notes[note.artworkId] = note }
    override suspend fun update(note: NoteEntity) { notes[note.artworkId] = note }
    override suspend fun deleteByArtworkId(artworkId: Int) { notes.remove(artworkId) }
    override suspend fun hasNote(artworkId: Int): Boolean = notes.containsKey(artworkId)
}

class FakeHistoryDao : HistoryDao {
    private val history = mutableListOf<HistoryEntity>()

    override fun getRecentHistory(limit: Int): Flow<List<HistoryEntity>> =
        MutableStateFlow(history.sortedByDescending { it.viewedAt }.take(limit))
    override fun getAllHistory(): Flow<List<HistoryEntity>> = MutableStateFlow(history.sortedByDescending { it.viewedAt })
    override suspend fun insert(history: HistoryEntity) { this.history.add(history) }
    override suspend fun clearAll() { history.clear() }
    override suspend fun deleteOlderThan(timestamp: Long) { history.removeAll { it.viewedAt < timestamp } }
}

class FakeCachedArtworkDao : CachedArtworkDao {
    private val cache = mutableMapOf<Int, CachedArtworkEntity>()

    override suspend fun getCachedArtwork(id: Int): CachedArtworkEntity? = cache[id]
    override fun getAllCachedArtworks(): Flow<List<CachedArtworkEntity>> = MutableStateFlow(cache.values.toList())
    override suspend fun insert(artwork: CachedArtworkEntity) { cache[artwork.id] = artwork }
    override suspend fun deleteById(id: Int) { cache.remove(id) }
    override suspend fun deleteOlderThan(timestamp: Long) { cache.entries.removeAll { it.value.cachedAt < timestamp } }
    override suspend fun clearAll() { cache.clear() }
    override suspend fun getCachedCount(): Int = cache.size
}
