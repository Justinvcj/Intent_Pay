package com.intentpay.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.intentpay.R
import com.intentpay.data.local.IntentPayDatabase
import com.intentpay.data.repository.TransactionRepository
import com.intentpay.ui.MainActivity
import kotlinx.coroutines.flow.first

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = IntentPayDatabase.getDatabase(context)
        val repository = TransactionRepository(database.transactionDao(), database.budgetDao())
        
        val count = repository.getUncategorizedCount().first()

        if (count > 0) {
            showNotification(count)
        }

        return Result.success()
    }

    private fun showNotification(count: Int) {
        val channelId = "intentpay_reminders"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Using app icon
            .setContentTitle("Pending Transactions")
            .setContentText("You have $count uncategorized transactions. Tap to review them.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Checking permissions for Android 13+ is required before showing notifications
        // However, we assume the permission was requested if needed at runtime in UI.
        // For simplicity we catch SecurityException if permission is missing.
        try {
            NotificationManagerCompat.from(context).notify(1, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
