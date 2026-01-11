package com.example.splitmate_nosova_2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.splitmate_nosova_2.viewmodel.CalculationViewModel

@Composable
fun HistoryScreen(
    viewModel: CalculationViewModel,
    onBackClick: () -> Unit,
    onResultClick: (Int) -> Unit
) {
    val history by viewModel.history.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Кнопка назад и заголовок
        Row {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
            Text("История (последние 5)", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(history) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onResultClick(item.id) } // Клик открывает результат [cite: 24]
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tip Amount: ${String.format("%.2f", item.tipAmount)}")
                        Text("Total with Tip: ${String.format("%.2f", item.totalWithTip)}")
                        Text("Per Person: ${String.format("%.2f", item.perPerson)}")
                    }
                }
            }
        }
    }
}