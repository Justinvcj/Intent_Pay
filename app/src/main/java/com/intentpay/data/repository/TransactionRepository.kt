package com.intentpay.data.repository

import com.intentpay.categorization.normalizeMerchantKey
import com.intentpay.data.local.BudgetDao
import com.intentpay.data.local.BudgetEntity
import com.intentpay.data.local.TransactionDao
import com.intentpay.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class TransactionRepository(
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

    suspend fun getUnsyncedTransactions(): List<TransactionEntity> {
        return transactionDao.getUnsyncedTransactions()
    }

    suspend fun markAsSynced(ids: List<Long>) = withContext(Dispatchers.IO) {
        transactionDao.markAsSynced(ids)
    }

    fun getUncategorized(): Flow<List<TransactionEntity>> = transactionDao.getUncategorized()
    
    fun getUncategorizedCount(): Flow<Int> = transactionDao.getUncategorizedCount()
    
    suspend fun getUncategorizedSince(since: Long) = transactionDao.getUncategorizedSince(since)
    
    suspend fun getUncategorizedBefore(before: Long) = transactionDao.getUncategorizedBefore(before)
    
    suspend fun incrementReminderCount(ids: List<Long>) = transactionDao.incrementReminderCount(ids)

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
        // DAO returns one auto-detected row with the same amount in the +/-120s window; tighten it here.
        val existing = transactionDao.findSimilarAutoDetected(
            amount = transaction.amount,
            startWindow = transaction.dateMillis - 120_000,
            endWindow = transaction.dateMillis + 120_000
        )
        if (existing == null || !isSameTransaction(existing, transaction)) {
            return transactionDao.insertTransaction(transaction)
        }
        return existing.id
    }

    private fun isSameTransaction(a: TransactionEntity, b: TransactionEntity): Boolean {
        val refA = a.upiRefId?.takeIf { it.isNotBlank() }
        val refB = b.upiRefId?.takeIf { it.isNotBlank() }
        if (refA != null && refB != null) return refA == refB
        val merchA = a.merchant?.let(::normalizeMerchantKey)?.takeIf { it.isNotEmpty() }
        val merchB = b.merchant?.let(::normalizeMerchantKey)?.takeIf { it.isNotEmpty() }
        if (merchA == null || merchB == null) return true // no merchant on a side: amount + window only
        return merchA == merchB
    }
}

internal const val DEDUP_WINDOW_MILLIS = 120_000L

/**
 * Pure duplicate check between two transactions. Same non-blank UPI ref is always a duplicate;
 * otherwise amount must match, timestamps must be within [windowMillis], and if both have a
 * merchant their normalized forms must match (a missing merchant falls back to a match).
 * Note: deduplicateAndInsert still pre-filters via the DAO (amount + window); not yet wired to this.
 */
internal fun isDuplicate(
    a: TransactionEntity,
    b: TransactionEntity,
    windowMillis: Long = DEDUP_WINDOW_MILLIS
): Boolean {
    if (!a.upiRefId.isNullOrBlank() && a.upiRefId == b.upiRefId) return true
    if (a.amount != b.amount) return false
    if (kotlin.math.abs(a.dateMillis - b.dateMillis) > windowMillis) return false
    val ma = a.merchant?.let { com.intentpay.categorization.normalizeMerchantKey(it) }.orEmpty()
    val mb = b.merchant?.let { com.intentpay.categorization.normalizeMerchantKey(it) }.orEmpty()
    if (ma.isNotEmpty() && mb.isNotEmpty() && ma != mb) return false
    return true
}
