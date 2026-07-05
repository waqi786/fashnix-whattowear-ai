package com.fashnix.app.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fashnix.app.workers.OutfitReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val DAILY_OUTFIT_WORK = "daily_outfit_reminder"

    fun scheduleDailyOutfitReminder(context: Context) {
        val request = PeriodicWorkRequestBuilder<OutfitReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilNextMorning(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_OUTFIT_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun millisUntilNextMorning(): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return next.timeInMillis - now.timeInMillis
    }
}
