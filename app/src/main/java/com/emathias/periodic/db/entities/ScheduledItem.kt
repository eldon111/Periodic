package com.emathias.periodic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cronutils.model.Cron
import com.emathias.periodic.util.DateTimeUtils
import java.time.Instant

@Entity
data class ScheduledItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val startsAt: Instant,
    val repeats: Boolean = false,
    val cronExpression: Cron? = null,
    val expiration: Instant? = null,
) {
    val firstOccurrence: Instant by lazy {
        DateTimeUtils.getFirstOccurrence(startsAt, cronExpression)
    }
}

