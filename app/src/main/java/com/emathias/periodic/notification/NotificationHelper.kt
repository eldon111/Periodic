package com.emathias.periodic.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.emathias.periodic.MainActivity
import com.emathias.periodic.R
import com.emathias.periodic.db.entities.ScheduledItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Daily reminders", NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /**
     * Posts one notification per item. Returns the number posted, or -1 if notifications are
     * not permitted.
     */
    fun notify(items: List<ScheduledItem>): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted; skipping ${items.size} notification(s)")
            return -1
        }

        createChannel()
        val contentIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val manager = NotificationManagerCompat.from(context)
        var posted = 0
        items.forEach { item ->
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(item.title)
                .setContentText(item.description)
                .setStyle(NotificationCompat.BigTextStyle().bigText(item.description))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()
            try {
                manager.notify(item.id.toInt(), notification)
                posted++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to post notification for item ${item.id}", e)
            }
        }
        return posted
    }

    companion object {
        private const val TAG = "NotificationHelper"
        const val CHANNEL_ID = "daily_reminders"

        /** True if [item] has an occurrence on [date] in the device's local time zone. */
        fun isDueOn(item: ScheduledItem, date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Boolean {
            val startOfDay = date.atStartOfDay(zone).toInstant()
            val endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant()
            val next = item.schedule.nextOccurrence(startOfDay.minusMillis(1)) ?: return false
            return next.isBefore(endOfDay)
        }
    }
}
