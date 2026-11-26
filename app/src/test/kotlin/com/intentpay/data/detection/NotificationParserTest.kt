package com.intentpay.data.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class NotificationParserTest {

    @Test
    fun amountInTitlePlusText() {
        val r = NotificationParser.parseBody("Payment successful Paid ₹1,250.50 to Swiggy")!!
        assertEquals(1250.50, r.amount, 0.001)
        assertFalse(r.isIncome)
    }

    @Test
    fun debitVsCreditDiscriminator() {
        val debit = NotificationParser.parseBody("You paid Rs. 40 to Chai Point")!!
        val credit = NotificationParser.parseBody("You received Rs. 40 from Rahul")!!
        assertFalse(debit.isIncome)
        assertTrue(credit.isIncome)
    }

    @Test
    fun sourceAppIsPassedThrough() {
        val r = NotificationParser.parseBody("Paid INR 10 to shop", sourceApp = "GPay")!!
        assertEquals("GPay", r.sourceApp)
    }

    @Test
    fun nonUpiNotificationReturnsNull() {
        assertNull(NotificationParser.parseBody("Your order has shipped. Track it now"))
    }

    @Test
    fun paymentWordWithoutAmountReturnsNull() {
        assertNull(NotificationParser.parseBody("Payment sent successfully"))
    }

    @Ignore("depends on C3: parse() must accept bigText")
    @Test
    fun amountOnlyInBigText() {
        // Needs the widened parse(title, text, bigText, summary, pkg) API from C3.
    }

    @Ignore("depends on C3: parse() must accept summaryText")
    @Test
    fun amountOnlyInSummaryText() {
        // Needs the widened parse(title, text, bigText, summary, pkg) API from C3.
    }
}
