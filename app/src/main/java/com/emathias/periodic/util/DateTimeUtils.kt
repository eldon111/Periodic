package com.emathias.periodic.util

import java.time.Instant
import java.time.ZoneId
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
}
