package com.emathias.periodic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.shyiko.skedule.Schedule
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity
data class ScheduledItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val firstOccurrence: Instant,
    val repeats: Boolean = false,
    val intervalInMinutes: Long? = null,
    val expiration: Instant? = null,
) {
    val schedule: Schedule? by lazy {
        intervalInMinutes?.let { Schedule.every(it, ChronoUnit.MINUTES) }
//        Schedule.parse(cronString)
    }
}
