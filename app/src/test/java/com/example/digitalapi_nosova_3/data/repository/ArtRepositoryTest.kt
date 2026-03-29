package com.example.digitalapi_nosova_3.data.repository

import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.local.ArtDao
import com.example.digitalapi_nosova_3.data.local.ArtEntity
import com.example.digitalapi_nosova_3.data.model.ApiConfig
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.model.ArtworkDetailResponse
import com.example.digitalapi_nosova_3.data.model.ArtworkResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArtRepositoryTest {

    private lateinit var api: ArtApiService
    private lateinit var dao: ArtDao
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
        dao = mockk(relaxed = true)
        repository = ArtRepository(api, dao)
    }

    @Test
    fun `getArtworks should return artworks and iiif url`() = runTest {
        val artworks = listOf(testArtwork)
        val response = ArtworkResponse(data = artworks, config = testConfig)
        coEvery { api.getArtworks(any(), any(), any()) } returns response

        val (data, url) = repository.getArtworks()

        assertEquals(artworks, data)
        assertEquals(testConfig.iiifUrl, url)
        coVerify { api.getArtworks(any(), any(), any()) }
    }

    @Test
    fun `getArtworks should use cache when forceRefresh is false`() = runTest {
        val artworks = listOf(testArtwork)
        val response = ArtworkResponse(data = artworks, config = testConfig)
        coEvery { api.getArtworks(any(), any(), any()) } returns response

        repository.getArtworks(forceRefresh = false)
        val (data, url) = repository.getArtworks(forceRefresh = false)

        assertEquals(artworks, data)
        coVerify(exactly = 1) { api.getArtworks(any(), any(), any()) }
    }

    @Test
    fun `getArtworks with forceRefresh should bypass cache`() = runTest {
        val artworks = listOf(testArtwork)
        val response = ArtworkResponse(data = artworks, config = testConfig)
        coEvery { api.getArtworks(any(), any(), any()) } returns response

        repository.getArtworks(forceRefresh = false)
        repository.getArtworks(forceRefresh = true)

        coVerify(exactly = 2) { api.getArtworks(any(), any(), any()) }
    }

    @Test
    fun `searchArtworks should return search results`() = runTest {
        val query = "Monet"
        val artworks = listOf(testArtwork)
        val response = ArtworkResponse(data = artworks, config = testConfig)
        coEvery { api.searchArtworks(query, any(), any()) } returns response

        val (data, url) = repository.searchArtworks(query)

        assertEquals(artworks, data)
        assertEquals(testConfig.iiifUrl, url)
        coVerify { api.searchArtworks(query, any(), any()) }
    }

    @Test
    fun `getArtworkDetails should return artwork details`() = runTest {
        val artId = 1
        val response = ArtworkDetailResponse(data = testArtwork, config = testConfig)
        coEvery { api.getArtworkDetails(artId, any()) } returns response

        val (data, url) = repository.getArtworkDetails(artId)

        assertEquals(testArtwork, data)
        assertEquals(testConfig.iiifUrl, url)
        coVerify { api.getArtworkDetails(artId, any()) }
    }

    @Test
    fun `isFavorite should return correct status`() = runTest {
        val artId = 1
        coEvery { dao.isFavorite(artId) } returns true

        val result = repository.isFavorite(artId)

        assertTrue(result)
        coVerify { dao.isFavorite(artId) }
    }

    @Test
    fun `toggleFavorite should add to favorites when not favorite`() = runTest {
        val artId = 1
        coEvery { dao.isFavorite(artId) } returns false

        repository.toggleFavorite(testArtwork)

        coVerify { dao.insert(any()) }
        coVerify(exactly = 0) { dao.deleteById(any()) }
    }

    @Test
    fun `toggleFavorite should remove from favorites when already favorite`() = runTest {
        val artId = 1
        coEvery { dao.isFavorite(artId) } returns true

        repository.toggleFavorite(testArtwork)

        coVerify { dao.deleteById(artId) }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `toggleFavorite twice should add then remove artwork`() = runTest {
        coEvery { dao.isFavorite(testArtwork.id) } returns false andThen true

        repository.toggleFavorite(testArtwork)
        repository.toggleFavorite(testArtwork)

        coVerify(exactly = 1) { dao.insert(any()) }
        coVerify(exactly = 1) { dao.deleteById(testArtwork.id) }
    }

    @Test
    fun `getFavoritesFlow should return flow from dao`() = runTest {
        val entities = listOf(
            ArtEntity(1, "Art 1", "Artist 1", "img1"),
            ArtEntity(2, "Art 2", "Artist 2", "img2")
        )
        coEvery { dao.getAllFavorites() } returns flowOf(entities)

        val flow = repository.getFavoritesFlow()

        coVerify { dao.getAllFavorites() }
    }
}