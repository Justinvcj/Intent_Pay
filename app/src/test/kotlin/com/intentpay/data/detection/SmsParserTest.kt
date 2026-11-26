package com.intentpay.data.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class SmsParserTest {

    @Test
    fun debitMessageParsesAmountMerchantAndDirection() {
        val r = SmsParser.parseUpiMessage("Rs.1000 debited from A/c XX1234 to ZOMATO via UPI")!!
        assertEquals(1000.0, r.amount, 0.001)
        assertEquals("ZOMATO", r.merchant)
        assertFalse(r.isIncome)
    }

    @Test
    fun creditMessageIsIncome() {
        val r = SmsParser.parseUpiMessage("Rs.250 credited to your A/c XX1234")!!
        assertEquals(250.0, r.amount, 0.001)
        assertTrue(r.isIncome)
    }

    @Test
    fun amountWithRsPrefixAndCommas() {
        val r = SmsParser.parseUpiMessage("Rs.1,234.56 debited from A/c to SHOP")!!
        assertEquals(1234.56, r.amount, 0.001)
    }

    @Test
    fun amountWithInrPrefix() {
        val r = SmsParser.parseUpiMessage("INR 2,500 debited from A/c to SHOP")!!
        assertEquals(2500.0, r.amount, 0.001)
    }

    @Ignore("depends on C3: SmsParser amount regex has no rupee-symbol support yet")
    @Test
    fun amountWithRupeeSymbol() {
        val r = SmsParser.parseUpiMessage("₹99 debited from A/c to SHOP")!!
        assertEquals(99.0, r.amount, 0.001)
    }

    @Test
    fun upiRefIsCaptured() {
        val r = SmsParser.parseUpiMessage("Rs.100 debited from A/c to SHOP. UPI Ref No 123456789012")!!
        assertEquals("123456789012", r.upiRef)
    }

    @Test
    fun noUpiRefYieldsNull() {
        val r = SmsParser.parseUpiMessage("Rs.100 debited from A/c to SHOP")!!
        assertNull(r.upiRef)
    }

    // depends on C3 merchant-regex widening (currently only the first word is captured)
    @Ignore("depends on C3 merchant-regex widening")
    @Test
    fun multiWordMerchantIsNotTruncated() {
        val r = SmsParser.parseUpiMessage("Rs.500 debited from A/c XX1234 to DOMINOS PIZZA on 01-01")!!
        assertEquals("DOMINOS PIZZA", r.merchant)
    }

    @Test
    fun otpMessageReturnsNull() {
        assertNull(SmsParser.parseUpiMessage("Your OTP is 482913. Do not share it with anyone."))
    }

    @Test
    fun promoMessageReturnsNull() {
        assertNull(SmsParser.parseUpiMessage("Get 50% off on your next order. Use code SAVE50"))
    }

    @Test
    fun emptyBodyReturnsNull() {
        assertNull(SmsParser.parseUpiMessage(""))
        assertNull(SmsParser.parseUpiMessage("   "))
    }

    @Test
    fun upperCaseBodyMatchesLowerCaseBody() {
        val body = "Rs.750 debited from A/c to Swiggy. UPI Ref No 123456789012"
        val lower = SmsParser.parseUpiMessage(body.lowercase())
        val upper = SmsParser.parseUpiMessage(body.uppercase())
        assertNotNull(lower)
        assertNotNull(upper)
        assertEquals(lower!!.amount, upper!!.amount, 0.001)
        assertEquals(lower.isIncome, upper.isIncome)
        assertEquals(lower.upiRef, upper.upiRef)
        assertEquals(lower.merchant, upper.merchant!!.lowercase())
    }
}
