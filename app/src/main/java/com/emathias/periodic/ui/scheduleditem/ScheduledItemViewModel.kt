package com.emathias.periodic.ui.scheduleditem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emathias.periodic.db.dao.ScheduledItemDao
import com.emathias.periodic.db.dao.TodoItemDao
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.model.Schedule
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.ArchiveItem
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.CreateItem
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.HideAddDialog
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.HideDeleteConfirmation
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.HideEditDialog
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.ShowDeleteConfirmation
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.ShowEditDialog
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.RefreshScheduledItems
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.ShowAddDialog
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.UpdateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

class ScheduledItemViewModel(
    private val scheduledItemDao: ScheduledItemDao,
    private val todoItemDao: TodoItemDao,
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduledItemState())
    private val _scheduledItems =
        scheduledItemDao.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = combine(_state, _scheduledItems) { state, scheduledItems ->
        state.copy(scheduledItems = scheduledItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduledItemState())

    fun onEvent(event: ScheduledItemEvent) {
        when (event) {
            ShowAddDialog -> _state.update {
                it.copy(showingAddDialog = true, editingItem = null)
            }

            HideAddDialog -> _state.update { it.copy(showingAddDialog = false) }

            is ShowEditDialog -> _state.update {
                it.copy(showingAddDialog = false, editingItem = event.scheduledItem)
            }

            HideEditDialog -> _state.update { it.copy(editingItem = null) }

            is CreateItem -> viewModelScope.launch {
                val zone = ZoneId.systemDefault()
                val start = event.startDate.atStartOfDay(zone)
                val schedule = Schedule(start, event.repetition?.toRrule(event.startDate, zone))
                val title = event.title.trim()
                scheduledItemDao.insert(
                    ScheduledItem(
                        title = title,
                        description = event.description.trim().ifEmpty { title },
                        schedule = schedule,
                    )
                )
                onEvent(HideAddDialog)
            }

            is UpdateItem -> viewModelScope.launch {
                val title = event.title.trim()
                val description = event.description.trim().ifEmpty { title }
                scheduledItemDao.update(
                    event.scheduledItem.copy(
                        title = title,
                        description = description,
                        schedule = event.scheduledItem.schedule.withEndDate(event.endDate),
                    )
                )
                todoItemDao.updateOpenContent(event.scheduledItem.id, title, description)
                onEvent(HideEditDialog)
            }

            is ShowDeleteConfirmation -> _state.update {
                it.copy(itemToDelete = event.scheduledItem)
            }

            HideDeleteConfirmation -> _state.update { it.copy(itemToDelete = null) }

            is ArchiveItem -> viewModelScope.launch {
                scheduledItemDao.update(
                    event.scheduledItem.copy(archivedAt = java.time.Instant.now())
                )
                _state.update { it.copy(itemToDelete = null, editingItem = null) }
            }

            RefreshScheduledItems -> Unit
        }
    }
}
