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
            learn(rawMerchant, keywordMatch, createdByUser = false)
            return keywordMatch
        }

        return Category.UNCATEGORIZED
    }

    suspend fun learn(rawMerchant: String?, category: Category, createdByUser: Boolean) {
        if (rawMerchant.isNullOrBlank()) return
        val normalized = normalize(rawMerchant)
        if (normalized.isEmpty()) return

        val rule = MerchantRuleEntity(
            merchantPattern = normalized,
            categoryName = category.name,
            timesUsed = 1,
            lastUsedMillis = System.currentTimeMillis(),
            createdByUser = createdByUser
        )
        merchantRuleDao.insertOrUpdate(rule)
        cache[normalized] = category
    }

    private fun normalize(merchant: String): String = normalizeMerchantKey(merchant)
}

private val NON_ALNUM = Regex("[^a-z0-9]")

/** Canonical merchant key used for both lookups and storage (also used by the DB migration). */
fun normalizeMerchantKey(merchant: String): String =
    merchant.trim().lowercase().replace(NON_ALNUM, "")
