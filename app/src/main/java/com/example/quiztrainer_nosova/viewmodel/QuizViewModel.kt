package com.example.quiztrainer_nosova.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 1. Модели данных
data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class QuizUiState(
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val selectedAnswerIndex: Int? = null,
    val isQuizFinished: Boolean = false,
    val isQuizStarted: Boolean = false
)

// 2. Логика приложения
class QuizViewModel : ViewModel() {
    val questions = listOf(
        Question("В каком году Александр II взошел на престол?", listOf("1825", "1855", "1861", "1881"), 1),
        Question("Какое прозвище получил император за свои реформы?", listOf("Миротворец", "Великий", "Освободитель"), 2),
        Question("Когда было отменено крепостное право?", listOf("1861", "1812", "1881"), 0),
        Question("Какую территорию Россия продала США в 1867 году?", listOf("Крым", "Аляску", "Курилы"), 1),
        Question("Кто организовал покушение на царя в 1881 году?", listOf("Декабристы", "Народная воля", "Большевики"), 1)
    )

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState = _uiState.asStateFlow()

    // Обработка событий (Events)
    fun startQuiz() { _uiState.update { it.copy(isQuizStarted = true) } }

    fun selectAnswer(index: Int) { _uiState.update { it.copy(selectedAnswerIndex = index) } }

    fun nextQuestion() {
        val state = _uiState.value
        val isCorrect = state.selectedAnswerIndex == questions[state.currentQuestionIndex].correctAnswerIndex
        val newScore = if (isCorrect) state.score + 1 else state.score

        if (state.currentQuestionIndex + 1 < questions.size) {
            _uiState.update { it.copy(
                currentQuestionIndex = state.currentQuestionIndex + 1,
                score = newScore,
                selectedAnswerIndex = null
            ) }
        } else {
            _uiState.update { it.copy(score = newScore, isQuizFinished = true) }
        }
    }

    fun restartQuiz() { _uiState.value = QuizUiState(isQuizStarted = true) }
}