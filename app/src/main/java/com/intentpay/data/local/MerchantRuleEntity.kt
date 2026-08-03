package com.intentpay.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "merchant_rules",
    indices = [Index(value = ["merchantPattern"], unique = true)]
)
data class MerchantRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchantPattern: String,
    val categoryName: String,
    val timesUsed: Int = 1,
    val lastUsedMillis: Long,
    val createdByUser: Boolean
)
