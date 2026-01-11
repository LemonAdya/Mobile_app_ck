package com.example.quiztrainer_nosova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quiztrainer_nosova.ui.screens.QuizMainScreen
import com.example.quiztrainer_nosova.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Инициализация State Holder
            val quizViewModel: QuizViewModel = viewModel()
            // Запуск UI
            QuizMainScreen(viewModel = quizViewModel)
        }
    }
}