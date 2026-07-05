package com.fashnix.app.workers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fashnix.app.MainApplication
import com.fashnix.app.R
import com.fashnix.app.ui.main.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OutfitReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (!notificationManager.areNotificationsEnabled()) {
            return Result.success()
        }

        val outfitName = inputData.getString("outfit_name") ?: "Planned outfit"
        val items = inputData.getString("items") ?: "Open Fashnix for today's suggestion."

        val pendingIntent = NavDeepLinkBuilder(applicationContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.plannerFragment)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(applicationContext, MainApplication.CHANNEL_DAILY_OUTFIT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Outfit Reminder")
            .setContentText("Today: $outfitName - $items")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(outfitName.hashCode(), notification)
        return Result.success()
    }
}
