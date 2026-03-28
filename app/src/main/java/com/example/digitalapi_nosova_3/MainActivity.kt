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
import com.example.digitalapi_nosova_3.ui.screens.ArtDetailScreen
import com.example.digitalapi_nosova_3.ui.screens.ArtListScreen
import com.example.digitalapi_nosova_3.ui.theme.DigitalAPI_Nosova_3Theme
import com.example.digitalapi_nosova_3.ui.viewmodel.ArtDetailViewModel
import com.example.digitalapi_nosova_3.ui.viewmodel.ArtListViewModel
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

                        ArtListScreen(
                            state = uiState,
                            searchQuery = searchQuery,
                            showOnlyFavorites = showOnlyFavorites,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onToggleFavoritesFilter = { viewModel.toggleFavoritesFilter() },
                            onRefresh = { viewModel.refresh() },
                            onToggleFavorite = { artwork -> viewModel.toggleFavorite(artwork) },
                            onArtworkClick = { id -> navController.navigate("detail/$id") }
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
                            onRetry = { viewModel.retry() }
                        )
                    }
                }
            }
        }
    }
}
