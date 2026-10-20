package com.emathias.periodic.notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/** App-wide notification preferences. The reminder time is a local wall-clock time. */
@Singleton
class NotificationSettings @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)

    var reminderTime: LocalTime
        get() = LocalTime.of(
            prefs.getInt(KEY_HOUR, DEFAULT_HOUR).coerceIn(0, 23),
            prefs.getInt(KEY_MINUTE, 0).coerceIn(0, 59),
        )
        set(value) {
            prefs.edit().putInt(KEY_HOUR, value.hour).putInt(KEY_MINUTE, value.minute).apply()
        }

    companion object {
        const val DEFAULT_HOUR = 8
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_MINUTE = "reminder_minute"
    }
}
