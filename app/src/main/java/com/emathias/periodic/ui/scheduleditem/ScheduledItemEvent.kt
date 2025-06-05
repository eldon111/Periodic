package com.emathias.periodic.ui.scheduleditem

import com.emathias.periodic.db.entities.ScheduledItem

sealed interface ScheduledItemEvent {
    object ShowAddDialog : ScheduledItemEvent
    object HideAddDialog : ScheduledItemEvent

    data class GenerateItem(val prompt: String) : ScheduledItemEvent
    data class ShowConfirmDialog(val scheduledItem: ScheduledItem) : ScheduledItemEvent
    object HideConfirmDialog : ScheduledItemEvent
    data class ConfirmScheduledItem(val scheduledItem: ScheduledItem) : ScheduledItemEvent
    object RefreshScheduledItems : ScheduledItemEvent
}
