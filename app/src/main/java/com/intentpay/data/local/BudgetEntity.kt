package com.intentpay.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_config")
data class BudgetEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton
    val monthlyBudget: Double,
    val dailyLimit: Double,
    val reflectionThreshold: Double
)
