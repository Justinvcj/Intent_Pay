package com.intentpay

import android.app.Application
import com.intentpay.data.local.BudgetEntity
import com.intentpay.data.local.IntentPayDatabase
import com.intentpay.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    lateinit var dataStoreManager: com.intentpay.data.local.DataStoreManager

    /** Application-lifetime scope for fire-and-forget startup work. */
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = IntentPayDatabase.getDatabase(this)
        transactionRepository = TransactionRepository(database.transactionDao(), database.budgetDao())
        merchantMemory = MerchantMemory(database.merchantRuleDao())
        transactionDetector = UnifiedTransactionDetector(transactionRepository, merchantMemory)
        dataStoreManager = com.intentpay.data.local.DataStoreManager(this)
        
        // Initialize default budget if not exists, then schedule the daily reminder
        // at the configured wall-clock time.
        applicationScope.launch {
            var budget = transactionRepository.getBudgetSync()
            if (budget == null) {
                budget = BudgetEntity(
                    id = 1,
                    monthlyBudget = 10000.0,
                    dailyLimit = 500.0,
                    reflectionThreshold = 200.0
                )
                transactionRepository.setBudgetConfig(budget)
            }
            scheduleReminder(budget.endOfDayReminderHour, budget.endOfDayReminderMinute)
        }
    }

    private fun scheduleReminder(hour: Int, minute: Int) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntil(hour, minute), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun millisUntil(hour: Int, minute: Int): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
