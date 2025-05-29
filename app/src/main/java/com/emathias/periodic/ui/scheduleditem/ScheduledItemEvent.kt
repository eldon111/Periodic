package com.emathias.periodic.ui.scheduleditem

import com.emathias.periodic.db.entities.ScheduledItem

sealed interface ScheduledItemEvent {
    //    data class Check(val todoItem: TodoItem) : ScheduledItemEvent
//    data class Uncheck(val todoItem: TodoItem) : ScheduledItemEvent
    object ShowAddDialog : ScheduledItemEvent
    object HideAddDialog : ScheduledItemEvent

    //    data class AddItem(val todoItem: TodoItem) : ScheduledItemEvent
    data class GenerateItem(val prompt: String) : ScheduledItemEvent
    data class ShowConfirmDialog(val scheduledItem: ScheduledItem) : ScheduledItemEvent
    object HideConfirmDialog : ScheduledItemEvent
    data class ConfirmScheduledItem(val scheduledItem: ScheduledItem) : ScheduledItemEvent
}
