package com.intentpay.categorization

import com.intentpay.data.local.MerchantRuleDao
import com.intentpay.data.local.MerchantRuleEntity
import com.intentpay.domain.models.Category

class MerchantMemory(
    private val merchantRuleDao: MerchantRuleDao
) {
    // In-memory cache for fast lookups
    private val cache = mutableMapOf<String, Category>()

    suspend fun resolve(rawMerchant: String?): Category {
        if (rawMerchant.isNullOrBlank()) return Category.UNCATEGORIZED

        val normalized = normalize(rawMerchant)

        // 1. Check cache
        cache[normalized]?.let { return it }

        // 2. Check database
        val rule = merchantRuleDao.findRule(normalized)
        if (rule != null) {
            val category = try {
                Category.valueOf(rule.categoryName)
            } catch (e: IllegalArgumentException) {
                Category.UNCATEGORIZED
            }
            cache[normalized] = category
            merchantRuleDao.incrementUsage(normalized, System.currentTimeMillis())
            return category
        }

        // 3. Check keyword rules (built-in heuristics)
        val keywordMatch = KeywordRules.match(normalized)
        if (keywordMatch != null) {
            // Auto-save this as a rule
            learn(normalized, keywordMatch, createdByUser = false)
            return keywordMatch
        }

        return Category.UNCATEGORIZED
    }

    suspend fun learn(normalizedMerchant: String, category: Category, createdByUser: Boolean) {
        val rule = MerchantRuleEntity(
            merchantPattern = normalizedMerchant,
            categoryName = category.name,
            timesUsed = 1,
            lastUsedMillis = System.currentTimeMillis(),
            createdByUser = createdByUser
        )
        merchantRuleDao.insertOrUpdate(rule)
        cache[normalizedMerchant] = category
    }

    private fun normalize(merchant: String): String {
        return merchant.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
    }
}
