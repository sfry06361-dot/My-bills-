package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Bill
import java.util.Locale

object BillReminderManager {
    const val CHANNEL_ID = "bill_reminders_channel"
    private const val CHANNEL_NAME = "Bill Payment Reminders"
    private const val CHANNEL_DESC = "Notifications for upcoming bill payment deadlines"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showInstantReminderNotification(context: Context, bill: Bill) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            bill.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val amountStr = String.format(Locale.US, "%.2f", bill.amount)
        val daysBefore = bill.reminderDaysBefore
        val reminderTimingText = when (daysBefore) {
            0 -> "is due TODAY"
            1 -> "is due TOMORROW"
            else -> "is due in $daysBefore days"
        }

        val contentTitle = "Payment Reminder: ${bill.title}"
        val contentText = "${bill.currency}$amountStr $reminderTimingText (${bill.dueDate})!"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$contentText\nCategory: ${bill.category}${if (bill.notes.isNotBlank()) "\nNote: ${bill.notes}" else ""}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (bill.id.ifBlank { System.currentTimeMillis().toString() }).hashCode()
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Handled when permission is not granted
        }
    }
}
