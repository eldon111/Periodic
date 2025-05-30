package com.emathias.periodic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cronutils.model.Cron
import com.cronutils.model.time.ExecutionTime
import java.time.Instant
import java.time.ZoneId

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
        cronExpression?.let { cron ->
            ExecutionTime.forCron(cron)
                .nextExecution(startsAt.atZone(ZoneId.systemDefault()))
                .orElse(null)
                ?.toInstant()
        } ?: startsAt
    }
}

