package com.intentpay.data.detection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.intentpay.IntentPayApplication
import com.intentpay.data.local.TransactionEntity
import com.intentpay.domain.models.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val app = context.applicationContext as IntentPayApplication

        val pendingResult = goAsync()
        
        scope.launch {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (msg in messages) {
                    val body = msg.displayMessageBody
                    val parsedSms = SmsParser.parseUpiMessage(body ?: "")
                    if (parsedSms != null) {
                        val parsedTx = ParsedTransaction(
                            amount = parsedSms.amount,
                            merchant = parsedSms.merchant,
                            isIncome = parsedSms.isIncome,
                            source = "SMS",
                            upiRef = parsedSms.upiRef
                        )
                        
                        val result = app.transactionDetector.process(parsedTx)

                        // Threshold decision lives in UnifiedTransactionDetector
                        if (result.shouldReflect) {
                            val reflectionIntent = Intent().apply {
                                setClassName(context, "com.intentpay.ui.reflection.ReflectionActivity")
                                putExtra("TRANSACTION_ID", result.id)
                                putExtra("AMOUNT", result.amount)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            context.startActivity(reflectionIntent)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
