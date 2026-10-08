package com.intentpay.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intentpay.data.local.TransactionEntity
import com.intentpay.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _groupedTransactions = MutableStateFlow<Map<String, List<TransactionEntity>>>(emptyMap())
    val groupedTransactions: StateFlow<Map<String, List<TransactionEntity>>> = _groupedTransactions.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            repository.allTransactions.collectLatest { transactions ->
                val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                
                val grouped = transactions
                    .sortedByDescending { it.dateMillis }
                    .groupBy { tx -> 
                        formatter.format(Date(tx.dateMillis)) 
                    }
                
                _groupedTransactions.value = grouped
            }
        }
    }
}

class HistoryViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
