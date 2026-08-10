package com.intentpay.ui.intercept

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.IntentPayApplication
import com.intentpay.data.local.TransactionEntity
import com.intentpay.ui.components.GlassButton
import com.intentpay.ui.components.GlassCard
import com.intentpay.ui.components.GlassTextField
import com.intentpay.ui.theme.IosBlue
import com.intentpay.ui.theme.IosRed
import com.intentpay.ui.theme.TextSecondary
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
            var intentInput by remember { mutableStateOf("") }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000)), // Semi-transparent black background
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight(),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Payment Captured!",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "₹$amount to $payee",
                            color = IosBlue,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Text(
                            text = "What was this for?",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        GlassTextField(
                            value = intentInput,
                            onValueChange = { intentInput = it },
                            placeholder = "e.g., Groceries, Dinner, Rent",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassButton(
                                text = "Skip for later",
                                onClick = {
                                    saveTransaction(amount, payee, null, true, app)
                                    finish()
                                },
                                isPrimary = false,
                                modifier = Modifier.weight(1f)
                            )
                            GlassButton(
                                text = "Save",
                                onClick = {
                                    saveTransaction(amount, payee, intentInput.takeIf { it.isNotBlank() }, false, app)
                                    finish()
                                },
                                isPrimary = true,
                                modifier = Modifier.weight(1f)
                            )
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
