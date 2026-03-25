package com.example.digitalapi_nosova_3.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.digitalapi_nosova_3.data.local.ArtEntity
import com.example.digitalapi_nosova_3.data.model.ApiConfig
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ArtViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ArtRepository
    private lateinit var viewModel: ArtViewModel

    private val testArtwork = Artwork(
        id = 1,
        title = "Test Art",
        artistTitle = "Test Artist",
        imageId = "test123",
        description = "Test Description",
        dateDisplay = "2020",
        mediumDisplay = "Oil on canvas"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        coEvery { repository.getFavoritesFlow() } returns flowOf(emptyList())
        viewModel = ArtViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Тест 1: Успешная загрузка данных
    @Test
    fun `loadArtworks should update state to Success when data is loaded`() = runTest {
        // Given
        val artworks = listOf(testArtwork)
        val iiifUrl = "https://example.com/iiif"
        coEvery { repository.getArtworks(any()) } returns Pair(artworks, iiifUrl)

        // When
        viewModel.loadArtworks()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState
        assertTrue(state is ArtUiState.Success)
        assertEquals(artworks, state.artworks)
        assertEquals(iiifUrl, state.iiifUrl)
    }

    // Тест 2: Обработка ошибки загрузки
    @Test
    fun `loadArtworks should update state to Error when exception occurs`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { repository.getArtworks(any()) } throws Exception(errorMessage)

        // When
        viewModel.loadArtworks()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState
        assertTrue(state is ArtUiState.Error)
        assertEquals(errorMessage, state.message)
    }

    // Тест 3: Retry после ошибки (нетривиальный тест)
    @Test
    fun `retry after error should initiate new request`() = runTest {
        // Given - первый запрос с ошибкой
        coEvery { repository.getArtworks(any()) } throws Exception("Network error")
        viewModel.loadArtworks()
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState is ArtUiState.Error)

        // When - повторный запрос с успехом
        val artworks = listOf(testArtwork)
        coEvery { repository.getArtworks(any()) } returns Pair(artworks, "https://example.com")
        viewModel.loadArtworks(forceRefresh = true)
        advanceUntilIdle()

        // Then - состояние изменилось на Success
        val state = viewModel.uiState
        assertTrue(state is ArtUiState.Success)
        assertEquals(artworks, state.artworks)
        coVerify(exactly = 2) { repository.getArtworks(any()) }
    }

    // Тест 4: Корректное начальное состояние
    @Test
    fun `initial state should be Loading`() {
        // Given & When - создание ViewModel
        val freshViewModel = ArtViewModel(repository)

        // Then
        assertTrue(freshViewModel.uiState is ArtUiState.Loading)
        assertEquals("", freshViewModel.searchQuery)
    }

    // Тест 5: Пустой результат должен давать Empty, а не Success
    @Test
    fun `empty result should give Empty state not Success with empty list`() = runTest {
        // Given
        coEvery { repository.getArtworks(any()) } returns Pair(emptyList(), "https://example.com")

        // When
        viewModel.loadArtworks()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState is ArtUiState.Empty)
        assertTrue(viewModel.uiState !is ArtUiState.Success)
    }

    // Тест 6: Поиск с пустым запросом должен загружать все артворки
    @Test
    fun `search with blank query should load all artworks`() = runTest {
        // Given
        val artworks = listOf(testArtwork)
        coEvery { repository.getArtworks(any()) } returns Pair(artworks, "https://example.com")

        // When
        viewModel.search("   ")
        advanceUntilIdle()

        // Then
        coVerify { repository.getArtworks(any()) }
        coVerify(exactly = 0) { repository.searchArtworks(any()) }
    }

    // Тест 7: Поиск с непустым запросом
    @Test
    fun `search with non-blank query should call searchArtworks`() = runTest {
        // Given
        val query = "Monet"
        val artworks = listOf(testArtwork)
        coEvery { repository.searchArtworks(query) } returns Pair(artworks, "https://example.com")

        // When
        viewModel.search(query)
        advanceUntilIdle()

        // Then
        assertEquals(query, viewModel.searchQuery)
        coVerify { repository.searchArtworks(query) }
        assertTrue(viewModel.uiState is ArtUiState.Success)
    }

    // Тест 8: Загрузка деталей артворка
    @Test
    fun `loadDetail should update detailUiState to Success`() = runTest {
        // Given
        val artId = 1
        coEvery { repository.getArtworkDetails(artId) } returns Pair(testArtwork, "https://example.com")
        coEvery { repository.isFavorite(artId) } returns false

        // When
        viewModel.loadDetail(artId)
        advanceUntilIdle()

        // Then
        val state = viewModel.detailUiState
        assertTrue(state is DetailUiState.Success)
        assertEquals(testArtwork, state.artwork)
        assertEquals(false, state.isFavorite)
    }

    // Тест 9: Toggle favorite обновляет состояние детального экрана
    @Test
    fun `toggleFavorite should update detail state isFavorite flag`() = runTest {
        // Given
        val artId = 1
        coEvery { repository.getArtworkDetails(artId) } returns Pair(testArtwork, "https://example.com")
        coEvery { repository.isFavorite(artId) } returns false
        coEvery { repository.toggleFavorite(any()) } returns Unit

        viewModel.loadDetail(artId)
        advanceUntilIdle()

        val initialState = viewModel.detailUiState as DetailUiState.Success
        assertEquals(false, initialState.isFavorite)

        // When
        viewModel.toggleFavorite(testArtwork)
        advanceUntilIdle()

        // Then
        val updatedState = viewModel.detailUiState as DetailUiState.Success
        assertEquals(true, updatedState.isFavorite)
        coVerify { repository.toggleFavorite(testArtwork) }
    }

    // Тест 10: Flow последовательность эмиссий favorites
    @Test
    fun `favorites flow should emit correct sequence`() = runTest {
        // Given
        val entity1 = ArtEntity(1, "Art 1", "Artist 1", "img1")
        val entity2 = ArtEntity(2, "Art 2", "Artist 2", "img2")
        
        val favoritesFlow = flowOf(
            emptyList(),
            listOf(entity1),
            listOf(entity1, entity2)
        )
        coEvery { repository.getFavoritesFlow() } returns favoritesFlow

        // When
        val vm = ArtViewModel(repository)

        // Then - проверяем полную последовательность эмиссий
        vm.favorites.test {
            assertEquals(emptyList(), awaitItem())
            assertEquals(listOf(entity1), awaitItem())
            assertEquals(listOf(entity1, entity2), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
