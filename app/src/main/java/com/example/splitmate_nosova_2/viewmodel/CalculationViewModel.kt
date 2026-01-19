package com.example.splitmate_nosova_2.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculationResult(
    val id: Long,
    val bill: Double,
    val people: Int,
    val tip: Double,
    val total: Double,
    val perPerson: Double
)

class CalculationViewModel : ViewModel() {

    private val _billAmount = MutableStateFlow("")
    val billAmount = _billAmount.asStateFlow()

    private val _numPeople = MutableStateFlow("")
    val numPeople = _numPeople.asStateFlow()

    private val _history = MutableStateFlow<List<CalculationResult>>(emptyList())
    val history = _history.asStateFlow()

    fun onBillChange(input: String) { _billAmount.value = input }
    fun onPeopleChange(input: String) { _numPeople.value = input }

    fun isValid(): Boolean {
        val cleanBill = _billAmount.value.trim().replace(',', '.')
        val cleanPeople = _numPeople.value.trim()

        val bill = cleanBill.toDoubleOrNull() ?: 0.0
        val people = cleanPeople.toIntOrNull() ?: 0

        return bill > 0 && bill < 1_000_000 && people > 0 && people < 1000
    }
    fun calculate(): Long {
        val b = _billAmount.value.trim().replace(',', '.').toDouble()
        val p = _numPeople.value.trim().toInt()

        val tip = b * 0.10
        val total = b + tip
        val perPerson = total / p

        val newId = System.currentTimeMillis()
        val result = CalculationResult(newId, b, p, tip, total, perPerson)

        _history.update { it + result }
        return newId
    }

    fun getResultById(id: Long): CalculationResult? {
        return _history.value.find { it.id == id }
    }
}