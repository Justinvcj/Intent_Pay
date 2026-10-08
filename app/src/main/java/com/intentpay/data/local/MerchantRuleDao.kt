package com.intentpay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface MerchantRuleDao {
    @Query("SELECT * FROM merchant_rules WHERE merchantPattern = :pattern LIMIT 1")
    suspend fun findRule(pattern: String): MerchantRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rule: MerchantRuleEntity): Long

    @Query("UPDATE merchant_rules SET timesUsed = timesUsed + 1, lastUsedMillis = :now WHERE merchantPattern = :pattern")
    suspend fun incrementUsage(pattern: String, now: Long): Int

    @Query("SELECT * FROM merchant_rules ORDER BY timesUsed DESC")
    fun getAllRules(): Flow<List<MerchantRuleEntity>>
}
