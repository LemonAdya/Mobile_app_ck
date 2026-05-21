package com.example.digitalapi_nosova_3.data.sync

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.local.*
import com.example.digitalapi_nosova_3.data.model.ApiConfig
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.model.ArtworkResponse
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import com.example.digitalapi_nosova_3.data.repository.FakeArtDao
import com.example.digitalapi_nosova_3.data.repository.FakeCachedArtworkDao
import com.example.digitalapi_nosova_3.data.repository.FakeCollectionDao
import com.example.digitalapi_nosova_3.data.repository.FakeHistoryDao
import com.example.digitalapi_nosova_3.data.repository.FakeNoteDao
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SyncWorkerTest {

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
    fun `syncAllArtworks should cache artworks from API`() = runTest {
        val artworks = listOf(testArtwork1)
        val response = ArtworkResponse(data = artworks, config = ApiConfig(iiifUrl = "https://example.com"))
        coEvery { api.getArtworks() } returns response

        repository.syncAllArtworks()

        val cached = repository.getAllCachedArtworks().first()
        assertEquals(1, cached.size)
        assertEquals(testArtwork1.id, cached[0].id)
    }

    @Test
    fun `syncAllArtworks should throw on network error`() = runTest {
        coEvery { api.getArtworks() } throws RuntimeException("Network error")

        var threw = false
        try {
            repository.syncAllArtworks()
        } catch (e: Exception) {
            threw = true
        }
        assertEquals(true, threw)
    }
}
