package com.intentpay.data.parser

object SmsParser {
    data class ParsedSms(val amount: Double, val merchant: String?, val upiRef: String?)

    fun parseUpiDebitMessage(messageBody: String): ParsedSms? {
        val lowerMsg = messageBody.lowercase()
        // Broad checks to avoid parsing irrelevant SMS
        if (!lowerMsg.contains("debited") && !lowerMsg.contains("sent") && !lowerMsg.contains("paid")) return null
        if (lowerMsg.contains("credited") || lowerMsg.contains("received")) return null // Ignore credits

        // Amount Regex to match Rs., INR, Rs, etc followed by digits
        val amountRegex = Regex("(?i)(?:rs\\.?|inr)\\s*([\\d,]+\\.?\\d*)")
        val amountMatch = amountRegex.find(lowerMsg)
        val amountStr = amountMatch?.groupValues?.get(1)?.replace(",", "") ?: return null
        val amount = amountStr.toDoubleOrNull() ?: return null

        // 12-digit UPI reference ID
        val upiRefRegex = Regex("(?i)upi(?:/|\\\\w+|\\\\s)+(?:ref|txn).{0,5}(\\d{12})")
        val upiMatch = upiRefRegex.find(lowerMsg) ?: Regex("(?i)upi ref no\\s*(\\d{12})").find(lowerMsg)
        val upiRef = upiMatch?.groupValues?.get(1)

        // Extrapolate merchant
        var merchant: String? = null
        val toMatch = Regex("(?i)(?:to)\\s+([a-zA-Z0-9@.-]+)").find(messageBody)
        if (toMatch != null) {
            merchant = toMatch.groupValues[1]
        } else {
            val paidMatch = Regex("(?i)paid to\\s+([^\\n\\.,]+)").find(messageBody)
            if (paidMatch != null) {
                merchant = paidMatch.groupValues[1]
            }
        }

        return ParsedSms(amount, merchant?.trim(), upiRef)
    }
}
