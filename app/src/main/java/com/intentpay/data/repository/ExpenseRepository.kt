package com.intentpay.data.repository

import com.intentpay.data.local.BudgetDao
import com.intentpay.data.local.BudgetEntity
import com.intentpay.data.local.TransactionDao
import com.intentpay.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val budgetDetails: Flow<BudgetEntity?> = budgetDao.getBudgetDetails()

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun setBudgetConfig(budget: BudgetEntity) {
        budgetDao.insertOrUpdateBudget(budget)
    }
    
    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }
    
    suspend fun getBudgetSync(): BudgetEntity? {
        return budgetDao.getBudgetDetailsSync()
    }

    fun getTodaySpending(): Flow<Double?> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1

        return transactionDao.getSumBetween(startOfDay, endOfDay)
    }

    fun getMonthSpending(): Flow<Double?> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis - 1

        return transactionDao.getSumBetween(startOfMonth, endOfMonth)
    }
    
    suspend fun deduplicateAndInsert(transaction: TransactionEntity): Long {
        // Find existing transaction with same amount within 2 minutes window
        val existing = transactionDao.findSimilarAutoDetected(
            amount = transaction.amount,
            startWindow = transaction.dateMillis - 120_000,
            endWindow = transaction.dateMillis + 120_000
        )
        if (existing == null) {
            return transactionDao.insertTransaction(transaction)
        }
        return existing.id
    }
}
