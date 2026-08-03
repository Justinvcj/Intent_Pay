package com.intentpay

import android.app.Application
import com.intentpay.data.local.BudgetEntity
import com.intentpay.data.local.IntentPayDatabase
import com.intentpay.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.intentpay.categorization.MerchantMemory
import com.intentpay.data.detection.UnifiedTransactionDetector
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.intentpay.workers.ReminderWorker
import java.util.concurrent.TimeUnit

class IntentPayApplication : Application() {

    lateinit var database: IntentPayDatabase
    lateinit var transactionRepository: TransactionRepository
    lateinit var merchantMemory: MerchantMemory
    lateinit var transactionDetector: UnifiedTransactionDetector

    override fun onCreate() {
        super.onCreate()
        database = IntentPayDatabase.getDatabase(this)
        transactionRepository = TransactionRepository(database.transactionDao(), database.budgetDao())
        merchantMemory = MerchantMemory(database.merchantRuleDao())
        transactionDetector = UnifiedTransactionDetector(transactionRepository, merchantMemory)
        
        // Initialize default budget if not exists
        CoroutineScope(Dispatchers.IO).launch {
            if (transactionRepository.getBudgetSync() == null) {
                transactionRepository.setBudgetConfig(
                    BudgetEntity(
                        id = 1,
                        monthlyBudget = 10000.0,
                        dailyLimit = 500.0,
                        reflectionThreshold = 200.0
                    )
                )
            }
        }
        
        // Setup Reminders
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
