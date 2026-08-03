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
        val repository = app.transactionRepository

        val pendingResult = goAsync()
        
        scope.launch {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (msg in messages) {
                    val body = msg.displayMessageBody
                    val parsedSms = SmsParser.parseUpiMessage(body ?: "")
                    if (parsedSms != null) {
                        val thresholdAmount = repository.getBudgetSync()?.reflectionThreshold ?: 200.0
                        
                        val parsedTx = ParsedTransaction(
                            amount = parsedSms.amount,
                            merchant = parsedSms.merchant,
                            isIncome = parsedSms.isIncome,
                            source = "SMS",
                            upiRef = parsedSms.upiRef
                        )
                        
                        val insertedId = app.transactionDetector.process(parsedTx)
                        
                        // If it exceeds threshold and is not income, push ReflectionActivity
                        if (parsedTx.amount > thresholdAmount && !parsedTx.isIncome) {
                            val reflectionIntent = Intent().apply {
                                setClassName(context, "com.intentpay.ui.reflection.ReflectionActivity")
                                putExtra("TRANSACTION_ID", insertedId)
                                putExtra("AMOUNT", parsedTx.amount)
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
