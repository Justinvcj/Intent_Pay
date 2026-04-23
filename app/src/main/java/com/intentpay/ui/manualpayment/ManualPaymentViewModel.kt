package com.intentpay.ui.manualpayment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intentpay.data.local.TransactionEntity
import com.intentpay.data.repository.ExpenseRepository
import com.intentpay.domain.models.Category
import kotlinx.coroutines.launch

class ManualPaymentViewModel(private val repository: ExpenseRepository) : ViewModel() {
    fun recordPlannedTransaction(amount: Double, merchant: String, purpose: String, category: Category) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    merchant = merchant,
                    dateMillis = System.currentTimeMillis(),
                    purpose = purpose.trim(),
                    categoryName = category.name,
                    upiRefId = null,
                    autoDetected = false
                )
            )
        }
    }
}

class ManualPaymentViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManualPaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManualPaymentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
