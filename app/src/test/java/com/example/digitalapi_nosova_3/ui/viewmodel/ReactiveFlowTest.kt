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
    fun `favorites flow emits additions and removals`() = runTest {
        val entity1 = ArtEntity(1, "Art 1", "Artist 1", "img1")
        val entity2 = ArtEntity(2, "Art 2", "Artist 2", "img2")
        val favoritesFlow = MutableStateFlow<List<ArtEntity>>(emptyList())
        every { repository.getFavoritesFlow() } returns favoritesFlow

        val vm = ArtListViewModel(repository, preferencesRepository, syncScheduler)

        vm.favorites.test {
            assertEquals(emptyList(), awaitItem())

            favoritesFlow.value = listOf(entity1)
            assertEquals(listOf(entity1), awaitItem())

            favoritesFlow.value = listOf(entity1, entity2)
            assertEquals(listOf(entity1, entity2), awaitItem())

            favoritesFlow.value = listOf(entity2)
            assertEquals(listOf(entity2), awaitItem())

            favoritesFlow.value = emptyList()
            assertEquals(emptyList(), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
