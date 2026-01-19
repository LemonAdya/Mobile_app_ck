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
    onResultClick: (Long) -> Unit
) {
    val history by viewModel.history.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Кнопка назад и заголовок
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Text("История расчетов", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Text("История пока пуста", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn {

                items(history.takeLast(5).reversed()) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onResultClick(item.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Счет: ${item.bill} ₽", style = MaterialTheme.typography.bodyLarge)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Чаевые: ${String.format("%.2f", item.tip)} ₽")
                                Text(
                                    "Итого: ${String.format("%.2f", item.total)} ₽",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Text(
                                "С каждого: ${String.format("%.2f", item.perPerson)} ₽",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}