package com.emathias.periodic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class ScheduledItem(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val description: String,
    val cronString: String,
    val expiration: LocalDateTime,
)
