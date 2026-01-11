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
import com.example.splitmate_nosova_2.ui.screens.*
import com.example.splitmate_nosova_2.viewmodel.CalculationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val vm: CalculationViewModel = viewModel()

            NavHost(navController = navController, startDestination = "home") {
                // 1. Экран Home
                composable("home") {
                    HomeScreen(
                        onStartClick = { navController.navigate("input") },
                        onHistoryClick = { navController.navigate("history") }
                    )
                }

                composable("history") {
                    HistoryScreen(
                        viewModel = vm,
                        onBackClick = { navController.popBackStack() }, // Кнопка назад
                        onResultClick = { calcId ->
                            navController.navigate("result/$calcId")
                        }
                    )
                }
                // 2. Экран Input [cite: 8]
                composable("input") {
                    InputScreen(
                        viewModel = vm,
                        onCalculateClick = { amount, people ->
                            // Генерируем ID для аргумента
                            val calcId = (amount + people).toInt()
                            navController.navigate("result/$calcId")
                        }
                    )
                }
                // 3. Экран Result с аргументом
                composable(
                    route = "result/{calcId}",
                    arguments = listOf(navArgument("calcId") { type = NavType.IntType })
                ) {
                    ResultScreen(
                        viewModel = vm,
                        onBackToEdit = { navController.popBackStack() },
                        onNewCalculation = {
                            vm.resetData()
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}