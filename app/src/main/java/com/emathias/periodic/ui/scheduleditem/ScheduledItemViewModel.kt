package com.emathias.periodic.ui.scheduleditem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emathias.periodic.db.dao.ScheduledItemDao
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.model.Schedule
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.CreateItem
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.HideAddDialog
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.RefreshScheduledItems
import com.emathias.periodic.ui.scheduleditem.ScheduledItemEvent.ShowAddDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

class ScheduledItemViewModel(
    private val scheduledItemDao: ScheduledItemDao,
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
            ShowAddDialog -> _state.update { it.copy(showingAddDialog = true) }

            HideAddDialog -> _state.update { it.copy(showingAddDialog = false) }

            is CreateItem -> viewModelScope.launch {
                val zone = ZoneId.systemDefault()
                val start = event.startDate.atStartOfDay(zone)
                val schedule = Schedule(start, event.repetition?.toRrule(event.startDate, zone))
                scheduledItemDao.insert(
                    ScheduledItem(
                        title = event.title.trim(),
                        description = event.description.trim().ifEmpty { event.title.trim() },
                        schedule = schedule,
                    )
                )
                onEvent(HideAddDialog)
            }

            RefreshScheduledItems -> Unit
        }
    }
}
