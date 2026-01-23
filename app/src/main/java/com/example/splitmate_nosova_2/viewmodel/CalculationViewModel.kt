package com.example.splitmate_nosova_2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class CalculationResult(
    val id: Long,
    val bill: Double,
    val people: Int,
    val tip: Double,
    val total: Double,
    val perPerson: Double
)

data class CalculationUiState(
    val billAmount: String = "",
    val numPeople: String = "",
    val history: List<CalculationResult> = emptyList()
)

class CalculationViewModel : ViewModel() {

    var uiState by mutableStateOf(CalculationUiState())
        private set

    fun onBillChange(input: String) {
        uiState = uiState.copy(billAmount = input)
    }

    fun onPeopleChange(input: String) {
        uiState = uiState.copy(numPeople = input)
    }

    fun isValid(): Boolean {
        val cleanBill = uiState.billAmount.trim().replace(',', '.')
        val cleanPeople = uiState.numPeople.trim()

        val bill = cleanBill.toDoubleOrNull() ?: 0.0
        val people = cleanPeople.toIntOrNull() ?: 0

        return bill > 0 && bill < 1_000_000 && people > 0 && people < 1000
    }

    fun calculate(): Long? {
        if (!isValid()) return null

        return try {
            val b = uiState.billAmount.trim().replace(',', '.').toDouble()
            val p = uiState.numPeople.trim().toInt()

            val tip = b * 0.10
            val total = b + tip
            val perPerson = total / p

            val newId = System.currentTimeMillis()
            val result = CalculationResult(newId, b, p, tip, total, perPerson)

            val updatedHistory = (uiState.history + result).takeLast(5)
            uiState = uiState.copy(history = updatedHistory)
            newId
        } catch (e: Exception) {
            null
        }
    }

    fun getResultById(id: Long): CalculationResult? {
        return uiState.history.find { it.id == id }
    }

    fun resetInputs() {
        uiState = uiState.copy(billAmount = "", numPeople = "")
    }
}