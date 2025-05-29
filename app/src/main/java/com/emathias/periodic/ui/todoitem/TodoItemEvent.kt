package com.emathias.periodic.ui.todoitem

import com.emathias.periodic.db.entities.TodoItem

sealed interface TodoItemEvent {
    data class Check(val todoItem: TodoItem) : TodoItemEvent
    data class Uncheck(val todoItem: TodoItem) : TodoItemEvent

    //    object ShowAddDialog : TodoItemEvent
//    object HideAddDialog : TodoItemEvent
    data class AddItem(val todoItem: TodoItem) : TodoItemEvent
//    data class GenerateItem(val prompt: String) : TodoItemEvent
//    data class ShowConfirmDialog(val scheduledItem: ScheduledItem) : TodoItemEvent
//    object HideConfirmDialog : TodoItemEvent
//    data class ConfirmTodoItem(val scheduledItem: ScheduledItem) : TodoItemEvent
}
