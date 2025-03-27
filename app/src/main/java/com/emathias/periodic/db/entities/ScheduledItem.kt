package com.emathias.periodic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.shyiko.skedule.Schedule
import java.time.Instant

@Entity
data class ScheduledItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val cronString: String,
    val expiration: Instant? = null,
) {
    val schedule: Schedule by lazy {
        Schedule.parse(cronString)
    }
}
