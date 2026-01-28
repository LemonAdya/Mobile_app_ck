package com.example.digitalapi_nosova_3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.digitalapi_nosova_3.data.api.ArtApiService
import com.example.digitalapi_nosova_3.data.repository.ArtRepository
import com.example.digitalapi_nosova_3.ui.screens.ArtDetailScreen
import com.example.digitalapi_nosova_3.ui.screens.ArtListScreen
import com.example.digitalapi_nosova_3.ui.viewmodel.ArtViewModel
import com.example.digitalapi_nosova_3.ui.viewmodel.ArtViewModelFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.artic.edu/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ArtApiService::class.java)
        val repository = ArtRepository(apiService)
        val factory = ArtViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[ArtViewModel::class.java]

        setContent {
            val navController = rememberNavController()

            NavHost(navController, startDestination = "list") {
                composable("list") {
                    ArtListScreen(
                        state = viewModel.uiState,
                        searchQuery = viewModel.searchQuery,
                        favorites = viewModel.favorites,
                        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                        onRefresh = { viewModel.loadArtworks(true) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onArtworkClick = { id -> navController.navigate("detail/$id") }
                    )
                }
                composable(
                    "detail/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStack ->
                    val id = backStack.arguments?.getInt("id") ?: 0

                    LaunchedEffect(id) {
                        if (id > 0) {
                            viewModel.loadArtworkDetails(id)
                        }
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