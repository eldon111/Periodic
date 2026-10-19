package com.emathias.periodic.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.emathias.periodic.worker.DailyNotificationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the daily reminder run via WorkManager. Work is persisted across reboots, and if the
 * device was off at the scheduled time WorkManager runs it as soon as the device is back up.
 * Each run re-enqueues the next day's run, which keeps it aligned with local time across DST.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: NotificationSettings,
) {
    /** Enqueues the next run. [replace] restarts the timer (e.g. after the time changes). */
    fun schedule(replace: Boolean = false) {
        val delay = delayUntilNext(settings.reminderTime)
        val request = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request,
        )
    }

    fun setReminderTime(time: LocalTime) {
        settings.reminderTime = time
        schedule(replace = true)
    }

    companion object {
        const val WORK_NAME = "daily_notification"

        fun delayUntilNext(
            time: LocalTime,
            now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault()),
        ): Duration {
            var next = now.with(time)
            if (!next.isAfter(now)) next = now.plusDays(1).with(time)
            return Duration.between(now, next)
        }
    }
}
