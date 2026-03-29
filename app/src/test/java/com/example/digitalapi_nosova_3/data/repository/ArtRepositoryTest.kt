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

    // Тест 1: Успешная загрузка артворков
    @Test
    fun `getArtworks should return artworks and iiif url`() = runTest {
        // Given
        val artworks = listOf(testArtwork)
        val response = ArtworkResponse(data = artworks, config = testConfig)
        coEvery { api.getArtworks(any(), any(), any()) } returns response

        // When
        val (data, url) = repository.getArtworks()

        // Then
        assertEquals(artworks, data)
        assertEquals(testConfig.iiifUrl, url)
        coVerify { api.getArtworks(any(), any(), any()) }
    }

    // Тест 2: Кеширование работает корректно
    @Test
    fun `getArtworks should use cache when forceRefresh is false`() = runTest {
        // Given
        val artworks = listOf(testArtwork)
        val response = ArtworkResponse(data = artworks, config = testConfig)
        coEvery { api.getArtworks(any(), any(), any()) } returns response

        // When - первый запрос
        repository.getArtworks(forceRefresh = false)
        // второй запрос без forceRefresh
        val (data, url) = repository.getArtworks(forceRefresh = false)

        // Then - API вызван только один раз
        assertEquals(artworks, data)
        coVerify(exactly = 1) { api.getArtworks(any(), any(), any()) }
    }

    // Тест 3: ForceRefresh игнорирует кеш
    @Test
    fun `getArtworks with forceRefresh should bypass cache`() = runTest {
        // Given
        val artworks = listOf(testArtwork)
        val response = ArtworkResponse(data = artworks, config = testConfig)
        coEvery { api.getArtworks(any(), any(), any()) } returns response

        // When
        repository.getArtworks(forceRefresh = false)
        repository.getArtworks(forceRefresh = true)

        // Then - API вызван дважды
        coVerify(exactly = 2) { api.getArtworks(any(), any(), any()) }
    }

    // Тест 4: Поиск артворков
    @Test
    fun `searchArtworks should return search results`() = runTest {
        // Given
        val query = "Monet"
        val artworks = listOf(testArtwork)
        val response = ArtworkResponse(data = artworks, config = testConfig)
        coEvery { api.searchArtworks(query, any(), any()) } returns response

        // When
        val (data, url) = repository.searchArtworks(query)

        // Then
        assertEquals(artworks, data)
        assertEquals(testConfig.iiifUrl, url)
        coVerify { api.searchArtworks(query, any(), any()) }
    }

    // Тест 5: Получение деталей артворка
    @Test
    fun `getArtworkDetails should return artwork details`() = runTest {
        // Given
        val artId = 1
        val response = ArtworkDetailResponse(data = testArtwork, config = testConfig)
        coEvery { api.getArtworkDetails(artId, any()) } returns response

        // When
        val (data, url) = repository.getArtworkDetails(artId)

        // Then
        assertEquals(testArtwork, data)
        assertEquals(testConfig.iiifUrl, url)
        coVerify { api.getArtworkDetails(artId, any()) }
    }

    // Тест 6: Проверка избранного
    @Test
    fun `isFavorite should return correct status`() = runTest {
        // Given
        val artId = 1
        coEvery { dao.isFavorite(artId) } returns true

        // When
        val result = repository.isFavorite(artId)

        // Then
        assertTrue(result)
        coVerify { dao.isFavorite(artId) }
    }

    // Тест 7: Добавление в избранное (нетривиальный)
    @Test
    fun `toggleFavorite should add to favorites when not favorite`() = runTest {
        // Given
        val artId = 1
        coEvery { dao.isFavorite(artId) } returns false

        // When
        repository.toggleFavorite(testArtwork)

        // Then
        coVerify { dao.insert(any()) }
        coVerify(exactly = 0) { dao.deleteById(any()) }
    }

    // Тест 8: Удаление из избранного (нетривиальный)
    @Test
    fun `toggleFavorite should remove from favorites when already favorite`() = runTest {
        // Given
        val artId = 1
        coEvery { dao.isFavorite(artId) } returns true

        // When
        repository.toggleFavorite(testArtwork)

        // Then
        coVerify { dao.deleteById(artId) }
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    // Тест 9: Повторный toggle удаляет запись (нетривиальный)
    @Test
    fun `toggleFavorite twice should add then remove artwork`() = runTest {
        // Given
        coEvery { dao.isFavorite(testArtwork.id) } returns false andThen true

        // When - первый toggle добавляет
        repository.toggleFavorite(testArtwork)
        // второй toggle удаляет
        repository.toggleFavorite(testArtwork)

        // Then - первый раз insert, второй раз delete
        coVerify(exactly = 1) { dao.insert(any()) }
        coVerify(exactly = 1) { dao.deleteById(testArtwork.id) }
    }

    // Тест 10: getFavoritesFlow возвращает Flow
    @Test
    fun `getFavoritesFlow should return flow from dao`() = runTest {
        // Given
        val entities = listOf(
            ArtEntity(1, "Art 1", "Artist 1", "img1"),
            ArtEntity(2, "Art 2", "Artist 2", "img2")
        )
        coEvery { dao.getAllFavorites() } returns flowOf(entities)

        // When
        val flow = repository.getFavoritesFlow()

        // Then
        coVerify { dao.getAllFavorites() }
    }
}
