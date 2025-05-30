package com.emathias.periodic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.Period

@Entity
data class ScheduledItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val firstOccurrence: Instant,
    val repeats: Boolean = false,
    val interval: Period? = null,
    val expiration: Instant? = null,
)
