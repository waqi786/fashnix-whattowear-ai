package com.fashnix.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fashnix.app.R

/**
 * NotificationAgent: AI-driven notification system for Fashnix.
 * Handles style reminders, laundry alerts, and trend updates.
 */
object NotificationAgent {
    private const val CHANNEL_ID = "fashnix_alerts"
    private const val CHANNEL_NAME = "Fashnix Style Alerts"

    fun sendStyleNotification(context: Context, title: String, message: String) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent to open the app when clicking the notification
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun sendLaundryReminder(context: Context, itemName: String) {
        sendStyleNotification(
            context,
            "Care Alert: $itemName",
            "Your $itemName has been worn multiple times. Time for a refresh?"
        )
    }

    fun sendDailyTrendAlert(context: Context) {
        sendStyleNotification(
            context,
            "Morning Style Suggestions",
            "The weather is perfect for your favorite jacket today. Check your daily picks!"
        )
    }
}
