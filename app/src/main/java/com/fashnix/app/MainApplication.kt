package com.fashnix.app

import android.content.Context
import androidx.multidex.MultiDex
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.fashnix.app.util.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    companion object {
        const val CHANNEL_DAILY_OUTFIT = "daily_outfit"
        const val CHANNEL_LAUNDRY_REMINDER = "laundry_reminder"
        const val CHANNEL_FAMILY_CLOSET = "family_closet"
        const val CHANNEL_BADGES = "badges"
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        ReminderScheduler.scheduleDailyOutfitReminder(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_DAILY_OUTFIT,
                    "Daily Outfit",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily outfit suggestions"
                    enableLights(true)
                    lightColor = 0xFFD70F64.toInt()
                },
                NotificationChannel(
                    CHANNEL_LAUNDRY_REMINDER,
                    "Laundry Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders to wash your clothes"
                },
                NotificationChannel(
                    CHANNEL_FAMILY_CLOSET,
                    "Family Closet",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Updates from your family closet"
                },
                NotificationChannel(
                    CHANNEL_BADGES,
                    "Badges & Rewards",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Badge and streak notifications"
                    enableVibration(true)
                }
            )
            val manager = getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
}
