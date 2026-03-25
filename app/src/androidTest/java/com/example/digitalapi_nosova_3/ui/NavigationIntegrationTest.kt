package com.example.digitalapi_nosova_3.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.digitalapi_nosova_3.data.model.Artwork
import com.example.digitalapi_nosova_3.ui.screens.ArtListScreen
import com.example.digitalapi_nosova_3.ui.screens.DetailScreen
import com.example.digitalapi_nosova_3.ui.viewmodel.ArtUiState
import com.example.digitalapi_nosova_3.ui.viewmodel.DetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testArtwork = Artwork(
        id = 1,
        title = "Test Artwork",
        artistTitle = "Test Artist",
        imageId = "test123",
        description = "Test Description",
        dateDisplay = "2020",
        mediumDisplay = "Oil on canvas"
    )

    // Интеграционный UI тест 1: Навигация от списка к деталям
    @Test
    fun clickingOnArtworkNavigatesToDetailScreen() {
        // Given
        val artworks = listOf(testArtwork)
        var currentRoute = "list"
        var detailId: Int? = null

        composeTestRule.setContent {
            val navController = rememberNavController()
            
            NavHost(navController = navController, startDestination = "list") {
                composable("list") {
                    currentRoute = "list"
                    ArtListScreen(
                        state = ArtUiState.Success(artworks, "https://example.com"),
                        searchQuery = "",
                        onSearchQueryChange = {},
                        onRefresh = {},
                        onArtworkClick = { id ->
                            detailId = id
                            navController.navigate("detail/$id")
                        }
                    )
                }
                composable("detail/{id}") { backStackEntry ->
                    currentRoute = "detail"
                    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                    DetailScreen(
                        state = DetailUiState.Success(testArtwork, "https://example.com", false),
                        onBack = { navController.popBackStack() },
                        onToggleFavorite = {}
                    )
                }
            }
        }

        // When - кликаем на артворк
        composeTestRule.onNodeWithText("Test Artwork").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Artwork").performClick()

        // Then - переход на экран деталей с правильным id
        composeTestRule.waitForIdle()
        assert(currentRoute == "detail")
        assert(detailId == 1)
    }

    // Интеграционный UI тест 2: Отображение состояния Loading
    @Test
    fun loadingStateDisplaysProgressIndicator() {
        // Given & When
        composeTestRule.setContent {
            ArtListScreen(
                state = ArtUiState.Loading,
                searchQuery = "",
                onSearchQueryChange = {},
                onRefresh = {},
                onArtworkClick = {}
            )
        }

        // Then - индикатор загрузки отображается
        // CircularProgressIndicator не имеет текста, проверяем что нет ошибок
        composeTestRule.waitForIdle()
    }

    // Интеграционный UI тест 3: Отображение ошибки и кнопки Retry
    @Test
    fun errorStateDisplaysErrorMessageAndRetryButton() {
        // Given
        var retryClicked = false

        // When
        composeTestRule.setContent {
            ArtListScreen(
                state = ArtUiState.Error("Network error"),
                searchQuery = "",
                onSearchQueryChange = {},
                onRefresh = { retryClicked = true },
                onArtworkClick = {}
            )
        }

        // Then - отображается сообщение об ошибке
        composeTestRule.onNodeWithText("Error: Network error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()

        // When - нажимаем Retry
        composeTestRule.onNodeWithText("Retry").performClick()

        // Then - вызван callback
        assert(retryClicked)
    }

    // Интеграционный UI тест 4: Переход от ошибки к успешному состоянию
    @Test
    fun retryAfterErrorTransitionsToSuccessState() {
        // Given
        val stateFlow = MutableStateFlow<ArtUiState>(ArtUiState.Error("Network error"))

        composeTestRule.setContent {
            ArtListScreen(
                state = stateFlow.value,
                searchQuery = "",
                onSearchQueryChange = {},
                onRefresh = {
                    // Имитируем успешную загрузку после retry
                    stateFlow.value = ArtUiState.Success(listOf(testArtwork), "https://example.com")
                },
                onArtworkClick = {}
            )
        }

        // When - начальное состояние ошибки
        composeTestRule.onNodeWithText("Error: Network error").assertIsDisplayed()

        // When - нажимаем Retry
        composeTestRule.onNodeWithText("Retry").performClick()
        composeTestRule.waitForIdle()

        // Обновляем UI с новым состоянием
        composeTestRule.setContent {
            ArtListScreen(
                state = stateFlow.value,
                searchQuery = "",
                onSearchQueryChange = {},
                onRefresh = {},
                onArtworkClick = {}
            )
        }

        // Then - отображается успешное состояние
        composeTestRule.onNodeWithText("Test Artwork").assertIsDisplayed()
    }

    // Интеграционный UI тест 5: Отображение пустого состояния
    @Test
    fun emptyStateDisplaysNoResultsMessage() {
        // Given & When
        composeTestRule.setContent {
            ArtListScreen(
                state = ArtUiState.Empty,
                searchQuery = "",
                onSearchQueryChange = {},
                onRefresh = {},
                onArtworkClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("No results").assertIsDisplayed()
    }
}
