package com.emathias.periodic.ui.todolist

import com.emathias.periodic.db.entities.TodoItem

sealed interface TodoListEvent {
    data class Check(val todoItem: TodoItem) : TodoListEvent
    data class Uncheck(val todoItem: TodoItem) : TodoListEvent
    data class AddItem(val todoItem: TodoItem) : TodoListEvent
}