package com.expenseos.app.features.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.expenseos.app.MainActivity
import com.expenseos.app.R
import com.expenseos.app.core.model.TransactionCandidate

class SmsNotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Expense Transactions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for automatically detected expenses from SMS"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showTransactionDetectedNotification(transaction: TransactionCandidate) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // TODO: In a real app we'd add "Confirm" and "Ignore" action buttons that trigger a BroadcastReceiver or Service
        // to directly interact with AppRepository without opening the app.

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Using default launcher icon, replace with specific drawable later
            .setContentTitle("New Expense Detected")
            .setContentText("PKR ${transaction.amount} at ${transaction.merchant}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("We noticed a transaction of PKR ${transaction.amount} at ${transaction.merchant}. Tap to review and earn +10 Coins!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(transaction.id.hashCode(), builder.build())
    }

    companion object {
        const val CHANNEL_ID = "expense_os_transactions"
    }
}
