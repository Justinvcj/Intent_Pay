package com.intentpay

import android.app.Application
import com.intentpay.data.local.BudgetEntity
import com.intentpay.data.local.IntentPayDatabase
import com.intentpay.data.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntentPayApplication : Application() {

    lateinit var database: IntentPayDatabase
    lateinit var expenseRepository: ExpenseRepository

    override fun onCreate() {
        super.onCreate()
        database = IntentPayDatabase.getDatabase(this)
        expenseRepository = ExpenseRepository(database.transactionDao(), database.budgetDao())
        
        // Initialize default budget if not exists
        CoroutineScope(Dispatchers.IO).launch {
            if (expenseRepository.getBudgetSync() == null) {
                expenseRepository.setBudgetConfig(
                    BudgetEntity(
                        id = 1,
                        monthlyBudget = 10000.0,
                        dailyLimit = 500.0,
                        reflectionThreshold = 200.0
                    )
                )
            }
        }
    }
}
