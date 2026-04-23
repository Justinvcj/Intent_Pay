package com.intentpay.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intentpay.data.local.BudgetEntity
import com.intentpay.data.local.TransactionEntity
import com.intentpay.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(repository: ExpenseRepository) : ViewModel() {

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetDetails: StateFlow<BudgetEntity?> = repository.budgetDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todaySpending: StateFlow<Double?> = repository.getTodaySpending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthSpending: StateFlow<Double?> = repository.getMonthSpending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}

class DashboardViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
