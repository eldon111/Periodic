package com.emathias.periodic.ui.todoitem

import com.emathias.periodic.db.entities.TodoItem

data class TodoItemState(
    val todoItems: List<TodoItem> = emptyList(),
)
