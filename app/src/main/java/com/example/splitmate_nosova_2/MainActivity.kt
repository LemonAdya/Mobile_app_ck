package com.example.splitmate_nosova_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.splitmate_nosova_2.ui.screens.InputScreen
import com.example.splitmate_nosova_2.ui.screens.ResultScreen
import com.example.splitmate_nosova_2.viewmodel.CalculationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val calcViewModel: CalculationViewModel = viewModel()

            NavHost(navController = navController, startDestination = "input") {
                composable("input") {
                    InputScreen(
                        viewModel = calcViewModel,
                        onNavigateToResult = { id ->
                            navController.navigate("result/$id")
                        }
                    )
                }
                composable(
                    route = "result/{calcId}",
                    arguments = listOf(navArgument("calcId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("calcId") ?: 0L
                    ResultScreen(viewModel = calcViewModel, calcId = id)
                }
            }
        }
    }
}