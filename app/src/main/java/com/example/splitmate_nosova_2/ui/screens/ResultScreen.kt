package com.example.splitmate_nosova_2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.splitmate_nosova_2.viewmodel.CalculationViewModel

@Composable
fun ResultScreen(viewModel: CalculationViewModel, onBackToEdit: () -> Unit, onNewCalculation: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Results:", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(10.dp))
        Text("Tip Amount: ${viewModel.getTipAmount()}") // [cite: 16]
        Text("Total with Tip: ${viewModel.getTotalWithTip()}") // [cite: 17]
        Text("Per Person: ${viewModel.getPerPerson()}") // [cite: 18]

        Spacer(modifier = Modifier.height(30.dp))

        Button(onClick = onBackToEdit, modifier = Modifier.fillMaxWidth()) { // [cite: 20]
            Text("Back to edit")
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(onClick = onNewCalculation, modifier = Modifier.fillMaxWidth()) { // [cite: 21]
            Text("New calculation")
        }
    }
}