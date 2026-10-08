package com.intentpay.utils

import com.google.gson.GsonBuilder
import com.intentpay.data.local.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    fun generateCsv(transactions: List<TransactionEntity>): String {
        val builder = java.lang.StringBuilder()
        // Header
        builder.append("ID,Date,Amount,Merchant,Category,IsIncome,Source,Purpose,UPIRef\n")
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        transactions.forEach { tx ->
            val date = dateFormat.format(Date(tx.dateMillis))
            // Escape quotes and commas in strings
            val merchant = escapeCsv(tx.merchant ?: "")
            val purpose = escapeCsv(tx.purpose ?: "")
            val upiRef = escapeCsv(tx.upiRefId ?: "")
            
            builder.append("${tx.id},$date,${tx.amount},$merchant,${tx.categoryName},${tx.isIncome},${tx.source},$purpose,$upiRef\n")
        }
        
        return builder.toString()
    }

    fun generateJson(transactions: List<TransactionEntity>): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(transactions)
    }

    private fun escapeCsv(value: String): String {
        var escaped = value.replace("\"", "\"\"")
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            escaped = "\"$escaped\""
        }
        return escaped
    }
}
