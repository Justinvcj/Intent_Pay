package com.intentpay.data.detection

import android.util.Log

object NotificationParser {
    data class ParsedNotification(
        val amount: Double,
        val merchant: String?,
        val isIncome: Boolean,
        val sourceApp: String // e.g., "GPay", "PhonePe", "Paytm"
    )

    private const val TAG = "NotificationParser"

    fun parse(title: String, text: String, packageName: String): ParsedNotification? {
        val combinedText = "$title $text".lowercase()
        Log.d(TAG, "Parsing notification from $packageName: $combinedText")

        val sourceApp = when (packageName) {
            "com.google.android.apps.nbu.paisa.user" -> "GPay"
            "com.phonepe.app" -> "PhonePe"
            "net.one97.paytm" -> "Paytm"
            "in.org.npci.upiapp" -> "BHIM"
            else -> "Unknown UPI"
        }

        val isDebit = combinedText.contains("paid") || combinedText.contains("sent") || combinedText.contains("debited")
        val isCredit = combinedText.contains("received") || combinedText.contains("credited")

        if (!isDebit && !isCredit) {
            Log.d(TAG, "Notification is neither debit nor credit.")
            return null
        }

        val isIncome = isCredit && !isDebit

        // Regex for amount: ₹50.00, Rs. 50, INR 50
        val amountRegex = Regex("(?i)(?:₹|rs\\.?|inr)\\s*([\\d,]+\\.?\\d*)")
        val amountMatch = amountRegex.find(combinedText)
        val amountStr = amountMatch?.groupValues?.get(1)?.replace(",", "") ?: return null
        val amount = amountStr.toDoubleOrNull() ?: return null

        // Extrapolate merchant
        var merchant: String? = null
        val toMatch = Regex("(?i)(?:to|from)\\s+([a-zA-Z0-9@.-]+(?:\\s+[a-zA-Z0-9@.-]+){0,2})").find(combinedText)
        if (toMatch != null) {
            merchant = toMatch.groupValues[1]
        } else {
            val paidMatch = Regex("(?i)paid to\\s+([^\\n\\.,]+)").find(combinedText)
            if (paidMatch != null) {
                merchant = paidMatch.groupValues[1]
            }
        }

        // Clean up merchant name slightly
        merchant = merchant?.trim()?.removeSuffix("successful")?.removeSuffix("successfully")?.trim()

        return ParsedNotification(amount, merchant, isIncome, sourceApp)
    }
}
