package com.emathias.periodic.util

import com.cronutils.descriptor.CronDescriptor
import com.cronutils.model.Cron
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinition
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Centralized utility object for cron expression handling throughout the app.
 * Provides a consistent cron definition and parser for all components.
 */
object CronUtils {

    /**
     * The cron definition used throughout the app.
     * Uses Unix cron format (5 fields: minute hour day month weekday).
     */
    val cronDefinition: CronDefinition =
        CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J)

    /**
     * The cron parser instance used throughout the app.
     */
    val cronParser: CronParser = CronParser(cronDefinition)

    /**
     * Parse a cron expression string safely.
     * @param cronExpression The cron expression string to parse
     * @return Parsed Cron object or null if parsing fails
     */
    fun parseCronExpression(cronExpression: String): Cron? {
        return try {
            cronParser.parse(cronExpression)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validate if a cron expression string is valid.
     * @param cronExpression The cron expression string to validate
     * @return true if valid, false otherwise
     */
    fun isValidCronExpression(cronExpression: String): Boolean {
        return parseCronExpression(cronExpression) != null
    }

    /**
     * Format a Cron expression into human-readable recurring text
     * Examples: "every week", "every month", "every day at 9:00 AM"
     */
    fun formatCronExpression(cron: Cron, locale: Locale = Locale.getDefault()): String {
        return try {
            val descriptor = CronDescriptor.instance(locale)
            val text = descriptor.describe(cron)
            text.replace("every day between Monday and Friday", "every weekday")
        } catch (e: Exception) {
            // Fallback to cron string if description fails
            cron.asString()
        }
    }

    /**
     * Format the next occurrence of a specific day of the week
     * Example: "next Wednesday", "this Friday"
     */
    fun formatNextOccurrence(dayOfWeek: DayOfWeek, locale: Locale = Locale.getDefault()): String {
        val today = LocalDate.now()
        val targetDay = today.with(java.time.temporal.TemporalAdjusters.next(dayOfWeek))
        val daysBetween = ChronoUnit.DAYS.between(today, targetDay)

        return when (daysBetween.toInt()) {
            0 -> "today"
            1 -> "tomorrow"
            in 2..6 -> "this ${dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}"
            7 -> "next ${dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}"
            else -> "in $daysBetween days"
        }
    }

    /**
     * Format an Instant as a relative time
     * Examples: "in 2 hours", "3 days ago", "next week"
     */
    fun formatRelativeTime(instant: Instant, locale: Locale = Locale.getDefault()): String {
        val now = Instant.now()
        val duration = java.time.Duration.between(now, instant)

        return when {
            duration.isNegative -> {
                val absDuration = duration.abs()
                when {
                    absDuration.toDays() > 0 -> "${absDuration.toDays()} days ago"
                    absDuration.toHours() > 0 -> "${absDuration.toHours()} hours ago"
                    absDuration.toMinutes() > 0 -> "${absDuration.toMinutes()} minutes ago"
                    else -> "just now"
                }
            }

            else -> {
                when {
                    duration.toDays() > 0 -> "in ${duration.toDays()} days"
                    duration.toHours() > 0 -> "in ${duration.toHours()} hours"
                    duration.toMinutes() > 0 -> "in ${duration.toMinutes()} minutes"
                    else -> "now"
                }
            }
        }
    }

    /**
     * Format the next occurrence based on an Instant and optional Cron expression
     * This is useful for scheduled items to show when they will next occur
     */
    fun formatNextScheduledOccurrence(
        firstOccurrence: Instant,
        cronExpression: Cron?,
        locale: Locale = Locale.getDefault(),
    ): String {
        val now = Instant.now()

        if (cronExpression == null) {
            // One-time occurrence
            return if (firstOccurrence.isAfter(now)) {
                formatRelativeTime(firstOccurrence, locale)
            } else {
                "completed"
            }
        }

        // Recurring occurrence - find next occurrence using cron
        val executionTime = ExecutionTime.forCron(cronExpression)
        val nowZoned = now.atZone(ZoneId.systemDefault())
        val nextExecution = executionTime.nextExecution(nowZoned)

        return if (nextExecution.isPresent) {
            formatRelativeTime(nextExecution.get().toInstant(), locale)
        } else {
            "no future occurrences"
        }
    }

    /**
     * Common cron expressions for convenience.
     */
    object CommonExpressions {
        const val EVERY_MINUTE = "* * * * *"
        const val EVERY_HOUR = "0 * * * *"
        const val EVERY_DAY_AT_MIDNIGHT = "0 0 * * *"
        const val EVERY_DAY_AT_NOON = "0 12 * * *"
        const val EVERY_WEEK_SUNDAY_MIDNIGHT = "0 0 * * 0"
        const val EVERY_MONTH_FIRST_DAY_MIDNIGHT = "0 0 1 * *"
        const val EVERY_YEAR_JAN_FIRST_MIDNIGHT = "0 0 1 1 *"

        // Weekday expressions (Monday = 1, Sunday = 0)
        const val EVERY_MONDAY_9AM = "0 9 * * 1"
        const val EVERY_TUESDAY_9AM = "0 9 * * 2"
        const val EVERY_WEDNESDAY_9AM = "0 9 * * 3"
        const val EVERY_THURSDAY_9AM = "0 9 * * 4"
        const val EVERY_FRIDAY_9AM = "0 9 * * 5"
        const val EVERY_SATURDAY_9AM = "0 9 * * 6"
        const val EVERY_SUNDAY_9AM = "0 9 * * 0"
    }
}
