package com.example.digitalapi_nosova_3.ui.viewmodel

import app.cash.turbine.test
import com.example.digitalapi_nosova_3.data.local.ArtEntity
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ArtViewModelTest {

    private lateinit var repository: ArtRepository
    private lateinit var viewModel: ArtViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testArtwork = Artwork(
        id = 1,
        title = "Test Art",
        artistTitle = "Test Artist",
        imageId = "img123",
        description = "Test Description",
        dateDisplay = "2024",
        mediumDisplay = "Oil on canvas"
    )

    private val testArtworks = listOf(
        testArtwork,
        Artwork(2, "Art 2", "Artist 2", "img2", null, null, null)
    )

    private val testIiifUrl = "https://www.artic.edu/iiif/2"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.getFavoritesFlow() } returns flowOf(emptyList())
        viewModel = ArtViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialStateShouldBeLoading`() {
        assertIs<ArtUiState.Loading>(viewModel.uiState)
    }

    @Test
    fun `initialDetailStateShouldBeLoading`() {
        assertIs<DetailUiState.Loading>(viewModel.detailUiState)
    }

    @Test
    fun `loadArtworksSuccessShouldSetSuccessState`() = runTest {
        coEvery { repository.getArtworks(any()) } returns Pair(testArtworks, testIiifUrl)

        viewModel.loadArtworks()
        advanceUntilIdle()

        val state = viewModel.uiState
        assertIs<ArtUiState.Success>(state)
        assertEquals(testArtworks, state.artworks)
        assertEquals(testIiifUrl, state.iiifUrl)
    }

    @Test
    fun `loadArtworksErrorShouldSetErrorState`() = runTest {
        val errorMsg = "Network error"
        coEvery { repository.getArtworks(any()) } throws RuntimeException(errorMsg)

        viewModel.loadArtworks()
        advanceUntilIdle()

        val state = viewModel.uiState
        assertIs<ArtUiState.Error>(state)
        assertEquals(errorMsg, state.message)
    }

    @Test
    fun `loadArtworksEmptyResultShouldSetEmptyStateNotSuccess`() = runTest {
        coEvery { repository.getArtworks(any()) } returns Pair(emptyList(), testIiifUrl)

        viewModel.loadArtworks()
        advanceUntilIdle()

        val state = viewModel.uiState
        assertIs<ArtUiState.Empty>(state)
    }

    @Test
    fun `retryAfterErrorShouldReloadArtworks`() = runTest {
        coEvery { repository.getArtworks(any()) } throws RuntimeException("Error")
        viewModel.loadArtworks()
        advanceUntilIdle()
        assertIs<ArtUiState.Error>(viewModel.uiState)

        coEvery { repository.getArtworks(any()) } returns Pair(testArtworks, testIiifUrl)
        viewModel.loadArtworks()
        advanceUntilIdle()

        val state = viewModel.uiState
        assertIs<ArtUiState.Success>(state)
        assertEquals(testArtworks, state.artworks)
        coVerify(exactly = 2) { repository.getArtworks(any()) }
    }

    @Test
    fun `loadDetailSuccessShouldSetDetailUiStateSuccess`() = runTest {
        coEvery { repository.getArtworkDetails(1) } returns Pair(testArtwork, testIiifUrl)
        coEvery { repository.isFavorite(1) } returns false

        viewModel.loadDetail(1)
        advanceUntilIdle()

        val state = viewModel.detailUiState
        assertIs<DetailUiState.Success>(state)
        assertEquals(testArtwork, state.artwork)
        assertEquals(testIiifUrl, state.iiifUrl)
        assertTrue(!state.isFavorite)
    }

    @Test
    fun `loadDetailErrorShouldSetDetailUiStateError`() = runTest {
        val errorMsg = "Not found"
        coEvery { repository.getArtworkDetails(1) } throws RuntimeException(errorMsg)

        viewModel.loadDetail(1)
        advanceUntilIdle()

        val state = viewModel.detailUiState
        assertIs<DetailUiState.Error>(state)
        assertEquals(errorMsg, state.message)
    }

    @Test
    fun `searchWithBlankQueryShouldLoadArtworks`() = runTest {
        coEvery { repository.getArtworks(any()) } returns Pair(testArtworks, testIiifUrl)

        viewModel.search("")
        advanceUntilIdle()

        val state = viewModel.uiState
        assertIs<ArtUiState.Success>(state)
        coVerify { repository.getArtworks(any()) }
    }

    @Test
    fun `searchWithQueryShouldSearchArtworks`() = runTest {
        coEvery { repository.searchArtworks("Monet") } returns Pair(testArtworks, testIiifUrl)

        viewModel.search("Monet")
        advanceUntilIdle()

        val state = viewModel.uiState
        assertIs<ArtUiState.Success>(state)
        assertEquals(testArtworks, state.artworks)
        coVerify { repository.searchArtworks("Monet") }
    }

    @Test
    fun `searchEmptyResultShouldSetEmptyState`() = runTest {
        coEvery { repository.searchArtworks("xyznonexistent") } returns Pair(emptyList(), testIiifUrl)

        viewModel.search("xyznonexistent")
        advanceUntilIdle()

        assertIs<ArtUiState.Empty>(viewModel.uiState)
    }

    @Test
    fun `toggleFavoriteShouldUpdateDetailStateIsFavorite`() = runTest {
        coEvery { repository.getArtworkDetails(1) } returns Pair(testArtwork, testIiifUrl)
        coEvery { repository.isFavorite(1) } returns false

        viewModel.loadDetail(1)
        advanceUntilIdle()

        val beforeToggle = viewModel.detailUiState
        assertIs<DetailUiState.Success>(beforeToggle)
        assertTrue(!beforeToggle.isFavorite)

        viewModel.toggleFavorite(testArtwork)
        advanceUntilIdle()

        val afterToggle = viewModel.detailUiState
        assertIs<DetailUiState.Success>(afterToggle)
        assertTrue(afterToggle.isFavorite)
    }

    @Test
    fun `searchErrorShouldSetErrorState`() = runTest {
        coEvery { repository.searchArtworks(any()) } throws RuntimeException("Search failed")

        viewModel.search("test")
        advanceUntilIdle()

        val state = viewModel.uiState
        assertIs<ArtUiState.Error>(state)
    }

    @Test
    fun `favoritesFlowEmitsCompleteSequenceOfAdditions`() = runTest {
        val entity1 = ArtEntity(1, "Art 1", "Artist 1", "img1")
        val entity2 = ArtEntity(2, "Art 2", "Artist 2", "img2")
        val favoritesStateFlow = MutableStateFlow<List<ArtEntity>>(emptyList())
        every { repository.getFavoritesFlow() } returns favoritesStateFlow

        val vm = ArtViewModel(repository)

        vm.favorites.test {
            assertEquals(emptyList(), awaitItem())

            favoritesStateFlow.value = listOf(entity1)
            assertEquals(listOf(entity1), awaitItem())

            favoritesStateFlow.value = listOf(entity1, entity2)
            assertEquals(listOf(entity1, entity2), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favoritesFlowEmitsCompleteSequenceOfAdditionsAndRemovals`() = runTest {
        val entity1 = ArtEntity(1, "Art 1", "Artist 1", "img1")
        val entity2 = ArtEntity(2, "Art 2", "Artist 2", "img2")
        val entity3 = ArtEntity(3, "Art 3", "Artist 3", "img3")
        val favoritesStateFlow = MutableStateFlow<List<ArtEntity>>(emptyList())
        every { repository.getFavoritesFlow() } returns favoritesStateFlow

        val vm = ArtViewModel(repository)

        vm.favorites.test {
            assertEquals(emptyList(), awaitItem())

            favoritesStateFlow.value = listOf(entity1, entity2, entity3)
            assertEquals(listOf(entity1, entity2, entity3), awaitItem())

            favoritesStateFlow.value = listOf(entity1, entity3)
            assertEquals(listOf(entity1, entity3), awaitItem())

            favoritesStateFlow.value = listOf(entity1)
            assertEquals(listOf(entity1), awaitItem())

            favoritesStateFlow.value = emptyList()
            assertEquals(emptyList(), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favoritesFlowInitialValueIsEmptyList`() = runTest {
        every { repository.getFavoritesFlow() } returns flowOf(emptyList())

        val vm = ArtViewModel(repository)

        assertEquals(emptyList(), vm.favorites.value)
    }
}
