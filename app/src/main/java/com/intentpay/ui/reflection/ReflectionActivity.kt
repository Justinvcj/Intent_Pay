package com.intentpay.ui.reflection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intentpay.IntentPayApplication
import com.intentpay.ui.theme.IntentPayTheme

class ReflectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val transactionId = intent.getLongExtra("TRANSACTION_ID", -1L)
        val amount = intent.getDoubleExtra("AMOUNT", 0.0)
        
        val app = application as IntentPayApplication
        val factory = ReflectionViewModelFactory(app.expenseRepository, transactionId)

        setContent {
            IntentPayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ReflectionViewModel = viewModel(factory = factory)
                    ReflectionScreen(
                        viewModel = viewModel,
                        amount = amount,
                        onFinish = { finish() }
                    )
                }
            }
        }
    }
}
