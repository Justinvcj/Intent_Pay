package com.intentpay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity): Int

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE dateMillis >= :startDate AND dateMillis <= :endDate ORDER BY dateMillis DESC")
    fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE dateMillis >= :startDate AND dateMillis <= :endDate AND isIncome = 0")
    fun getSumBetween(startDate: Long, endDate: Long): Flow<Double?>
    
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?
    
    // For manual transaction deduplication or updates
    @Query("SELECT * FROM transactions WHERE autoDetected = 1 AND amount = :amount AND dateMillis BETWEEN :startWindow AND :endWindow LIMIT 1")
    suspend fun findSimilarAutoDetected(amount: Double, startWindow: Long, endWindow: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE syncedWithSheets = 0 ORDER BY dateMillis ASC")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET syncedWithSheets = 1 WHERE id IN (:ids)")
    fun markAsSynced(ids: List<Long>)
}
