package com.intentpay.ui.reflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intentpay.data.local.TransactionEntity
import com.intentpay.data.repository.TransactionRepository
import com.intentpay.categorization.MerchantMemory
import com.intentpay.domain.models.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReflectionViewModel(
    private val repository: TransactionRepository,
    private val merchantMemory: MerchantMemory,
    private val transactionId: Long
) : ViewModel() {

    private val _transaction = MutableStateFlow<TransactionEntity?>(null)
    val transaction: StateFlow<TransactionEntity?> = _transaction

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    init {
        viewModelScope.launch {
            if (transactionId != -1L) {
                _transaction.value = repository.getTransactionById(transactionId)
            }
        }
    }

    fun saveReflection(purpose: String, category: Category) {
        val currentTx = _transaction.value ?: return
        
        viewModelScope.launch {
            repository.updateTransaction(
                currentTx.copy(
                    purpose = purpose.trim().takeIf { it.isNotBlank() },
                    categoryName = category.name,
                    needsReview = false
                )
            )
            
            if (currentTx.normalizedMerchant != null) {
                merchantMemory.learn(currentTx.normalizedMerchant, category, createdByUser = true)
            }
            
            _isSaved.value = true
        }
    }
    
    fun skipReflection() {
        val currentTx = _transaction.value ?: return
        viewModelScope.launch {
            repository.updateTransaction(
                currentTx.copy(
                    needsReview = true
                )
            )
            _isSaved.value = true
        }
    }
}

class ReflectionViewModelFactory(
    private val repository: TransactionRepository,
    private val merchantMemory: MerchantMemory,
    private val transactionId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReflectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReflectionViewModel(repository, merchantMemory, transactionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
