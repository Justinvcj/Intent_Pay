package com.intentpay.categorization

import com.intentpay.data.local.MerchantRuleDao
import com.intentpay.data.local.MerchantRuleEntity
import com.intentpay.domain.models.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantMemoryTest {

    private class FakeDao : MerchantRuleDao {
        val rows = mutableMapOf<String, MerchantRuleEntity>()
        override suspend fun findRule(pattern: String) = rows[pattern]
        override suspend fun insertOrUpdate(rule: MerchantRuleEntity): Long {
            rows[rule.merchantPattern] = rule
            return 1L
        }
        override suspend fun incrementUsage(pattern: String, now: Long): Int {
            val r = rows[pattern] ?: return 0
            rows[pattern] = r.copy(timesUsed = r.timesUsed + 1, lastUsedMillis = now)
            return 1
        }
        override fun getAllRules(): Flow<List<MerchantRuleEntity>> = flowOf(rows.values.toList())
    }

    @Test
    fun learnRawThenResolveDifferentFormattingMatches() = runBlocking {
        val dao = FakeDao()
        MerchantMemory(dao).learn("Zomato Ltd.", Category.FOOD, createdByUser = true)

        assertEquals(setOf("zomatoltd"), dao.rows.keys)
        // Fresh instance so the in-memory cache cannot mask a DB key mismatch.
        assertEquals(Category.FOOD, MerchantMemory(dao).resolve("zomato-ltd"))
    }

    @Test
    fun blankOrNullLearnIsNoOp() = runBlocking {
        val dao = FakeDao()
        val memory = MerchantMemory(dao)
        memory.learn(null, Category.FOOD, true)
        memory.learn("   ", Category.FOOD, true)
        memory.learn("---", Category.FOOD, true)
        assertTrue(dao.rows.isEmpty())
    }
}
