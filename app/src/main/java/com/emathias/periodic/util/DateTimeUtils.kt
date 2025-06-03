package com.emathias.periodic.util

import com.cronutils.model.Cron
import com.cronutils.model.time.ExecutionTime
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Utility object for date and time formatting throughout the app.
 * Provides consistent date/time formatting functions for UI components.
 */
object DateTimeUtils {

    /**
     * Format an Instant as a human-readable date and time string.
     * Uses 12-hour format with AM/PM for consistency with cron descriptions.
     *
     * @param instant The Instant to format
     * @return Formatted string like "Jan 15, 2024 at 2:30 PM"
     */
    fun formatDateTime(instant: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a")
        return instant.atZone(ZoneId.systemDefault()).format(formatter)
    }

    /**
     * Format an Instant as a date-only string.
     *
     * @param instant The Instant to format
     * @return Formatted string like "Jan 15, 2024"
     */
    fun formatDate(instant: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        return instant.atZone(ZoneId.systemDefault()).format(formatter)
    }

    /**
     * Format an Instant as a time-only string with 12-hour format.
     *
     * @param instant The Instant to format
     * @return Formatted string like "2:30 PM"
     */
    fun formatTime(instant: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return instant.atZone(ZoneId.systemDefault()).format(formatter)
    }

    /**
     * Calculate the first occurrence of a scheduled item based on its cron expression and start time.
     * If the item has a cron expression, it calculates the first execution time that matches the cron pattern.
     * For non-repeating items, it simply returns the start time.
     *
     * @param startsAt The starting time of the scheduled item
     * @param cronExpression The cron expression for repeating items (null for one-time items)
     * @return The first occurrence time as an Instant
     */
    fun getFirstOccurrence(startsAt: Instant, cronExpression: Cron?): Instant {
        return cronExpression?.let { cron ->
            val executionTime = ExecutionTime.forCron(cron)
            val startsAtZoned = startsAt.atZone(ZoneOffset.systemDefault())
            if (executionTime.isMatch(startsAtZoned)) {
                startsAt
            } else {
                executionTime.nextExecution(startsAtZoned).orElse(null)?.toInstant()
            }
        } ?: startsAt
    }
}
