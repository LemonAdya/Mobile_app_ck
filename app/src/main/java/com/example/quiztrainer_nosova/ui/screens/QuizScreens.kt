package com.example.quiztrainer_nosova.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quiztrainer_nosova.viewmodel.QuizViewModel

@Composable
fun QuizMainScreen(viewModel: QuizViewModel) {
    val state by viewModel.uiState.collectAsState()

    when {
        !state.isQuizStarted -> StartScreen(onStart = viewModel::startQuiz)
        state.isQuizFinished -> ResultScreen(
            score = state.score,
            total = viewModel.questions.size,
            onRestart = viewModel::restartQuiz
        )
        else -> QuestionScreen(
            question = viewModel.questions[state.currentQuestionIndex],
            currentIndex = state.currentQuestionIndex,
            totalQuestions = viewModel.questions.size,
            selectedAnswer = state.selectedAnswerIndex,
            onAnswerSelected = viewModel::selectAnswer,
            onNext = viewModel::nextQuestion
        )
    }
}

@Composable
fun StartScreen(onStart: () -> Unit) { // Экран приветствия
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Александр II: Освободитель", style = MaterialTheme.typography.headlineMedium)
        Text("Тест по биографии императора", Modifier.padding(16.dp))
        Button(onClick = onStart) { Text("Начать") }
    }
}

@Composable
fun QuestionScreen( // Экран вопроса [cite: 25]
    question: com.example.quiztrainer_nosova.viewmodel.Question,
    currentIndex: Int,
    totalQuestions: Int,
    selectedAnswer: Int?,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    Column(Modifier.padding(16.dp)) {
        Text("Вопрос ${currentIndex + 1} из $totalQuestions")
        Spacer(Modifier.height(16.dp))
        Text(question.text, style = MaterialTheme.typography.titleLarge)

        question.options.forEachIndexed { index, option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedAnswer == index, onClick = { onAnswerSelected(index) })
                Text(option)
            }
        }

        Button(
            onClick = onNext,
            enabled = selectedAnswer != null, // Кнопка "Дальше" только после выбора [cite: 32, 33]
            modifier = Modifier.align(Alignment.End)
        ) { Text("Дальше") }
    }
}

@Composable
fun ResultScreen(score: Int, total: Int, onRestart: () -> Unit) { // Экран результата [cite: 37]
    val percent = (score.toFloat() / total * 100).toInt()
    val comment = when { // Комментарии по диапазонам [cite: 42]
        percent < 50 -> "Нужно подучить историю!"
        percent <= 80 -> "Хороший результат!"
        else -> "Вы эксперт по эпохе Александра II!"
    }

    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Правильных ответов: $score из $total ($percent%)")
        Text(comment, Modifier.padding(16.dp))
        Button(onClick = onRestart) { Text("Пройти ещё раз") }
    }
}