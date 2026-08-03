package com.intentpay.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intentpay.data.repository.TransactionRepository
import com.intentpay.domain.models.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategorySpend(
    val category: Category,
    val amount: Double,
    val percentage: Float
)

class AnalyticsViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _categorySpends = MutableStateFlow<List<CategorySpend>>(emptyList())
    val categorySpends: StateFlow<List<CategorySpend>> = _categorySpends.asStateFlow()

    private val _totalSpent = MutableStateFlow(0.0)
    val totalSpent: StateFlow<Double> = _totalSpent.asStateFlow()

    init {
        loadMonthlyData()
    }

    private fun loadMonthlyData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val startOfMonth = calendar.timeInMillis

        viewModelScope.launch {
            repository.allTransactions.collectLatest { transactions ->
                val thisMonthExpenses = transactions.filter { 
                    it.dateMillis >= startOfMonth && !it.isIncome && it.categoryName != Category.UNCATEGORIZED.name
                }

                val total = thisMonthExpenses.sumOf { it.amount }
                _totalSpent.value = total

                if (total > 0) {
                    val grouped = thisMonthExpenses.groupBy { 
                        try { Category.valueOf(it.categoryName) } catch (e: Exception) { Category.OTHER }
                    }

                    val spends = grouped.map { (cat, txs) ->
                        val catTotal = txs.sumOf { it.amount }
                        CategorySpend(
                            category = cat,
                            amount = catTotal,
                            percentage = (catTotal / total).toFloat()
                        )
                    }.sortedByDescending { it.amount }

                    _categorySpends.value = spends
                } else {
                    _categorySpends.value = emptyList()
                }
            }
        }
    }
}

class AnalyticsViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
