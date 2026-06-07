package com.example.digitalapi_nosova_3.ui.viewmodel

import app.cash.turbine.test
import com.example.digitalapi_nosova_3.data.local.*
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.preferences.UserPreferencesRepository
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import com.example.digitalapi_nosova_3.data.sync.SyncScheduler
import io.mockk.coEvery
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

@OptIn(ExperimentalCoroutinesApi::class)
class ReactiveFlowTest {

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
        description = null,
        dateDisplay = null,
        mediumDisplay = null
    )

    private val testIiifUrl = "https://www.artic.edu/iiif/2"

    private val testArtworks = listOf(
        testArtwork,
        Artwork(2, "Art 2", "Artist 2", "img2", null, null, null)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        preferencesRepository = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        every { repository.getFavoritesFlow() } returns flowOf(emptyList())
        every { repository.getFavoriteArtworks() } returns flowOf(emptyList())
        every { repository.getAllCachedArtworks() } returns flowOf(emptyList())
        every { repository.getAllCollections() } returns flowOf(emptyList())
        every { repository.getRecentHistory(any()) } returns flowOf(emptyList())
        every { preferencesRepository.autoSyncFlow } returns flowOf(false)
        coEvery { repository.getArtworks(any(), any()) } returns Pair(testArtworks, testIiifUrl)
        coEvery { repository.searchArtworks(any()) } returns Pair(testArtworks, testIiifUrl)
        viewModel = ArtListViewModel(repository, preferencesRepository, syncScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `collections flow emits complete sequence`() = runTest {
        val collection1 = CollectionEntity(1, "Collection 1", null, 1000L)
        val collection2 = CollectionEntity(2, "Collection 2", null, 2000L)
        val collectionsFlow = MutableStateFlow<List<CollectionEntity>>(emptyList())
        every { repository.getAllCollections() } returns collectionsFlow

        val vm = ArtListViewModel(repository, preferencesRepository, syncScheduler)

        vm.collections.test {
            assertEquals(emptyList(), awaitItem())

            collectionsFlow.value = listOf(collection1)
            assertEquals(listOf(collection1), awaitItem())

            collectionsFlow.value = listOf(collection1, collection2)
            assertEquals(listOf(collection1, collection2), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `history flow emits complete sequence`() = runTest {
        val history1 = HistoryEntity(1, 1, "Art 1", "Artist 1", "img1", 1000L)
        val history2 = HistoryEntity(2, 2, "Art 2", "Artist 2", "img2", 2000L)
        val historyFlow = MutableStateFlow<List<HistoryEntity>>(emptyList())
        every { repository.getRecentHistory(any()) } returns historyFlow

        val vm = ArtListViewModel(repository, preferencesRepository, syncScheduler)

        vm.history.test {
            assertEquals(emptyList(), awaitItem())

            historyFlow.value = listOf(history1)
            assertEquals(listOf(history1), awaitItem())

            historyFlow.value = listOf(history2, history1)
            assertEquals(listOf(history2, history1), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `autoSync flow emits correct values`() = runTest {
        val autoSyncFlow = MutableStateFlow(false)
        every { preferencesRepository.autoSyncFlow } returns autoSyncFlow

        val vm = ArtListViewModel(repository, preferencesRepository, syncScheduler)

        vm.autoSync.test {
            assertEquals(false, awaitItem())

            autoSyncFlow.value = true
            assertEquals(true, awaitItem())

            autoSyncFlow.value = false
            assertEquals(false, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search query flow emits correct sequence`() = runTest {
        viewModel.updateSearchQuery("Monet")
        advanceUntilIdle()

        assertEquals("Monet", viewModel.searchQuery.value)

        viewModel.updateSearchQuery("Van Gogh")
        advanceUntilIdle()

        assertEquals("Van Gogh", viewModel.searchQuery.value)
    }

    @Test
    fun `showOnlyFavorites flow toggles correctly`() = runTest {
        assertEquals(false, viewModel.showOnlyFavorites.value)

        viewModel.toggleFavoritesFilter()
        advanceUntilIdle()
        assertEquals(true, viewModel.showOnlyFavorites.value)

        viewModel.toggleFavoritesFilter()
        advanceUntilIdle()
        assertEquals(false, viewModel.showOnlyFavorites.value)
    }

    @Test
    fun `favorites flow reflects in uiState favoriteIds`() = runTest {
        val artwork1 = Artwork(1, "Art 1", "Artist 1", "img1", null, null, null)
        val artwork2 = Artwork(2, "Art 2", "Artist 2", "img2", null, null, null)
        val favoritesFlow = MutableStateFlow<List<ArtEntity>>(emptyList())
        every { repository.getFavoritesFlow() } returns favoritesFlow
        coEvery { repository.getArtworks(any(), any()) } returns Pair(listOf(artwork1, artwork2), testIiifUrl)

        val vm = ArtListViewModel(repository, preferencesRepository, syncScheduler)
        advanceUntilIdle()

        vm.uiState.test {
            val initial = awaitItem()
            assertIs<ArtListUiState.Success>(initial)
            assertEquals(emptySet(), initial.favoriteIds)

            favoritesFlow.value = listOf(ArtEntity(1, "Art 1", "Artist 1", "img1"))
            advanceUntilIdle()
            val withOne = awaitItem()
            assertIs<ArtListUiState.Success>(withOne)
            assertEquals(setOf(1), withOne.favoriteIds)

            favoritesFlow.value = listOf(
                ArtEntity(1, "Art 1", "Artist 1", "img1"),
                ArtEntity(2, "Art 2", "Artist 2", "img2")
            )
            advanceUntilIdle()
            val withBoth = awaitItem()
            assertIs<ArtListUiState.Success>(withBoth)
            assertEquals(setOf(1, 2), withBoth.favoriteIds)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
