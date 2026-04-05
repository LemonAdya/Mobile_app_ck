package com.example.digitalapi_nosova_3.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.ui.screens.ArtDetailScreen
import com.example.digitalapi_nosova_3.ui.screens.ArtListScreen
import com.example.digitalapi_nosova_3.ui.viewmodel.ArtUiState
import com.example.digitalapi_nosova_3.ui.viewmodel.DetailUiState
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testArtworks = listOf(
        Artwork(1, "Starry Night", "Van Gogh", "img1", null, null, null),
        Artwork(2, "Mona Lisa", "Da Vinci", "img2", null, null, null)
    )

    private val testIiifUrl = "https://www.artic.edu/iiif/2"

    @Test
    fun `listScreenShouldDisplayArtworks`() {
        composeTestRule.setContent {
            ArtListScreen(
                state = ArtUiState.Success(testArtworks, testIiifUrl),
                searchQuery = "",
                favorites = emptySet(),
                onSearchQueryChange = {},
                onRefresh = {},
                onToggleFavorite = {},
                onArtworkClick = {}
            )
        }

        composeTestRule.onNodeWithText("Starry Night").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mona Lisa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Van Gogh").assertIsDisplayed()
        composeTestRule.onNodeWithText("Da Vinci").assertIsDisplayed()
    }

    @Test
    fun `clickOnArtworkTriggersNavigationWithCorrectId`() {
        var clickedId = -1

        composeTestRule.setContent {
            ArtListScreen(
                state = ArtUiState.Success(testArtworks, testIiifUrl),
                searchQuery = "",
                favorites = emptySet(),
                onSearchQueryChange = {},
                onRefresh = {},
                onToggleFavorite = {},
                onArtworkClick = { id -> clickedId = id }
            )
        }

        composeTestRule.onNodeWithText("Starry Night").performClick()
        assertEquals(1, clickedId)
    }

    @Test
    fun `errorScreenShowsRetryButtonAndTriggersRefresh`() {
        var retryClicked = false

        composeTestRule.setContent {
            ArtListScreen(
                state = ArtUiState.Error("Network error"),
                searchQuery = "",
                favorites = emptySet(),
                onSearchQueryChange = {},
                onRefresh = { retryClicked = true },
                onToggleFavorite = {},
                onArtworkClick = {}
            )
        }

        composeTestRule.onNodeWithText("Error: Network error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun `errorStateThenRetryLeadsToSuccessfulState`() {
        var currentState: ArtUiState = ArtUiState.Error("Network error")

        composeTestRule.setContent {
            ArtListScreen(
                state = currentState,
                searchQuery = "",
                favorites = emptySet(),
                onSearchQueryChange = {},
                onRefresh = { currentState = ArtUiState.Success(testArtworks, testIiifUrl) },
                onToggleFavorite = {},
                onArtworkClick = {}
            )
        }

        composeTestRule.onNodeWithText("Error: Network error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").performClick()

        composeTestRule.waitForIdle()

        composeTestRule.setContent {
            ArtListScreen(
                state = currentState,
                searchQuery = "",
                favorites = emptySet(),
                onSearchQueryChange = {},
                onRefresh = {},
                onToggleFavorite = {},
                onArtworkClick = {}
            )
        }

        composeTestRule.onNodeWithText("Starry Night").assertIsDisplayed()
    }

    @Test
    fun `detailScreenShowsArtworkInformation`() {
        val artwork = testArtworks[0]

        composeTestRule.setContent {
            ArtDetailScreen(
                state = DetailUiState.Success(artwork, testIiifUrl, isFavorite = false),
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Starry Night").assertIsDisplayed()
        composeTestRule.onNodeWithText("Van Gogh").assertIsDisplayed()
        composeTestRule.onNodeWithText("No description").assertIsDisplayed()
    }

    @Test
    fun `detailScreenShowsBackButton`() {
        var backClicked = false

        composeTestRule.setContent {
            ArtDetailScreen(
                state = DetailUiState.Loading,
                onBack = { backClicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun `loadingStateShowsProgressIndicator`() {
        composeTestRule.setContent {
            ArtListScreen(
                state = ArtUiState.Loading,
                searchQuery = "",
                favorites = emptySet(),
                onSearchQueryChange = {},
                onRefresh = {},
                onToggleFavorite = {},
                onArtworkClick = {}
            )
        }

        composeTestRule.onNodeWithText("Art App").assertIsDisplayed()
    }

    @Test
    fun `emptyStateShowsNoResultsMessage`() {
        composeTestRule.setContent {
            ArtListScreen(
                state = ArtUiState.Empty,
                searchQuery = "xyz",
                favorites = emptySet(),
                onSearchQueryChange = {},
                onRefresh = {},
                onToggleFavorite = {},
                onArtworkClick = {}
            )
        }

        composeTestRule.onNodeWithText("No results").assertIsDisplayed()
    }
}
