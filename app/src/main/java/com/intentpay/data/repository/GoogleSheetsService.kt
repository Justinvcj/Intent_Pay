package com.intentpay.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import com.intentpay.data.local.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoogleSheetsService(private val context: Context) {

    fun createSheetsService(account: GoogleSignInAccount): Sheets {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(SheetsScopes.SPREADSHEETS)
        ).apply {
            selectedAccount = account.account
        }

        return Sheets.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("IntentPay").build()
    }

    suspend fun appendTransactions(
        sheetsService: Sheets,
        spreadsheetId: String,
        transactions: List<TransactionEntity>
    ) = withContext(Dispatchers.IO) {
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val sheetName = "$currentYear Daily Exp" // Adjusts dynamically to '2024 Daily Exp', '2025 Daily Exp', etc.

        val values = transactions.map { t ->
            val dateStr = dateFormat.format(Date(t.dateMillis))
            val received = if (t.isIncome) t.amount else ""
            val expense = if (!t.isIncome) t.amount else ""
            val purpose = t.purpose ?: t.merchant ?: "Unknown"
            
            // Formula to calculate balance based on previous row and current income/expense
            // R[-1]C = previous balance
            // RC[-4] = current received (Col B)
            // RC[-3] = current expense (Col C)
            val balanceFormula = "=R[-1]C + RC[-4] - RC[-3]"

            listOf(dateStr, received, expense, purpose, "", balanceFormula)
        }

        val body = ValueRange().setValues(values)
        
        // Use USER_ENTERED to ensure the formula string gets parsed and evaluated by Sheets
        sheetsService.spreadsheets().values()
            .append(spreadsheetId, sheetName, body)
            .setValueInputOption("USER_ENTERED")
            .execute()
    }
}
