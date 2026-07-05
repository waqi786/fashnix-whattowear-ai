package com.fashnix.app.workers

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fashnix.app.R
import com.fashnix.app.ui.main.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BorrowReturnReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (!notificationManager.areNotificationsEnabled()) {
            return Result.success()
        }

        val itemName = inputData.getString("item_name") ?: return Result.failure()
        val ownerName = inputData.getString("owner_name") ?: ""

        val pendingIntent = NavDeepLinkBuilder(applicationContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.wardrobeFragment)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(applicationContext, "family_closet")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(applicationContext.getString(R.string.notification_reminder_return_title))
            .setContentText(applicationContext.getString(R.string.notification_reminder_return_body, itemName, ownerName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(itemName.hashCode(), notification)
        return Result.success()
    }
}
