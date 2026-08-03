package com.intentpay.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val merchant: String?,
    val normalizedMerchant: String? = null,
    val dateMillis: Long,
    val purpose: String?,
    val categoryName: String,
    val upiRefId: String?,
    val autoDetected: Boolean,
    val isIncome: Boolean = false,
    val source: String = "MANUAL",
    val needsReview: Boolean = false,
    val reminderCount: Int = 0,
    val syncedWithDrive: Boolean = false
)
