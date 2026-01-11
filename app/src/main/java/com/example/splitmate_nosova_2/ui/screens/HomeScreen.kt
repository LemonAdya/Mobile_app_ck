package com.example.splitmate_nosova_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onStartClick: () -> Unit, onHistoryClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "SplitMate", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onStartClick, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("Start")
        }
        // Бонусная кнопка истории
        OutlinedButton(onClick = onHistoryClick, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("History")
        }
    }
}