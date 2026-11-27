package com.emathias.periodic.ui.todoitem

import com.emathias.periodic.db.entities.TodoItem

sealed interface TodoItemEvent {
    data class Check(val todoItem: TodoItem) : TodoItemEvent
    data class Add(val title: String, val dueDate: java.time.LocalDate) : TodoItemEvent
}
