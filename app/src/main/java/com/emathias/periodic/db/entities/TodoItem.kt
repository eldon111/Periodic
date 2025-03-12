package com.emathias.periodic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val text: String,
    val checked: Boolean = false
)
