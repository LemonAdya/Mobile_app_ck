package com.example.splitmate_nosova_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.splitmate_nosova_2.viewmodel.CalculationViewModel

@Composable
fun InputScreen(viewModel: CalculationViewModel, onNavigateToResult: (Long) -> Unit) {
    val bill by viewModel.billAmount.collectAsState()
    val people by viewModel.numPeople.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Разделение счета", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = bill,
            onValueChange = { viewModel.onBillChange(it) },
            label = { Text("Сумма чека") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = people,
            onValueChange = { viewModel.onPeopleChange(it) },
            label = { Text("Количество человек") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val id = viewModel.calculate()
                onNavigateToResult(id)
            },
            enabled = viewModel.isValid(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Рассчитать")
        }
    }
}