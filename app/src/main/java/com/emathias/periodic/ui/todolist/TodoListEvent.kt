package com.emathias.periodic.ui.todolist

import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.db.entities.TodoItem

sealed interface TodoListEvent {
    data class Check(val todoItem: TodoItem) : TodoListEvent
    data class Uncheck(val todoItem: TodoItem) : TodoListEvent
    object ShowAddDialog : TodoListEvent
    object HideAddDialog : TodoListEvent
    data class AddItem(val todoItem: TodoItem) : TodoListEvent
    data class GenerateItem(val prompt: String) : TodoListEvent
    data class ShowConfirmDialog(val scheduledItem: ScheduledItem) : TodoListEvent
    object HideConfirmDialog : TodoListEvent
    data class ConfirmScheduledItem(val scheduledItem: ScheduledItem) : TodoListEvent
}