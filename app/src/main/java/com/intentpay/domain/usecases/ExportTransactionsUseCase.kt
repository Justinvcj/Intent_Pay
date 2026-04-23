package com.intentpay.domain.usecases

import android.content.Context
import android.os.Environment
import com.intentpay.data.local.TransactionEntity
import com.intentpay.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportTransactionsUseCase(private val repository: ExpenseRepository) {

    suspend fun exportToCsv(context: Context): Result<String> {
        return try {
            val transactions = repository.allTransactions.first()
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "IntentPay_Export_${System.currentTimeMillis()}.csv")
            
            val writer = FileWriter(file)
            writer.append("Date,Merchant,Amount,Purpose,Category,UPI Reference,Auto-Detected\n")
            
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            
            transactions.forEach {
                val dateStr = sdf.format(Date(it.dateMillis))
                val merchant = escapeCsv(it.merchant ?: "")
                val purpose = escapeCsv(it.purpose ?: "")
                val category = escapeCsv(it.categoryName)
                val upiRef = escapeCsv(it.upiRefId ?: "")
                
                writer.append("$dateStr,$merchant,${it.amount},$purpose,$category,$upiRef,${it.autoDetected}\n")
            }
            
            writer.flush()
            writer.close()
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun escapeCsv(value: String): String {
        var str = value
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            str = str.replace("\"", "\"\"")
            str = "\"$str\""
        }
        return str
    }
}
