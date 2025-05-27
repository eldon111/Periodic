package com.emathias.periodic.ui.todolist

import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.db.entities.TodoItem

data class TodoListState(
    val todoItems: List<TodoItem> = emptyList(),
    val showingAddDialog: Boolean = false,
    val showingConfirmDialog: Boolean = false,
    val pendingScheduledItem: ScheduledItem? = null,
)