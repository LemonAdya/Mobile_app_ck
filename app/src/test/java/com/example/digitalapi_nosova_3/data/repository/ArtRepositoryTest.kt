package com.example.digitalapi_nosova_3.data.repository

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.local.*
import com.example.digitalapi_nosova_3.data.model.ApiConfig
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.model.ArtworkResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArtRepositoryTest {

    private lateinit var api: ArtApiService
    private lateinit var artDao: ArtDao
    private lateinit var collectionDao: CollectionDao
    private lateinit var noteDao: NoteDao
    private lateinit var historyDao: HistoryDao
    private lateinit var cachedArtworkDao: CachedArtworkDao
    private lateinit var repository: ArtRepository

    private val testArtwork = Artwork(
        id = 1,
        title = "Test Art",
        artistTitle = "Test Artist",
        imageId = "test123",
        description = null,
        dateDisplay = null,
        mediumDisplay = null
    )

    private val testConfig = ApiConfig(iiifUrl = "https://example.com/iiif")

    @Before
    fun setup() {
        api = mockk()
        artDao = mockk(relaxed = true)
        collectionDao = mockk(relaxed = true)
        noteDao = mockk(relaxed = true)
        historyDao = mockk(relaxed = true)
        cachedArtworkDao = mockk(relaxed = true)
        repository = ArtRepository(api, artDao, collectionDao, noteDao, historyDao, cachedArtworkDao)
    }

    @Test
    fun `getArtworks returns cached data when available`() = runTest {
        val cachedEntity = CachedArtworkEntity(
            id = 1, title = "Cached", artistTitle = "Artist", imageId = "img1",
            description = null, dateDisplay = null, mediumDisplay = null,
            cachedAt = System.currentTimeMillis(), imageLocalPath = null
        )
        coEvery { cachedArtworkDao.getAllCachedArtworks() } returns flowOf(listOf(cachedEntity))

        val (data, url) = repository.getArtworks()

        assertEquals(1, data.size)
        assertEquals("Cached", data[0].title)
        coVerify(exactly = 0) { api.getArtworks() }
    }

    @Test
    fun `getArtworks fetches from API when cache is empty`() = runTest {
        coEvery { cachedArtworkDao.getAllCachedArtworks() } returns flowOf(emptyList())
        val artworks = listOf(testArtwork)
        val response = ArtworkResponse(data = artworks, config = testConfig)
        coEvery { api.getArtworks() } returns response

        val (data, url) = repository.getArtworks()

        assertEquals(artworks, data)
        coVerify { api.getArtworks() }
    }

    @Test
    fun `getArtworks falls back to cache on network error`() = runTest {
        val cachedEntity = CachedArtworkEntity(
            id = 1, title = "Cached", artistTitle = "Artist", imageId = "img1",
            description = null, dateDisplay = null, mediumDisplay = null,
            cachedAt = System.currentTimeMillis(), imageLocalPath = null
        )
        coEvery { cachedArtworkDao.getAllCachedArtworks() } returns flowOf(listOf(cachedEntity))
        coEvery { api.getArtworks() } throws RuntimeException("Network error")

        val (data, url) = repository.getArtworks()

        assertEquals(1, data.size)
        assertEquals("Cached", data[0].title)
    }

    @Test
    fun `searchArtworks falls back to cached search on network error`() = runTest {
        val cachedEntity = CachedArtworkEntity(
            id = 1, title = "Monet Painting", artistTitle = "Monet", imageId = "img1",
            description = null, dateDisplay = null, mediumDisplay = null,
            cachedAt = System.currentTimeMillis(), imageLocalPath = null
        )
        coEvery { api.searchArtworks(any()) } throws RuntimeException("Network error")
        coEvery { cachedArtworkDao.getAllCachedArtworks() } returns flowOf(listOf(cachedEntity))

        val (data, url) = repository.searchArtworks("Monet")

        assertEquals(1, data.size)
        assertTrue(data[0].title?.contains("Monet") == true)
    }

    @Test
    fun `toggleFavorite adds when not favorite`() = runTest {
        coEvery { artDao.isFavorite(1) } returns false

        repository.toggleFavorite(testArtwork)

        coVerify { artDao.insert(any()) }
        coVerify(exactly = 0) { artDao.deleteById(any()) }
    }

    @Test
    fun `toggleFavorite removes when already favorite`() = runTest {
        coEvery { artDao.isFavorite(1) } returns true

        repository.toggleFavorite(testArtwork)

        coVerify { artDao.deleteById(1) }
        coVerify(exactly = 0) { artDao.insert(any()) }
    }

    @Test
    fun `createCollection inserts new collection`() = runTest {
        coEvery { collectionDao.insert(any()) } returns 1L

        val id = repository.createCollection("Test Collection", "Description")

        coVerify { collectionDao.insert(any()) }
        assertEquals(1L, id)
    }

    @Test
    fun `saveNote creates new note when none exists`() = runTest {
        coEvery { noteDao.getNoteByArtworkId(1) } returns null

        repository.saveNote(1, "My note")

        coVerify { noteDao.insert(any()) }
        coVerify(exactly = 0) { noteDao.update(any()) }
    }

    @Test
    fun `saveNote updates existing note`() = runTest {
        val existingNote = NoteEntity(1, "Old note", 1000L, 1000L)
        coEvery { noteDao.getNoteByArtworkId(1) } returns existingNote

        repository.saveNote(1, "Updated note")

        coVerify { noteDao.update(any()) }
        coVerify(exactly = 0) { noteDao.insert(any()) }
    }

    @Test
    fun `addToHistory inserts history entry`() = runTest {
        repository.addToHistory(testArtwork)

        coVerify { historyDao.insert(any()) }
    }

    @Test
    fun `cacheArtwork stores artwork in cache`() = runTest {
        repository.cacheArtwork(testArtwork, null)

        coVerify { cachedArtworkDao.insert(any()) }
    }

    @Test
    fun `getArtworks fetches from API when cache is expired (ttlDays)`() = runTest {
        val oldCached = CachedArtworkEntity(
            id = 1, title = "Old", artistTitle = "Artist", imageId = "img1",
            description = null, dateDisplay = null, mediumDisplay = null,
            cachedAt = 0L, imageLocalPath = null
        )
        coEvery { cachedArtworkDao.getAllCachedArtworks() } returns flowOf(listOf(oldCached))
        val artworks = listOf(testArtwork)
        val response = ArtworkResponse(data = artworks, config = testConfig)
        coEvery { api.getArtworks() } returns response

        val (data, url) = repository.getArtworks(forceRefresh = false, ttlDays = 1)

        assertEquals(artworks, data)
        coVerify { api.getArtworks() }
    }

    @Test
    fun `clearHistory calls clearAll`() = runTest {
        repository.clearHistory()

        coVerify { historyDao.clearAll() }
    }

    @Test
    fun `clearExpiredCache deletes old entries`() = runTest {
        repository.clearExpiredCache(7)

        coVerify { cachedArtworkDao.deleteOlderThan(any()) }
    }
}
