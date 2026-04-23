package com.intentpay.data.parser

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
        val repository = app.expenseRepository

        val pendingResult = goAsync()
        
        scope.launch {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (msg in messages) {
                    val body = msg.displayMessageBody
                    val parsed = SmsParser.parseUpiDebitMessage(body ?: "")
                    if (parsed != null) {
                        val thresholdAmount = repository.getBudgetSync()?.reflectionThreshold ?: 200.0
                        
                        // Insert as UNCATEGORIZED by default
                        val entity = TransactionEntity(
                            amount = parsed.amount,
                            merchant = parsed.merchant,
                            dateMillis = System.currentTimeMillis(),
                            purpose = null,
                            categoryName = Category.UNCATEGORIZED.name,
                            upiRefId = parsed.upiRef,
                            autoDetected = true
                        )
                        
                        val insertedId = repository.deduplicateAndInsert(entity)
                        
                        // If it exceeds threshold, push ReflectionActivity
                        if (parsed.amount > thresholdAmount) {
                            val reflectionIntent = Intent().apply {
                                setClassName(context, "com.intentpay.ui.reflection.ReflectionActivity")
                                putExtra("TRANSACTION_ID", insertedId)
                                putExtra("AMOUNT", parsed.amount)
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
