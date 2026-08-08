package com.intentpay.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intentpay.data.local.BudgetEntity
import com.intentpay.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: TransactionRepository) : ViewModel() {
    
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
                    reflectionThreshold = reflection,
                    googleDriveFolderId = _budget.value?.googleDriveFolderId
                )
            )
        }
    }

    fun updateSheetId(sheetId: String) {
        viewModelScope.launch {
            val current = _budget.value ?: return@launch
            repository.setBudgetConfig(current.copy(googleDriveFolderId = sheetId))
        }
    }

    suspend fun generateExportData(format: String): String {
        val txs = repository.allTransactions.first()
        return if (format.lowercase() == "json") {
            com.intentpay.utils.ExportManager.generateJson(txs)
        } else {
            com.intentpay.utils.ExportManager.generateCsv(txs)
        }
    }
}

class SettingsViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
