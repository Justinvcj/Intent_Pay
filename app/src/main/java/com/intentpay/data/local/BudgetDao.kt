package com.intentpay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetEntity): Long

    @Query("SELECT * FROM budget_config WHERE id = 1 LIMIT 1")
    fun getBudgetDetails(): Flow<BudgetEntity?>
    
    @Query("SELECT * FROM budget_config WHERE id = 1 LIMIT 1")
    suspend fun getBudgetDetailsSync(): BudgetEntity?
}
