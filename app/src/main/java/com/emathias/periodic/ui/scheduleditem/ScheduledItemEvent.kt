package com.emathias.periodic.ui.scheduleditem

import com.emathias.periodic.model.Repetition
import java.time.LocalDate

sealed interface ScheduledItemEvent {
    object ShowAddDialog : ScheduledItemEvent
    object HideAddDialog : ScheduledItemEvent

    data class CreateItem(
        val title: String,
        val description: String,
        val startDate: LocalDate,
        val repetition: Repetition?,
    ) : ScheduledItemEvent
    object RefreshScheduledItems : ScheduledItemEvent
}
