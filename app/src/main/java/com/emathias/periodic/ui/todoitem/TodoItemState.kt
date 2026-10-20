package com.emathias.periodic.ui.todoitem

import com.emathias.periodic.db.entities.TodoItem
import java.time.LocalDate

data class TodoItemState(
    val openItems: List<TodoItem> = emptyList(),
    val completedItems: List<TodoItem> = emptyList(),
    val today: LocalDate = LocalDate.now(),
)
