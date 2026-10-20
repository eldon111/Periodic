package com.emathias.periodic.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ScheduledItem::class,
            parentColumns = ["id"],
            childColumns = ["scheduledItemId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("scheduledItemId")],
)
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduledItemId: Long,
    val title: String,
    val description: String,
    val dueDate: LocalDate,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null,
) {
    val completed: Boolean get() = completedAt != null
}
