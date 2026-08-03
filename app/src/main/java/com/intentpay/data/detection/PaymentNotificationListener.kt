package com.intentpay.data.detection

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.intentpay.IntentPayApplication

class PaymentNotificationListener : NotificationListenerService() {

    private val supportedPackages = listOf(
        "com.google.android.apps.nbu.paisa.user", // GPay
        "com.phonepe.app", // PhonePe
        "net.one97.paytm", // Paytm
        "in.org.npci.upiapp" // BHIM
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val packageName = sbn.packageName
        if (!supportedPackages.contains(packageName)) return

        val notification = sbn.notification
        val title = notification.extras.getCharSequence("android.title")?.toString() ?: ""
        val text = notification.extras.getCharSequence("android.text")?.toString() ?: ""

        Log.d("PaymentNotification", "Received from $packageName: $title - $text")

        val parsed = NotificationParser.parse(title, text, packageName)
        
        if (parsed != null) {
            val app = applicationContext as IntentPayApplication
            
            // Map ParsedNotification to ParsedTransaction for UnifiedTransactionDetector
            val parsedTransaction = ParsedTransaction(
                amount = parsed.amount,
                merchant = parsed.merchant,
                isIncome = parsed.isIncome,
                source = parsed.sourceApp,
                upiRef = null // Notifications usually don't have the 12 digit UPI ref
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    app.transactionDetector.process(parsedTransaction)
                    Log.d("PaymentNotification", "Processed notification transaction: $parsedTransaction")
                } catch (e: Exception) {
                    Log.e("PaymentNotification", "Failed to process notification", e)
                }
            }
        }
    }
}
