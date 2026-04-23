package com.intentpay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE dateMillis >= :startDate AND dateMillis <= :endDate ORDER BY dateMillis DESC")
    fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE dateMillis >= :startDate AND dateMillis <= :endDate")
    fun getSumBetween(startDate: Long, endDate: Long): Flow<Double?>
    
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?
    
    // For manual transaction deduplication or updates
    @Query("SELECT * FROM transactions WHERE autoDetected = 1 AND amount = :amount AND dateMillis BETWEEN :startWindow AND :endWindow LIMIT 1")
    suspend fun findSimilarAutoDetected(amount: Double, startWindow: Long, endWindow: Long): TransactionEntity?
}
