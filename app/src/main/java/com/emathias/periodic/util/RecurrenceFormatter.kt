package com.emathias.periodic.util

import com.emathias.periodic.model.Schedule
import org.dmfs.rfc5545.Weekday
import org.dmfs.rfc5545.recur.Freq
import org.dmfs.rfc5545.recur.RecurrenceRule
import org.dmfs.rfc5545.recur.RecurrenceRule.Part
import org.dmfs.rfc5545.recur.RecurrenceRule.Skip
import java.time.DayOfWeek
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Describes a [Schedule] in plain English for a supported subset of RRULE:
 * DAILY/WEEKLY/MONTHLY/YEARLY with INTERVAL, BYDAY (weekly, or one nth weekday monthly),
 * BYMONTHDAY (monthly, single value, -1 = last day), and UNTIL / COUNT.
 * Anything else is described as "Custom repeat".
 */
object RecurrenceFormatter {

    const val CUSTOM = "Custom repeat"

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    private val MONTH_DAY_FORMAT = DateTimeFormatter.ofPattern("MMMM d", Locale.US)

    private val SUPPORTED_PARTS = setOf(
        Part.FREQ, Part.INTERVAL, Part.UNTIL, Part.COUNT, Part.WKST,
        Part.BYDAY, Part.BYMONTHDAY, Part.BYMONTH,
    )

    fun describe(schedule: Schedule): String {
        val rule = schedule.recurrenceRule
        if (schedule.rrule == null) {
            return "Once on ${schedule.start.toLocalDate().format(DATE_FORMAT)}"
        }
        if (rule == null) return CUSTOM
        if (rule.skip != Skip.OMIT || schedule.rrule.hasNonGregorianScale()) return CUSTOM
        val base = describeRule(rule, schedule.start.toLocalDate()) ?: return CUSTOM
        val ending = when {
            rule.count != null -> ", ${rule.count} ${if (rule.count == 1) "time" else "times"}"
            rule.until != null -> {
                val until = rule.until!!
                val date = if (until.isAllDay) {
                    LocalDate.of(until.year, until.month + 1, until.dayOfMonth)
                } else {
                    Instant.ofEpochMilli(until.timestamp).atZone(schedule.start.zone).toLocalDate()
                }
                ", until ${date.format(DATE_FORMAT)}"
            }
            else -> ""
        }
        return "$base$ending"
    }

    private fun describeRule(rule: RecurrenceRule, startDate: LocalDate): String? {
        val unsupported = Part.values().any {
            !it.name.startsWith("_") && it !in SUPPORTED_PARTS &&
                it != Part.SKIP && it != Part.RSCALE && rule.hasPart(it)
        }
        if (unsupported) return null
        val interval = rule.interval
        val byDay = rule.byDayPart.orEmpty()
        val byMonthDay = rule.getByPart(Part.BYMONTHDAY).orEmpty()
        val byMonth = rule.getByPart(Part.BYMONTH).orEmpty()

        return when (rule.freq) {
            Freq.DAILY -> {
                if (byDay.isNotEmpty() || byMonthDay.isNotEmpty() || byMonth.isNotEmpty()) return null
                every(interval, "day")
            }

            Freq.WEEKLY -> {
                if (byMonthDay.isNotEmpty() || byMonth.isNotEmpty() || byDay.any { it.pos != 0 }) return null
                val days = byDay.map { it.weekday.toDayOfWeek() }.distinct().sorted()
                when {
                    days.isEmpty() -> every(interval, "week")
                    interval == 1 && days == WEEKDAYS -> "Every weekday"
                    else -> "${every(interval, "week")} on ${days.joinToString(", ") { it.name() }}"
                }
            }

            Freq.MONTHLY -> {
                if (byMonth.isNotEmpty()) return null
                val prefix = every(interval, "month")
                when {
                    byDay.size == 1 && byMonthDay.isEmpty() && byDay[0].pos != 0 -> {
                        val ordinal = ordinal(byDay[0].pos) ?: return null
                        "$prefix on the $ordinal ${byDay[0].weekday.toDayOfWeek().name()}"
                    }

                    byDay.isEmpty() && byMonthDay.size == 1 -> when (val day = byMonthDay[0]) {
                        -1 -> "$prefix on the last day"
                        in 1..31 -> "$prefix on day $day"
                        else -> return null
                    }

                    byDay.isEmpty() && byMonthDay.isEmpty() -> "$prefix on day ${startDate.dayOfMonth}"
                    else -> return null
                }
            }

            Freq.YEARLY -> {
                if (byDay.isNotEmpty() || byMonthDay.size > 1 || byMonth.size > 1 ||
                    (byMonthDay.isNotEmpty() && byMonth.isEmpty())
                ) return null
                val month = byMonth.firstOrNull()?.plus(1) ?: startDate.monthValue // lib-recur exposes BYMONTH 0-based
                val day = byMonthDay.firstOrNull() ?: startDate.dayOfMonth
                val date = try {
                    LocalDate.of(2024, month, day)
                } catch (_: DateTimeException) {
                    return null
                }
                "${every(interval, "year")} on ${date.format(MONTH_DAY_FORMAT)}"
            }

            else -> null
        }
    }

    private fun String.hasNonGregorianScale(): Boolean {
        val scale = split(';')
            .mapNotNull { part ->
                part.split('=', limit = 2).takeIf { it.size == 2 }
                    ?.let { it[0].trim().uppercase(Locale.US) to it[1].trim().uppercase(Locale.US) }
            }
            .firstOrNull { it.first == "RSCALE" }
            ?.second
        return scale != null && scale != "GREGORIAN"
    }

    private fun every(interval: Int, unit: String) = when (interval) {
        1 -> "Every $unit"
        2 -> "Every other $unit"
        else -> "Every $interval ${unit}s"
    }

    private fun ordinal(pos: Int): String? = when (pos) {
        1 -> "first"
        2 -> "second"
        3 -> "third"
        4 -> "fourth"
        5 -> "fifth"
        -1 -> "last"
        else -> null
    }

    private fun DayOfWeek.name() = getDisplayName(TextStyle.FULL, Locale.US)

    private fun Weekday.toDayOfWeek(): DayOfWeek = when (this) {
        Weekday.MO -> DayOfWeek.MONDAY
        Weekday.TU -> DayOfWeek.TUESDAY
        Weekday.WE -> DayOfWeek.WEDNESDAY
        Weekday.TH -> DayOfWeek.THURSDAY
        Weekday.FR -> DayOfWeek.FRIDAY
        Weekday.SA -> DayOfWeek.SATURDAY
        Weekday.SU -> DayOfWeek.SUNDAY
    }

    private val WEEKDAYS = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
    )
}
