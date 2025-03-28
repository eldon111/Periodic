package com.emathias.periodic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity
data class ScheduledItemProcessHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val processedAt: Instant,
)
