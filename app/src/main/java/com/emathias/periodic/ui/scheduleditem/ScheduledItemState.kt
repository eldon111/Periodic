package com.emathias.periodic.ui.scheduleditem

import com.emathias.periodic.db.entities.ScheduledItem

data class ScheduledItemState(
    val scheduledItems: List<ScheduledItem> = emptyList(),
    val showingAddDialog: Boolean = false,
    val showingConfirmDialog: Boolean = false,
    val pendingScheduledItem: ScheduledItem? = null,
)
