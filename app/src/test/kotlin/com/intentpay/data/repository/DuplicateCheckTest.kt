package com.intentpay.data.repository

import com.intentpay.data.local.TransactionEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicateCheckTest {

    private fun tx(
        amount: Double = 100.0,
        at: Long = 1_000_000L,
        merchant: String? = "Zomato",
        ref: String? = null
    ) = TransactionEntity(
        amount = amount, merchant = merchant, dateMillis = at, purpose = null,
        categoryName = "UNCATEGORIZED", upiRefId = ref, autoDetected = true
    )

    @Test
    fun sameAmount30sApartIsDuplicate() {
        assertTrue(isDuplicate(tx(), tx(at = 1_030_000L)))
    }

    @Test
    fun sameAmount3MinApartIsNotDuplicate() {
        assertFalse(isDuplicate(tx(), tx(at = 1_000_000L + 180_000L)))
    }

    @Test
    fun differentAmountIsNotDuplicate() {
        assertFalse(isDuplicate(tx(), tx(amount = 101.0, at = 1_010_000L)))
    }

    @Test
    fun sameUpiRefIsDuplicateRegardlessOfMerchantAndTime() {
        assertTrue(
            isDuplicate(
                tx(ref = "123456789012"),
                tx(merchant = "Other", amount = 5.0, at = 9_999_999L, ref = "123456789012")
            )
        )
    }

    @Test
    fun differentNormalizedMerchantIsNotDuplicate() {
        assertFalse(isDuplicate(tx(merchant = "Zomato"), tx(merchant = "Swiggy", at = 1_010_000L)))
    }

    @Test
    fun merchantFormattingDifferencesStillDuplicate() {
        assertTrue(isDuplicate(tx(merchant = "Zomato Ltd."), tx(merchant = "ZOMATO LTD", at = 1_010_000L)))
    }

    @Test
    fun missingMerchantFallsBackToDuplicate() {
        assertTrue(isDuplicate(tx(merchant = null), tx(merchant = "Zomato", at = 1_010_000L)))
    }
}
