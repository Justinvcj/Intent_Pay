package com.intentpay.data.detection

import com.intentpay.categorization.MerchantMemory
import com.intentpay.data.local.TransactionEntity
import com.intentpay.data.repository.TransactionRepository
import com.intentpay.domain.models.Category

data class ParsedTransaction(
    val amount: Double,
    val merchant: String?,
    val isIncome: Boolean,
    val source: String,
    val upiRef: String? = null
)

class UnifiedTransactionDetector(
    private val transactionRepo: TransactionRepository,
    private val merchantMemory: MerchantMemory
) {
    suspend fun process(parsed: ParsedTransaction): Long {
        // 2. Auto-categorize via Merchant Memory
        val category = merchantMemory.resolve(parsed.merchant)
        val normalized = parsed.merchant?.trim()?.lowercase()?.replace(Regex("[^a-z0-9]"), "")

        // 3. Build entity
        val entity = TransactionEntity(
            amount = parsed.amount,
            merchant = parsed.merchant,
            normalizedMerchant = normalized,
            dateMillis = System.currentTimeMillis(),
            purpose = null,
            categoryName = category.name,
            upiRefId = parsed.upiRef,
            autoDetected = true,
            isIncome = parsed.isIncome,
            source = parsed.source,
            needsReview = (category == Category.UNCATEGORIZED && !parsed.isIncome),
            reminderCount = 0
        )

        // 4. Insert (deduplicateAndInsert checks for existing matching amount in 2min window)
        val id = transactionRepo.deduplicateAndInsert(entity)

        return id
    }
}
