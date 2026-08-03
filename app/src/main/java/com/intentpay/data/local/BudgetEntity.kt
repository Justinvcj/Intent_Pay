package com.intentpay.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_config")
data class BudgetEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton
    val monthlyBudget: Double,
    val dailyLimit: Double,
    val reflectionThreshold: Double,
    val endOfDayReminderHour: Int = 21,
    val endOfDayReminderMinute: Int = 0,
    val enableThreeDayReminder: Boolean = true,
    val enableWeeklyReport: Boolean = true,
    val weeklyReportDay: Int = 1, // Calendar.SUNDAY = 1
    val googleDriveFolderId: String? = null,
    val exportTemplatePath: String? = null
)
