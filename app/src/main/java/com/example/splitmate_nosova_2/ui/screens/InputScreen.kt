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
fun InputScreen(viewModel: CalculationViewModel, onCalculateClick: (Double, Int) -> Unit) {
    val bill by viewModel.billAmount.collectAsState()
    val people by viewModel.numPeople.collectAsState()

    // Валидация: число > 0 [cite: 11, 13]
    val isValid = (bill.toDoubleOrNull() ?: 0.0) > 0.0 && (people.toIntOrNull() ?: 0) > 0

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        OutlinedTextField(
            value = bill,
            onValueChange = { viewModel.billAmount.value = it },
            label = { Text("Total Bill") }, // [cite: 10]
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = people,
            onValueChange = { viewModel.numPeople.value = it },
            label = { Text("Number of People") }, // [cite: 11]
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                val amountVal = bill.toDoubleOrNull() ?: 0.0
                val peopleVal = people.toIntOrNull() ?: 1
                val calcId = (amountVal * 100 + peopleVal).toInt() // Простая генерация ID

                viewModel.saveToHistory(calcId) // Сохраняем в список
                onCalculateClick(amountVal, peopleVal)
            },
            enabled = isValid, // [cite: 12, 13]
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }
    }
}