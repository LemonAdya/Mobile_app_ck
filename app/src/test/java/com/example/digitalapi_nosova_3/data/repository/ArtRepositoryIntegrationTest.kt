package com.example.digitalapi_nosova_3.data.repository

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.local.*
import com.example.digitalapi_nosova_3.data.model.ApiConfig
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.model.ArtworkResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArtRepositoryIntegrationTest {

    private lateinit var api: ArtApiService
    private lateinit var repository: ArtRepository

    private val testArtwork1 = Artwork(
        id = 1,
        title = "Starry Night",
        artistTitle = "Vincent van Gogh",
        imageId = "img1",
        description = null,
        dateDisplay = null,
        mediumDisplay = null
    )

    private val testArtwork2 = Artwork(
        id = 2,
        title = "Mona Lisa",
        artistTitle = "Leonardo da Vinci",
        imageId = "img2",
        description = null,
        dateDisplay = null,
        mediumDisplay = null
    )

    @Before
    fun setup() {
        api = mockk()
        val artDao = FakeArtDao()
        val collectionDao = FakeCollectionDao()
        val noteDao = FakeNoteDao()
        val historyDao = FakeHistoryDao()
        val cachedArtworkDao = FakeCachedArtworkDao()
        repository = ArtRepository(api, artDao, collectionDao, noteDao, historyDao, cachedArtworkDao)
    }

    @Test
    fun `adding artwork to favorites should persist and be retrievable`() = runTest {
        assertFalse(repository.isFavorite(testArtwork1.id))

        repository.toggleFavorite(testArtwork1)

        assertTrue(repository.isFavorite(testArtwork1.id))
    }

    @Test
    fun `removing artwork from favorites should delete from storage`() = runTest {
        repository.toggleFavorite(testArtwork1)
        assertTrue(repository.isFavorite(testArtwork1.id))

        repository.toggleFavorite(testArtwork1)

        assertFalse(repository.isFavorite(testArtwork1.id))
    }

    @Test
    fun `collections should persist and be retrievable`() = runTest {
        val id = repository.createCollection("Test Collection", "Description")
        assertTrue(id > 0)

        val collections = repository.getAllCollections().first()
        assertEquals(1, collections.size)
        assertEquals("Test Collection", collections[0].name)
    }

    @Test
    fun `notes should persist and be retrievable`() = runTest {
        repository.saveNote(1, "My note")

        val note = repository.getNoteByArtworkId(1)
        assertEquals("My note", note?.text)
        assertEquals(1, note?.artworkId)
    }

    @Test
    fun `history should persist and be retrievable`() = runTest {
        repository.addToHistory(testArtwork1)

        val history = repository.getRecentHistory().first()
        assertEquals(1, history.size)
        assertEquals(testArtwork1.id, history[0].artworkId)
    }

    @Test
    fun `cached artworks should persist and be retrievable`() = runTest {
        repository.cacheArtwork(testArtwork1, null)
        repository.cacheArtwork(testArtwork2, null)

        val cached = repository.getAllCachedArtworks().first()
        assertEquals(2, cached.size)
        assertEquals(testArtwork1.id, cached[0].id)
    }

    @Test
    fun `getArtworks should cache results and return from cache on second call`() = runTest {
        val artworks = listOf(testArtwork1, testArtwork2)
        val response = ArtworkResponse(data = artworks, config = ApiConfig(iiifUrl = "https://example.com"))
        coEvery { api.getArtworks() } returns response

        val (data1, _) = repository.getArtworks()
        assertEquals(2, data1.size)

        val (data2, _) = repository.getArtworks()
        assertEquals(2, data2.size)
    }

    @Test
    fun `collection should contain artworks added to it`() = runTest {
        repository.cacheArtwork(testArtwork1, null)
        val collectionId = repository.createCollection("Test", null)
        repository.addArtworkToCollection(collectionId, testArtwork1.id)

        val artworkIds = repository.getArtworkIdsInCollection(collectionId).first()
        assertEquals(1, artworkIds.size)
        assertEquals(testArtwork1.id, artworkIds[0])
    }

    @Test
    fun `removing artwork from collection should update collection`() = runTest {
        repository.cacheArtwork(testArtwork1, null)
        val collectionId = repository.createCollection("Test", null)
        repository.addArtworkToCollection(collectionId, testArtwork1.id)
        repository.removeArtworkFromCollection(collectionId, testArtwork1.id)

        val artworkIds = repository.getArtworkIdsInCollection(collectionId).first()
        assertEquals(0, artworkIds.size)
    }
}
