package com.example.digitalapi_nosova_3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.digitalapi_nosova_3.ui.screens.*
import com.example.digitalapi_nosova_3.ui.theme.DigitalAPI_Nosova_3Theme
import com.example.digitalapi_nosova_3.ui.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DigitalAPI_Nosova_3Theme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "list") {
                    composable("list") {
                        val viewModel: ArtListViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsState()
                        val searchQuery by viewModel.searchQuery.collectAsState()
                        val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()
                        val autoSync by viewModel.autoSync.collectAsState()

                        ArtListScreen(
                            state = uiState,
                            searchQuery = searchQuery,
                            showOnlyFavorites = showOnlyFavorites,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onToggleFavoritesFilter = { viewModel.toggleFavoritesFilter() },
                            onRefresh = { viewModel.refresh() },
                            onToggleFavorite = { artwork -> viewModel.toggleFavorite(artwork) },
                            onArtworkClick = { id -> navController.navigate("detail/$id") },
                            onNavigateToCollections = { navController.navigate("collections") },
                            onNavigateToHistory = { navController.navigate("history") },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }

                    composable(
                        route = "detail/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) {
                        val viewModel: ArtDetailViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsState()

                        ArtDetailScreen(
                            state = uiState,
                            onBack = { navController.popBackStack() },
                            onToggleFavorite = { viewModel.toggleFavorite() },
                            onRetry = { viewModel.retry() },
                            onSaveNote = { text -> viewModel.saveNote(text) },
                            onDeleteNote = { viewModel.deleteNote() }
                        )
                    }

                    composable("settings") {
                        val listViewModel: ArtListViewModel = hiltViewModel()
                        val theme by listViewModel.autoSync.collectAsState(initial = false)

                        SettingsScreen(
                            theme = "system",
                            autoSync = theme,
                            cacheTtlDays = 7,
                            onThemeChange = {},
                            onAutoSyncChange = { listViewModel.setAutoSync(it) },
                            onCacheTtlChange = {},
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("history") {
                        val listViewModel: ArtListViewModel = hiltViewModel()
                        val history by listViewModel.history.collectAsState()
                        val uiState by listViewModel.uiState.collectAsState()
                        val iiifUrl = (uiState as? ArtListUiState.Success)?.iiifUrl ?: "https://www.artic.edu/iiif/2"

                        HistoryScreen(
                            history = history,
                            iiifUrl = iiifUrl,
                            onArtworkClick = { id -> navController.navigate("detail/$id") },
                            onClearHistory = { listViewModel.clearHistory() },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("collections") {
                        val listViewModel: ArtListViewModel = hiltViewModel()
                        val collections by listViewModel.collections.collectAsState()

                        CollectionsScreen(
                            collections = collections,
                            onCollectionClick = { id -> navController.navigate("collection/$id") },
                            onCreateCollection = { name, desc -> listViewModel.createCollection(name, desc) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "collection/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val collectionId = backStackEntry.arguments?.getLong("id") ?: 0L
                        val listViewModel: ArtListViewModel = hiltViewModel()
                        val collections by listViewModel.collections.collectAsState()
                        val collection = collections.find { it.id == collectionId }
                        val uiState by listViewModel.uiState.collectAsState()
                        val iiifUrl = (uiState as? ArtListUiState.Success)?.iiifUrl ?: "https://www.artic.edu/iiif/2"

                        CollectionDetailScreen(
                            collection = collection,
                            artworks = emptyList(),
                            iiifUrl = iiifUrl,
                            onArtworkClick = { id -> navController.navigate("detail/$id") },
                            onRemoveArtwork = { artId -> listViewModel.removeFromCollection(collectionId, artId) },
                            onDeleteCollection = {
                                collection?.let { listViewModel.deleteCollection(it) }
                                navController.popBackStack()
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
