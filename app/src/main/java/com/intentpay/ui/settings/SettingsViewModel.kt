package com.intentpay.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intentpay.data.local.BudgetEntity
import com.intentpay.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: ExpenseRepository) : ViewModel() {
    
    private val _budget = MutableStateFlow<BudgetEntity?>(null)
    val budget: StateFlow<BudgetEntity?> = _budget

    init {
        viewModelScope.launch {
            repository.budgetDetails.collect { 
                _budget.value = it 
            }
        }
    }

    fun updateBudget(monthly: Double, daily: Double, reflection: Double) {
        viewModelScope.launch {
            repository.setBudgetConfig(
                BudgetEntity(
                    id = 1,
                    monthlyBudget = monthly,
                    dailyLimit = daily,
                    reflectionThreshold = reflection
                )
            )
        }
    }
}

class SettingsViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
