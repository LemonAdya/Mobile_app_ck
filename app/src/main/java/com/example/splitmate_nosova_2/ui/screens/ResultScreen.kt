package com.example.splitmate_nosova_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.splitmate_nosova_2.viewmodel.CalculationViewModel

@Composable
fun ResultScreen(viewModel: CalculationViewModel, calcId: Long) {
    val result = viewModel.getResultById(calcId)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Итоги расчета", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        if (result != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Чек: ${result.bill} ₽")
                    Text("Людей: ${result.people}")
                    Text("Чаевые (10%): ${"%.2f".format(result.tip)} ₽")
                    Text("Всего: ${"%.2f".format(result.total)} ₽")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        "С каждого: ${"%.2f".format(result.perPerson)} ₽",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            Text("Ошибка: Расчет не найден", color = MaterialTheme.colorScheme.error)
        }
    }
}