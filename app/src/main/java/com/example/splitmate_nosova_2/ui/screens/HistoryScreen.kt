package com.example.splitmate_nosova_2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.splitmate_nosova_2.viewmodel.CalculationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CalculationViewModel,
    onBackClick: () -> Unit,
    onResultClick: (Long) -> Unit
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История расчетов") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (state.history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("История пока пуста")
                }
            } else {
                LazyColumn {
                    items(state.history.reversed()) { item ->
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
}