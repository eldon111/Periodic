package com.emathias.periodic.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

enum class RepeatUnit(val label: String) { DAY("day"), WEEK("week"), MONTH("month"), YEAR("year") }

enum class MonthlyMode { DAY_OF_MONTH, NTH_WEEKDAY, LAST_DAY }

/**
 * The repetition choices offered by the UI. Converts to the RRULE subset the app supports
 * (see RecurrenceFormatter): DAILY/WEEKLY/MONTHLY/YEARLY with INTERVAL, BYDAY, BYMONTHDAY, UNTIL.
 */
data class Repetition(
    val unit: RepeatUnit = RepeatUnit.DAY,
    val interval: Int = 1,
    /** Weekly only; empty means the weekday of the start date. */
    val weekdays: Set<DayOfWeek> = emptySet(),
    val monthlyMode: MonthlyMode = MonthlyMode.DAY_OF_MONTH,
    val endDate: LocalDate? = null,
) {
    fun toRrule(startDate: LocalDate, zone: ZoneId = ZoneId.systemDefault()): String {
        val parts = mutableListOf<String>()
        parts += "FREQ=" + when (unit) {
            RepeatUnit.DAY -> "DAILY"
            RepeatUnit.WEEK -> "WEEKLY"
            RepeatUnit.MONTH -> "MONTHLY"
            RepeatUnit.YEAR -> "YEARLY"
        }
        if (interval > 1) parts += "INTERVAL=$interval"
        when (unit) {
            RepeatUnit.WEEK -> if (weekdays.isNotEmpty()) {
                parts += "BYDAY=" + weekdays.sorted().joinToString(",") { it.code() }
            }

            RepeatUnit.MONTH -> when (monthlyMode) {
                MonthlyMode.DAY_OF_MONTH -> parts += "BYMONTHDAY=${startDate.dayOfMonth}"
                MonthlyMode.NTH_WEEKDAY -> {
                    val nth = (startDate.dayOfMonth - 1) / 7 + 1
                    parts += "BYDAY=$nth${startDate.dayOfWeek.code()}"
                }

                MonthlyMode.LAST_DAY -> parts += "BYMONTHDAY=-1"
            }

            else -> Unit
        }
        endDate?.let {
            val endOfDay = it.plusDays(1).atStartOfDay(zone).minusSeconds(1).withZoneSameInstant(ZoneOffset.UTC)
            parts += "UNTIL=" + endOfDay.format(UNTIL_FORMAT)
        }
        return parts.joinToString(";")
    }

    companion object {
        private val UNTIL_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

        /** Label such as "second Tuesday" for the start date. */
        fun nthWeekdayLabel(date: LocalDate): String {
            val nth = (date.dayOfMonth - 1) / 7 + 1
            val ordinal = listOf("first", "second", "third", "fourth", "fifth")[nth - 1]
            return "$ordinal ${date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.US)}"
        }

        private fun DayOfWeek.code() = name.take(2)
    }
}
