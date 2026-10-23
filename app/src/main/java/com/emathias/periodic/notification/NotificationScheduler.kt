package com.emathias.periodic.notification

import android.content.Context
import androidx.work.Data
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
 * Schedules daily processing and reminders via WorkManager. Work is persisted across reboots, and
 * if the device was off at the scheduled time WorkManager runs it as soon as the device is back up.
 * Each run re-enqueues the next day's run, which keeps it aligned with local time across DST.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: NotificationSettings,
) {
    /** Enqueues the daily processing and notification runs. */
    fun schedule(replace: Boolean = false) {
        scheduleProcessing(replace)
        scheduleNotification(replace)
    }

    /** Enqueues the next processing run shortly after midnight. */
    fun scheduleProcessing(replace: Boolean = false) {
        enqueue(
            PROCESSING_WORK_NAME,
            ACTION_PROCESS,
            PROCESSING_TIME,
            replace,
        )
    }

    /** Enqueues the next notification run at the configured reminder time. */
    fun scheduleNotification(replace: Boolean = false) {
        enqueue(
            NOTIFICATION_WORK_NAME,
            ACTION_NOTIFY,
            settings.reminderTime,
            replace,
        )
    }

    private fun enqueue(
        workName: String,
        action: String,
        time: LocalTime,
        replace: Boolean,
    ) {
        val request = OneTimeWorkRequestBuilder<DailyNotificationWorker>()
            .setInputData(Data.Builder().putString(ACTION_KEY, action).build())
            .setInitialDelay(
                delayUntilNext(time).toMillis(),
                TimeUnit.MILLISECONDS,
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request,
        )
    }

    fun setReminderTime(time: LocalTime) {
        settings.reminderTime = time
        scheduleNotification(replace = true)
    }

    companion object {
        const val PROCESSING_WORK_NAME = "daily_processing"
        const val NOTIFICATION_WORK_NAME = "daily_notification"
        const val ACTION_KEY = "action"
        const val ACTION_PROCESS = "process"
        const val ACTION_NOTIFY = "notify"
        val PROCESSING_TIME: LocalTime = LocalTime.of(0, 1)

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
