package com.emathias.periodic.db.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.emathias.periodic.model.Schedule
import java.time.Instant

@Entity
data class ScheduledItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val schedule: Schedule,
) {
    @get:Ignore
    val startsAt: Instant get() = schedule.firstOccurrence

    @get:Ignore
    val repeats: Boolean get() = schedule.repeats

    @get:Ignore
    val expiration: Instant? get() = schedule.until

    @get:Ignore
    val firstOccurrence: Instant get() = schedule.firstOccurrence
}
