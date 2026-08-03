package com.intentpay.data.repository

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.intentpay.IntentPayApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val app = applicationContext as IntentPayApplication
        val repository = app.transactionRepository
        val googleSheetsService = GoogleSheetsService(applicationContext)

        try {
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            val budget = repository.getBudgetSync()
            val sheetId = budget?.googleDriveFolderId

            if (account == null || sheetId.isNullOrEmpty()) {
                return@withContext Result.success() // Can't sync without auth/id
            }

            val unsynced = repository.getUnsyncedTransactions()
            if (unsynced.isEmpty()) {
                return@withContext Result.success()
            }

            val sheetsService = googleSheetsService.createSheetsService(account)
            googleSheetsService.appendTransactions(sheetsService, sheetId, unsynced)

            // Mark as synced
            repository.markAsSynced(unsynced.map { it.id })
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
