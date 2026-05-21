package com.example.digitalapi_nosova_3.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.digitalapi_nosova_3.data.local.NoteEntity
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ArtDetailViewModelTest {

    private lateinit var repository: ArtRepository
    private lateinit var viewModel: ArtDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testArtwork = Artwork(
        id = 42,
        title = "Test Artwork",
        artistTitle = "Test Artist",
        imageId = "img42",
        description = "Test description",
        dateDisplay = "2024",
        mediumDisplay = "Oil"
    )

    private val testIiifUrl = "https://www.artic.edu/iiif/2"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        val savedStateHandle = SavedStateHandle(mapOf("id" to 42))
        every { repository.getFavoritesFlow() } returns flowOf(emptyList())
        coEvery { repository.getArtworkDetails(42) } returns Pair(testArtwork, testIiifUrl)
        coEvery { repository.getNoteByArtworkId(42) } returns null
        viewModel = ArtDetailViewModel(repository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        assertIs<DetailUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun `loadDetail success should set Success state`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<DetailUiState.Success>(state)
        assertEquals(testArtwork, state.artwork)
    }

    @Test
    fun `loadDetail error should set Error state`() = runTest {
        coEvery { repository.getArtworkDetails(42) } throws RuntimeException("Network error")

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<DetailUiState.Error>(state)
    }

    @Test
    fun `toggleFavorite should call repository`() = runTest {
        coEvery { repository.getArtworkDetails(42) } returns Pair(testArtwork, testIiifUrl)
        coEvery { repository.isFavorite(42) } returns false
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        coVerify { repository.toggleFavorite(testArtwork) }
    }

    @Test
    fun `saveNote should call repository`() = runTest {
        coEvery { repository.getArtworkDetails(42) } returns Pair(testArtwork, testIiifUrl)
        coEvery { repository.getNoteByArtworkId(42) } returns null
        advanceUntilIdle()

        viewModel.saveNote("My note")
        advanceUntilIdle()

        coVerify { repository.saveNote(42, "My note") }
    }

    @Test
    fun `deleteNote should call repository`() = runTest {
        coEvery { repository.getArtworkDetails(42) } returns Pair(testArtwork, testIiifUrl)
        coEvery { repository.getNoteByArtworkId(42) } returns null
        advanceUntilIdle()

        viewModel.deleteNote()
        advanceUntilIdle()

        coVerify { repository.deleteNote(42) }
    }

    @Test
    fun `addToCollection should call repository`() = runTest {
        viewModel.addToCollection(1L)
        advanceUntilIdle()

        coVerify { repository.addArtworkToCollection(1L, 42) }
    }

    @Test
    fun `removeFromCollection should call repository`() = runTest {
        viewModel.removeFromCollection(1L)
        advanceUntilIdle()

        coVerify { repository.removeArtworkFromCollection(1L, 42) }
    }

    @Test
    fun `retry should reload data`() = runTest {
        coEvery { repository.getArtworkDetails(42) } returns Pair(testArtwork, testIiifUrl)
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        coVerify(atLeast = 2) { repository.getArtworkDetails(42) }
    }
}
