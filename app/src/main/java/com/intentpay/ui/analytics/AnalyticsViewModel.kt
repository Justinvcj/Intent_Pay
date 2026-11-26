package com.intentpay.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.intentpay.data.local.TransactionEntity
import com.intentpay.data.repository.TransactionRepository
import com.intentpay.domain.models.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class CategorySpend(
    val category: Category,
    val amount: Double,
    val percentage: Float
)

private data class MonthlySummary(
    val total: Double,
    val spends: List<CategorySpend>
)

class AnalyticsViewModel(repository: TransactionRepository) : ViewModel() {

    private val monthlySummary: StateFlow<MonthlySummary> = repository.allTransactions
        .map { summarizeCurrentMonth(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlySummary(0.0, emptyList()))

    val categorySpends: StateFlow<List<CategorySpend>> = monthlySummary
        .map { it.spends }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpent: StateFlow<Double> = monthlySummary
        .map { it.total }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private fun summarizeCurrentMonth(transactions: List<TransactionEntity>): MonthlySummary {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        val thisMonthExpenses = transactions.filter {
            it.dateMillis >= startOfMonth && !it.isIncome && it.categoryName != Category.UNCATEGORIZED.name
        }

        val total = thisMonthExpenses.sumOf { it.amount }
        if (total <= 0) return MonthlySummary(total, emptyList())

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

        return MonthlySummary(total, spends)
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
