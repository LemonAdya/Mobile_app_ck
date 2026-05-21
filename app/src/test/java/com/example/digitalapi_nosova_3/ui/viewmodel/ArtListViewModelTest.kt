package com.example.digitalapi_nosova_3.ui.viewmodel

import com.example.digitalapi_nosova_3.data.local.*
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.preferences.UserPreferencesRepository
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import com.example.digitalapi_nosova_3.data.sync.SyncScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ArtListViewModelTest {

    private lateinit var repository: ArtRepository
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var syncScheduler: SyncScheduler
    private lateinit var viewModel: ArtListViewModel
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
        preferencesRepository = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        every { repository.getFavoritesFlow() } returns flowOf(emptyList())
        every { repository.getAllCachedArtworks() } returns flowOf(emptyList())
        every { preferencesRepository.autoSyncFlow } returns flowOf(false)
        viewModel = ArtListViewModel(repository, preferencesRepository, syncScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        assertIs<ArtListUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun `updateSearchQuery should update search query`() {
        viewModel.updateSearchQuery("test")
        assertEquals("test", viewModel.searchQuery.value)
    }

    @Test
    fun `toggleFavoritesFilter should toggle showOnlyFavorites`() = runTest {
        assertEquals(false, viewModel.showOnlyFavorites.value)
        viewModel.toggleFavoritesFilter()
        advanceUntilIdle()
        assertEquals(true, viewModel.showOnlyFavorites.value)
    }

    @Test
    fun `setAutoSync true should schedule sync`() = runTest {
        viewModel.setAutoSync(true)
        advanceUntilIdle()
        verify { syncScheduler.scheduleSync() }
    }

    @Test
    fun `setAutoSync false should cancel sync`() = runTest {
        viewModel.setAutoSync(false)
        advanceUntilIdle()
        verify { syncScheduler.cancelSync() }
    }

    @Test
    fun `createCollection should call repository`() = runTest {
        viewModel.createCollection("Test", "Desc")
        advanceUntilIdle()
        coVerify { repository.createCollection("Test", "Desc") }
    }

    @Test
    fun `clearHistory should call repository`() = runTest {
        viewModel.clearHistory()
        advanceUntilIdle()
        coVerify { repository.clearHistory() }
    }

    @Test
    fun `toggleFavorite should call repository`() = runTest {
        viewModel.toggleFavorite(testArtwork)
        advanceUntilIdle()
        coVerify { repository.toggleFavorite(testArtwork) }
    }
}
