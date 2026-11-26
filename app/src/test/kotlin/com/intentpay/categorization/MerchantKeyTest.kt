package com.intentpay.categorization

import com.intentpay.domain.models.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pure-function tests for merchant normalization and keyword fallback (no DAO).
 * DAO-backed MerchantMemory behaviour is covered by the existing MerchantMemoryTest.
 */
class MerchantKeyTest {

    @Test
    fun normalizeStripsPunctuationAndSpaces() {
        assertEquals("zomatoltd", normalizeMerchantKey("  Zomato Ltd. "))
    }

    @Test
    fun normalizeIsCaseInsensitive() {
        assertEquals(normalizeMerchantKey("SWIGGY"), normalizeMerchantKey("swiggy"))
    }

    @Test
    fun normalizeKeepsDigits() {
        assertEquals("1mg", normalizeMerchantKey("1-MG"))
    }

    @Test
    fun normalizeOfSymbolsOnlyIsEmpty() {
        assertEquals("", normalizeMerchantKey("---"))
    }

    @Test
    fun keywordMatchFindsFood() {
        assertEquals(Category.FOOD, KeywordRules.match(normalizeMerchantKey("Dominos Pizza")))
    }

    @Test
    fun keywordMatchFindsTravelAndShopping() {
        assertEquals(Category.TRAVEL, KeywordRules.match("uberindia"))
        assertEquals(Category.SHOPPING, KeywordRules.match("amazonpay"))
    }

    @Test
    fun keywordMatchUnknownReturnsNull() {
        assertNull(KeywordRules.match("qqqqzzzz"))
    }
}
