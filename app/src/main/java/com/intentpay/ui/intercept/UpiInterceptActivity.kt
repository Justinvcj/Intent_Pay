package com.intentpay.ui.intercept

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.intentpay.IntentPayApplication
import com.intentpay.data.local.TransactionEntity
import com.intentpay.ui.components.AppTextField
import com.intentpay.ui.components.GroupedCard
import com.intentpay.ui.components.PrimaryButton
import com.intentpay.ui.components.SecondaryButton
import com.intentpay.ui.theme.IntentPayTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpiInterceptActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val amount = intent.getStringExtra("AMOUNT") ?: "0.00"
        val payee = intent.getStringExtra("PAYEE") ?: "Unknown"

        val app = application as IntentPayApplication

        setContent {
            IntentPayTheme {
                var intentInput by remember { mutableStateOf("") }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000)), // scrim over the payment app behind
                    contentAlignment = Alignment.Center
                ) {
                    GroupedCard(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Payment detected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹$amount",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "to $payee",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            AppTextField(
                                value = intentInput,
                                onValueChange = { intentInput = it },
                                label = "What was this for",
                                supportingText = "Groceries, dinner, rent"
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SecondaryButton(
                                    text = "Later",
                                    onClick = {
                                        saveTransaction(amount, payee, null, true, app)
                                        finish()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                PrimaryButton(
                                    text = "Save",
                                    onClick = {
                                        saveTransaction(amount, payee, intentInput.takeIf { it.isNotBlank() }, false, app)
                                        finish()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveTransaction(amountStr: String, payee: String, intentStr: String?, needsReview: Boolean, app: IntentPayApplication) {
        val amount = amountStr.replace(",", "").toDoubleOrNull() ?: 0.0
        val tx = TransactionEntity(
            amount = amount,
            merchant = payee,
            dateMillis = System.currentTimeMillis(),
            purpose = intentStr,
            categoryName = "Categorizing...", // The MemoryEngine will pick it up or we let user do it
            upiRefId = null,
            autoDetected = true,
            isIncome = false,
            source = "UPI_INTERCEPT",
            needsReview = needsReview
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            val category = app.merchantMemory.resolve(payee)
            val updatedTx = tx.copy(categoryName = category.name)
            app.transactionRepository.insertTransaction(updatedTx)
        }
    }
}
