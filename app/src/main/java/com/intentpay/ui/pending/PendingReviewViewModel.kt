package com.intentpay.ui.pending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intentpay.categorization.MerchantMemory
import com.intentpay.data.local.TransactionEntity
import com.intentpay.data.repository.TransactionRepository
import com.intentpay.domain.models.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PendingReviewViewModel(
    private val repository: TransactionRepository,
    private val merchantMemory: MerchantMemory
) : ViewModel() {

    val pendingTransactions: StateFlow<List<TransactionEntity>> = repository.getUncategorized()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun categorize(transaction: TransactionEntity, category: Category) {
        viewModelScope.launch {
            // Update transaction
            val updated = transaction.copy(
                categoryName = category.name,
                needsReview = false
            )
            repository.updateTransaction(updated)
            
            // Teach merchant memory
            if (!transaction.merchant.isNullOrEmpty() && category != Category.UNCATEGORIZED) {
                merchantMemory.learn(transaction.merchant, category, true)
            }
        }
    }
}

class PendingReviewViewModelFactory(
    private val repository: TransactionRepository,
    private val merchantMemory: MerchantMemory
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PendingReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PendingReviewViewModel(repository, merchantMemory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
