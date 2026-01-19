package com.example.digitalapi_nosova_3 // ПРОВЕРЬТЕ ИМЯ ПАКЕТА!

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity( ) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.artic.edu/api/v1/" )
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ArtApiService::class.java)

        val repository = ArtRepository(apiService)
        val viewModel = ArtViewModel(repository)

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "list"
            ) {
                composable("list") {
                    ArtListScreen(
                        viewModel = viewModel,
                        onArtworkClick = { id ->
                            navController.navigate("detail/$id")
                        }
                    )
                }

                composable(
                    route = "detail/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("id") ?: 0
                    ArtDetailScreen(
                        id = id,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
