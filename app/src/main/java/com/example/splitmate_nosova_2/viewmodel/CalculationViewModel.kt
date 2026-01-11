package com.example.splitmate_nosova_2.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculationResult(
    val id: Int,
    val tipAmount: Double,
    val totalWithTip: Double,
    val perPerson: Double
)

class CalculationViewModel : ViewModel() {
    val billAmount = MutableStateFlow("")
    val numPeople = MutableStateFlow("")

    // Список для истории (бонусное задание) [cite: 24]
    private val _history = MutableStateFlow<List<CalculationResult>>(emptyList())
    val history = _history.asStateFlow()

    // Логика расчетов [cite: 16, 17, 18]
    fun getTipAmount(): Double = (billAmount.value.toDoubleOrNull() ?: 0.0) * 0.10
    fun getTotalWithTip(): Double = (billAmount.value.toDoubleOrNull() ?: 0.0) + getTipAmount()
    fun getPerPerson(): Double {
        val people = numPeople.value.toIntOrNull() ?: 1
        return getTotalWithTip() / if (people > 0) people else 1
    }

    // Сохранение в историю (максимум 5 записей)
    fun saveToHistory(id: Int) {
        val newResult = CalculationResult(
            id = id,
            tipAmount = getTipAmount(),
            totalWithTip = getTotalWithTip(),
            perPerson = getPerPerson()
        )
        _history.update { current ->
            (listOf(newResult) + current).take(5)
        }
    }

    fun resetData() {
        billAmount.value = ""
        numPeople.value = ""
    }
}
