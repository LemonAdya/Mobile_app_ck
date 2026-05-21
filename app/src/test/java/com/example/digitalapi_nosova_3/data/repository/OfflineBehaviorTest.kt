package com.example.digitalapi_nosova_3.data.repository

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.local.*
import com.example.digitalapi_nosova_3.data.model.ApiConfig
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.model.ArtworkResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineBehaviorTest {

    private lateinit var api: ArtApiService
    private lateinit var repository: ArtRepository

    private val testArtwork1 = Artwork(
        id = 1,
        title = "Starry Night",
        artistTitle = "Van Gogh",
        imageId = "img1",
        description = null,
        dateDisplay = null,
        mediumDisplay = null
    )

    private val testArtwork2 = Artwork(
        id = 2,
        title = "Mona Lisa",
        artistTitle = "Da Vinci",
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
    fun `getArtworks returns cached data when network fails`() = runTest {
        val cached1 = CachedArtworkEntity(
            id = 1, title = "Starry Night", artistTitle = "Van Gogh", imageId = "img1",
            description = null, dateDisplay = null, mediumDisplay = null,
            cachedAt = System.currentTimeMillis(), imageLocalPath = null
        )
        repository.cacheArtwork(testArtwork1, null)
        coEvery { api.getArtworks() } throws RuntimeException("No network")

        val (data, url) = repository.getArtworks()

        assertEquals(1, data.size)
        assertEquals("Starry Night", data[0].title)
    }

    @Test
    fun `searchArtworks filters cached data when network fails`() = runTest {
        repository.cacheArtwork(testArtwork1, null)
        repository.cacheArtwork(testArtwork2, null)
        coEvery { api.searchArtworks(any()) } throws RuntimeException("No network")

        val (data, url) = repository.searchArtworks("Van")

        assertEquals(1, data.size)
        assertEquals("Starry Night", data[0].title)
    }

    @Test
    fun `getArtworkDetails returns cached data when network fails`() = runTest {
        val cachedDetail = Artwork(
            id = 1, title = "Starry Night", artistTitle = "Van Gogh", imageId = "img1",
            description = "Full description", dateDisplay = "1889", mediumDisplay = "Oil on canvas"
        )
        repository.cacheArtwork(cachedDetail, null)
        coEvery { api.getArtworkDetails(1) } throws RuntimeException("No network")

        val (data, url) = repository.getArtworkDetails(1)

        assertEquals("Starry Night", data.title)
        assertEquals("Full description", data.description)
    }

    @Test
    fun `cached data persists across multiple offline calls`() = runTest {
        repository.cacheArtwork(testArtwork1, null)
        repository.cacheArtwork(testArtwork2, null)
        coEvery { api.getArtworks() } throws RuntimeException("No network")

        val (data1, _) = repository.getArtworks()
        val (data2, _) = repository.getArtworks()
        val (data3, _) = repository.getArtworks()

        assertEquals(2, data1.size)
        assertEquals(2, data2.size)
        assertEquals(2, data3.size)
    }

    @Test
    fun `offline search is case insensitive`() = runTest {
        repository.cacheArtwork(testArtwork1, null)
        coEvery { api.searchArtworks(any()) } throws RuntimeException("No network")

        val (data, _) = repository.searchArtworks("van")

        assertEquals(1, data.size)
    }

    @Test
    fun `clearExpiredCache removes old entries but keeps recent`() = runTest {
        val oldArtwork = CachedArtworkEntity(
            id = 1, title = "Old", artistTitle = "Artist", imageId = "img1",
            description = null, dateDisplay = null, mediumDisplay = null,
            cachedAt = 1000L, imageLocalPath = null
        )
        val newArtwork = CachedArtworkEntity(
            id = 2, title = "New", artistTitle = "Artist", imageId = "img2",
            description = null, dateDisplay = null, mediumDisplay = null,
            cachedAt = System.currentTimeMillis(), imageLocalPath = null
        )
        repository.cacheArtwork(testArtwork1.copy(id = 1), null)
        repository.cacheArtwork(testArtwork2.copy(id = 2), null)

        repository.clearExpiredCache(7)

        val cached = repository.getAllCachedArtworks().first()
        assertTrue(cached.isNotEmpty())
    }
}
