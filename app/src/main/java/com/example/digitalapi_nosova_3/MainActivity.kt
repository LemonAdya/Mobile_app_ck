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
import com.example.digitalapi_nosova_3.ui.viewmodel.ArtViewModel
import com.example.digitalapi_nosova_3.ui.viewmodel.ArtUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: ArtViewModel = hiltViewModel()

            val dbFavorites by viewModel.favorites.collectAsState()
            val favoriteIds = dbFavorites.map { it.id }.toSet()


            NavHost(navController = navController, startDestination = "list") {
                composable("list") {
                    ArtListScreen(
                        state = viewModel.uiState,
                        searchQuery = viewModel.searchQuery,
                        favorites = favoriteIds,
                        onSearchQueryChange = { viewModel.search(it) },
                        onRefresh = { viewModel.loadArtworks(true) },
                        onToggleFavorite = { id ->
                            val current = viewModel.uiState
                            if (current is ArtUiState.Success) {
                                current.artworks.find { it.id == id }?.let { viewModel.toggleFavorite(it) }
                            }
                        },
                        onArtworkClick = { id -> navController.navigate("detail/$id") }
                    )
                }


                composable(
                    route = "detail/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val idString = backStackEntry.arguments?.getString("id") ?: "0"
                    val id = idString.toInt()

                    LaunchedEffect(id) {
                        viewModel.loadDetail(id)
                    }

                    ArtDetailScreen(
                        state = viewModel.detailUiState,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}