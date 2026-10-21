package com.emathias.periodic.ui.scheduleditem

import com.emathias.periodic.model.Repetition
import com.emathias.periodic.db.entities.ScheduledItem
import java.time.LocalDate

sealed interface ScheduledItemEvent {
    object ShowAddDialog : ScheduledItemEvent
    object HideAddDialog : ScheduledItemEvent
    data class ShowEditDialog(val scheduledItem: ScheduledItem) : ScheduledItemEvent
    object HideEditDialog : ScheduledItemEvent

    data class CreateItem(
        val title: String,
        val description: String,
        val startDate: LocalDate,
        val repetition: Repetition?,
    ) : ScheduledItemEvent

    data class UpdateItem(
        val scheduledItem: ScheduledItem,
        val title: String,
        val description: String,
        val endDate: LocalDate?,
    ) : ScheduledItemEvent

    data class ShowDeleteConfirmation(val scheduledItem: ScheduledItem) : ScheduledItemEvent
    object HideDeleteConfirmation : ScheduledItemEvent
    data class ArchiveItem(val scheduledItem: ScheduledItem) : ScheduledItemEvent

    object RefreshScheduledItems : ScheduledItemEvent
}
