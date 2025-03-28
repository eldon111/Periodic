package com.emathias.periodic.ui.todolist

import com.emathias.periodic.db.entities.TodoItem

data class TodoListState(
    val todoItems: List<TodoItem> = emptyList(),
    val showingDialog: Boolean = false,
)